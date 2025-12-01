package com.im.qtech.data.common;

import com.im.qtech.data.async.AsyncExecutorProvider;
import com.im.qtech.data.config.DataSourceManager;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * 通用的简单 JDBC Sink（非二阶段提交）
 * 去掉两阶段提交，只在 invoke() 里做批量写入 + 提交，适用于幂等更新。
 * 不受 checkpoint 生命周期影响，不会出现“Sink 没有运行导致 checkpoint 失败”的问题。
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/04 11:01:37
 */

public abstract class BaseJdbcSimpleSink<T> extends RichSinkFunction<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseJdbcSimpleSink.class);
    private final String dsKey;
    private final String url;
    private final String user;
    private final String password;
    private final String driverClass;

    protected transient DataSource dataSource;

    public BaseJdbcSimpleSink(String dsKey, String url, String user, String password, String driverClass) {
        this.dsKey = dsKey;
        this.url = url;
        this.user = user;
        this.password = password;
        this.driverClass = driverClass;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.dataSource = DataSourceManager.getDataSource(dsKey, url, user, password, driverClass);
        // 检测一下连接是否可用，避免延迟抛错
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Database connection validation failed for: " + dsKey);
            }
        }
    }

    @Override
    public void close() throws Exception {
        // 不直接关闭 DataSource，由 DataSourceManager 控制全局生命周期
    }

    protected abstract String getSql();

    protected abstract void setPreparedStatementParams(PreparedStatement ps, T record) throws SQLException;

    @Override
    public void invoke(T value, Context context) throws Exception {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(getSql())) {

                ps.setQueryTimeout(30); // 30秒超时
                setPreparedStatementParams(ps, value);
                ps.executeUpdate();

            } catch (SQLException e) {
                logger.error("数据库操作失败，记录ID: " + value.getClass().getSimpleName() + ":" + value + ", 错误: " + e.getMessage(), e);
            }
        }, AsyncExecutorProvider.getExecutor()).exceptionally(throwable -> {
            logger.error("异步任务执行失败: " + throwable.getMessage(), throwable);
            return null;
        });
    }

}