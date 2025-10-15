package org.im.orm.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCP连接提供者实现
 * 基于HikariCP高性能连接池实现ConnectionProvider接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class HikariConnectionProvider implements ConnectionProvider {
    private static final Logger logger = LoggerFactory.getLogger(HikariConnectionProvider.class);
    private final HikariDataSource dataSource;

    /**
     * 构造函数
     *
     * @param config HikariCP配置
     */
    public HikariConnectionProvider(HikariConfig config) {
        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("Error releasing connection", e);
            }
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * 获取内部的HikariDataSource
     *
     * @return HikariDataSource实例
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
