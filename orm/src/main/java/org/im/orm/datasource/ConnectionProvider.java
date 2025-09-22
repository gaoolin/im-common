package org.im.orm.datasource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接提供者接口
 * 定义连接池的基本操作
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public interface ConnectionProvider {
    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection() throws SQLException;

    /**
     * 释放数据库连接
     *
     * @param connection 数据库连接
     */
    void releaseConnection(Connection connection);

    /**
     * 关闭连接提供者
     */
    void close();
}
