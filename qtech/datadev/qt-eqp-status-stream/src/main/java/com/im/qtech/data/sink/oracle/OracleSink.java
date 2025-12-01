package com.im.qtech.data.sink.oracle;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.model.EqNetworkStatus;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.im.qtech.data.util.Constants.*;

/**
 * Oracle Sink with connection pooling, batching and retry mechanism
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/07/30 09:02:34
 */
public class OracleSink extends RichSinkFunction<String> {
    private static final Logger logger = LoggerFactory.getLogger(OracleSink.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String sql;
    private final int batchSize;
    private final int maxRetries;
    private final long batchIntervalMs;
    private final ReentrantLock bufferLock = new ReentrantLock(); // 添加锁保护

    // 保留这些作为类字段，因为其他方法需要访问它们
    private transient DruidDataSource dataSource;
    private transient Connection connection;
    private transient PreparedStatement preparedStatement;
    private transient List<String> batchBuffer;
    private transient ScheduledExecutorService scheduler;
    private transient volatile boolean flushScheduled = false;

    // 指标监控
    private transient Counter recordsProcessed;
    private transient Meter recordsPerSecond;

    public OracleSink() {
        this.jdbcUrl = ORACLE_URL;
        this.username = ORACLE_USER;
        this.password = ORACLE_PASSWORD;
        this.sql = ORACLE_SQL;
        this.batchSize = ORACLE_BATCH_SIZE;
        this.maxRetries = ORACLE_MAX_RETRIES;
        this.batchIntervalMs = ORACLE_BATCH_INTERVAL_MS;
    }

    public OracleSink(String jdbcUrl, String username, String password, String sql, int batchSize, int maxRetries, long batchIntervalMs) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.sql = sql;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.batchIntervalMs = batchIntervalMs;
    }

    // 在 open 方法中配置数据源时添加以下配置
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        // 显式加载 Oracle 驱动类
        try {
            Class.forName(ORACLE_DRIVER_CLASS);
            logger.info("Successfully loaded Oracle JDBC driver: {}", ORACLE_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            logger.error("Oracle JDBC Driver not found: {}", ORACLE_DRIVER_CLASS, e);
            throw new RuntimeException("Oracle JDBC Driver not found: " + ORACLE_DRIVER_CLASS, e);
        }

        // 初始化 Druid 连接池 - 优化配置
        dataSource = new DruidDataSource(); // 注意这里赋值给类字段
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(ORACLE_DRIVER_CLASS);

        // 优化连接池配置以提高吞吐量
        dataSource.setInitialSize(ORACLE_INITIAL_SIZE);
        dataSource.setMinIdle(ORACLE_MIN_IDLE);
        dataSource.setMaxActive(ORACLE_MAX_ACTIVE);
        dataSource.setMaxWait(30000);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        // 关键优化参数
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(1800);
        dataSource.setLogAbandoned(true);

        connection = dataSource.getConnection(); // 赋值给类字段
        preparedStatement = connection.prepareStatement(sql); // 赋值给类字段

        // 设置 fetch size 和 auto commit
        preparedStatement.setFetchSize(1000);
        connection.setAutoCommit(false);

        batchBuffer = new ArrayList<>(batchSize); // 赋值给类字段

        // 初始化定时调度器
        scheduler = Executors.newSingleThreadScheduledExecutor(); // 赋值给类字段

        // 初始化指标
        recordsProcessed = getRuntimeContext().getMetricGroup().counter("oracleRecordsProcessed");

        recordsPerSecond = getRuntimeContext().getMetricGroup().meter("oracleRecordsPerSecond", new MeterView(60));
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        bufferLock.lock();
        try {
            batchBuffer.add(value);

            // 达到批量大小立即执行
            if (batchBuffer.size() >= batchSize) {
                executeBatchWithRetry();
                flushScheduled = false;
            } else if (!flushScheduled) {
                // 启动定时刷新
                flushScheduled = true;
                scheduler.schedule(this::flushBuffer, batchIntervalMs, TimeUnit.MILLISECONDS);
            }
        } finally {
            bufferLock.unlock();
        }
    }

    private void flushBuffer() {
        bufferLock.lock();
        try {
            if (!batchBuffer.isEmpty()) {
                try {
                    executeBatchWithRetry();
                } catch (Exception e) {
                    logger.error(">>>>> Scheduled buffer flush failed", e);
                }
            }
            flushScheduled = false;
        } finally {
            bufferLock.unlock();
        }
    }

    private void executeBatchWithRetry() throws Exception {
        if (batchBuffer.isEmpty()) {
            return;
        }

        // 创建当前批次的副本，避免并发修改问题
        List<String> currentBatch = new ArrayList<>(batchBuffer);
        batchBuffer.clear();

        Exception lastException = null;
        boolean success = false;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                connection.setAutoCommit(false); // 开始事务

                for (String value : currentBatch) {
                    try {
                        ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
                        EqNetworkStatus record = objectMapper.readValue(value, EqNetworkStatus.class);
                        // device_id - varchar(30)
                        preparedStatement.setString(1, record.getDeviceId());

                        // receive_date - date 类型
                        if (record.getReceiveDate() != null && !record.getReceiveDate().isEmpty()) {
                            try {
                                // 提取日期部分（假设格式为 yyyy-MM-dd HH:mm:ss）
                                String datePart = record.getReceiveDate().split(" ")[0];
                                preparedStatement.setDate(2, java.sql.Date.valueOf(datePart));
                            } catch (Exception e) {
                                logger.warn(">>>>> Failed to parse receiveDate: {}, setting to NULL", record.getReceiveDate());
                                preparedStatement.setNull(2, java.sql.Types.DATE);
                            }
                        } else {
                            preparedStatement.setNull(2, java.sql.Types.DATE);
                        }

                        // device_type - varchar(30)
                        preparedStatement.setString(3, record.getDeviceType());

                        // lot_name - varchar(300)
                        preparedStatement.setString(4, record.getLotName());

                        // status - int2 (smallint) 类型
                        if (record.getStatus() == null) {
                            preparedStatement.setNull(5, java.sql.Types.SMALLINT);
                        } else {
                            try {
                                short statusValue = Short.parseShort(record.getStatus());
                                preparedStatement.setShort(5, statusValue);
                            } catch (NumberFormatException e) {
                                logger.warn(">>>>> Invalid status value: {}, setting to NULL", record.getStatus());
                                preparedStatement.setNull(5, java.sql.Types.SMALLINT);
                            }
                        }

                        // remote_control - varchar(5)
                        preparedStatement.setString(6, record.getRemoteControl());

                        // last_update - timestamp(6)
                        if (record.getLastUpdated() != null) {
                            preparedStatement.setTimestamp(7, new java.sql.Timestamp(record.getLastUpdated().getTime()));
                        } else {
                            preparedStatement.setNull(7, java.sql.Types.TIMESTAMP);
                        }

                        preparedStatement.addBatch();
                    } catch (Exception e) {
                        logger.warn(">>>>> Failed to parse or add record to batch (attempt {}/{}): {}", attempt, maxRetries, value, e);
                    }
                }

                preparedStatement.executeBatch();
                connection.commit(); // 提交事务
                preparedStatement.clearBatch();

                // 更新指标
                recordsProcessed.inc(currentBatch.size());
                recordsPerSecond.markEvent(currentBatch.size());

                success = true;
                break;

            } catch (SQLException e) {
                lastException = e;
                String errorMessage = e.getMessage();

                // 专门处理死锁异常
                if (errorMessage.contains("ORA-00060") || errorMessage.contains("deadlock")) {
                    logger.warn(">>>>> Deadlock detected (attempt {}/{}): {}", attempt, maxRetries, errorMessage);

                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error("Failed to rollback transaction after deadlock", rollbackEx);
                    }

                    if (attempt < maxRetries) {
                        // 死锁时增加随机延迟，避免再次冲突
                        long delay = (1000L * attempt) + (new java.util.Random().nextInt(2000));
                        logger.info(">>>>> Waiting {} ms before retry due to deadlock", delay);
                        Thread.sleep(delay);
                        reconnect();
                        continue; // 继续重试循环
                    }
                } else {
                    // 其他SQL异常的处理
                    logger.warn(">>>>> Batch execution failed (attempt {}/{}): {}", attempt, maxRetries, errorMessage);

                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error(">>>>> Failed to rollback transaction", rollbackEx);
                    }

                    if (attempt < maxRetries) {
                        Thread.sleep(1000L * attempt);
                        reconnect();
                    }
                }
            }
        }

        if (!success) {
            logger.error(">>>>> Failed to execute batch after {} attempts. Dropping {} records.", maxRetries, currentBatch.size());
            throw new RuntimeException(">>>>> Failed to execute batch after " + maxRetries + " attempts", lastException);
        }
    }

    private void reconnect() throws SQLException {
        if (preparedStatement != null && !preparedStatement.isClosed()) {
            preparedStatement.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = dataSource.getConnection();
        preparedStatement = connection.prepareStatement(sql);
        connection.setAutoCommit(false);
    }

    @Override
    public void close() throws Exception {
        // 取消定时任务
        if (scheduler != null) {
            scheduler.shutdown();
        }

        // 在关闭前执行剩余的批量数据
        bufferLock.lock();
        try {
            if (!batchBuffer.isEmpty()) {
                try {
                    executeBatchWithRetry();
                } catch (Exception e) {
                    logger.error(">>>>> Failed to flush remaining batch data", e);
                }
            }
        } finally {
            bufferLock.unlock();
        }

        // 安全关闭资源
        closeQuietly(preparedStatement);
        closeQuietly(connection);
        closeQuietly(dataSource);
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
    }
}