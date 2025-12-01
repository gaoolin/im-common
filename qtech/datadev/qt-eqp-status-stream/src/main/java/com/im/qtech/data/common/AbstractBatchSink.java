package com.im.qtech.data.common;

import com.im.qtech.data.common.transaction.TransactionContext;
import com.im.qtech.data.common.transaction.TransactionContextSerializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用抽象批量 Sink，支持两阶段提交 + 批量写入
 *
 * @param <T> 输入数据类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/01
 */

public abstract class AbstractBatchSink<T> extends TwoPhaseCommitSinkFunction<T, TransactionContext, Void> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBatchSink.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String driverClass;
    private final int batchSize;
    private final int maxRetries;

    // transient 字段
    private transient List<T> buffer;
    private transient HikariDataSource dataSource;
    private transient Object bufferLock;

    public AbstractBatchSink(String jdbcUrl, String username, String password, String driverClass,
                             int batchSize, int maxRetries) {
        super(new TransactionContextSerializer(), VoidSerializer.INSTANCE);
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.driverClass = driverClass;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    /**
     * 初始化 DataSource
     */
    private void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) return;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClass);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        dataSource = new HikariDataSource(config);
        logger.info("Initialized datasource for {}", jdbcUrl);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        try {
            super.open(parameters);
            logger.info("Opening OracleAbstractBatchSink...");
            initDataSource();
            buffer = new ArrayList<>(batchSize);
            bufferLock = new Object();
            logger.info("OracleAbstractBatchSink opened successfully");
        } catch (Exception e) {
            logger.error("Failed to open OracleAbstractBatchSink", e);
            throw e; // 必须抛出，Flink 才能 failover
        }
    }

    // 反序列化后需重新初始化
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        buffer = new ArrayList<>(batchSize);
        bufferLock = new Object();
        initDataSource();
    }

    /**
     * 两阶段提交：开启事务
     */
    @Override
    protected TransactionContext beginTransaction() throws Exception {
        try {
            initDataSource();
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            long txnId = System.currentTimeMillis();
            logger.info("Begin transaction with txnId {}", txnId);
            return new TransactionContext(connection, txnId);
        } catch (Exception e) {
            logger.error("Failed to begin transaction for OracleAbstractBatchSink", e);
            throw e;
        }
    }

    /**
     * 缓冲数据，达到批量大小则写入
     */
    @Override
    protected void invoke(TransactionContext transaction, T value, Context context) throws Exception {
        synchronized (bufferLock) {
            buffer.add(value);
            if (buffer.size() >= batchSize) {
                flushBuffer(transaction.getConnection());
            }
        }
    }

    /**
     * 批量写入
     */
    private void flushBuffer(Connection connection) throws SQLException {
        if (buffer.isEmpty()) return;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (PreparedStatement ps = connection.prepareStatement(getSql())) {
                for (T record : buffer) {
                    setPreparedStatementParams(ps, record);
                    ps.addBatch();
                }
                ps.executeBatch();
                buffer.clear();
                logger.debug("Flushed batch successfully");
                return;
            } catch (Exception e) {
                lastException = e;
                logger.warn("Batch execution failed (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ignored) {
                }
            }
        }
        logger.error("Failed to execute batch after {} retries, dropping {} records.", maxRetries, buffer.size(), lastException);
        buffer.clear();
    }

    /**
     * 两阶段提交：预提交（确保数据已写入未提交的事务）
     */
    @Override
    protected void preCommit(TransactionContext transaction) throws Exception {
        logger.info("Pre-commit flush for txn {}", transaction.getTxnId());
        synchronized (bufferLock) {
            flushBuffer(transaction.getConnection());
        }
    }

    /**
     * 提交事务
     */
    @Override
    protected void commit(TransactionContext transaction) {
        try {
            transaction.getConnection().commit();
            logger.info("Committed transaction {}", transaction.getTxnId());
        } catch (SQLException e) {
            throw new RuntimeException("Commit failed", e);
        } finally {
            closeConnectionQuietly(transaction);
        }
    }

    /**
     * 回滚事务
     */
    @Override
    protected void abort(TransactionContext transaction) {
        try {
            transaction.getConnection().rollback();
            logger.warn("Aborted transaction {}", transaction.getTxnId());
        } catch (SQLException e) {
            throw new RuntimeException("Abort failed", e);
        } finally {
            closeConnectionQuietly(transaction);
        }
    }

    private void closeConnectionQuietly(TransactionContext transaction) {
        try {
            if (transaction.getConnection() != null && !transaction.getConnection().isClosed()) {
                transaction.getConnection().close();
            }
        } catch (SQLException e) {
            logger.warn("Failed to close connection: {}", e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        logger.info("Closing sink");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        super.close();
    }

    /**
     * SQL模板
     */
    protected abstract String getSql();

    /**
     * 参数绑定
     */
    protected abstract void setPreparedStatementParams(PreparedStatement ps, T record) throws SQLException;
}
