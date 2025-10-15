package org.im.orm.core;

/**
 * 支持多数据源的会话接口
 * 继承基础Session接口，增加数据源切换功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public interface MultiDataSourceSession extends Session {
    /**
     * 切换数据源
     *
     * @param dataSourceName 数据源名称
     */
    void switchDataSource(String dataSourceName);

    /**
     * 获取当前数据源名称
     *
     * @return 当前数据源名称
     */
    String getCurrentDataSourceName();
}
