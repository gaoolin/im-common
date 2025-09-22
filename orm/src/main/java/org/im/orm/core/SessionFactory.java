package org.im.orm.core;


import org.im.exception.type.orm.ORMException;
import org.im.orm.datasource.DataSourceManager;

/**
 * 会话工厂
 * 用于创建会话实例
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class SessionFactory {

    /**
     * 创建多数据源会话
     *
     * @param defaultDataSourceName 默认数据源名称
     * @return 多数据源会话
     */
    public static MultiDataSourceSession createSession(String defaultDataSourceName) {
        // 检查是否存在数据源
        if (!DataSourceManager.getDataSourceNames().contains(defaultDataSourceName)) {
            throw new ORMException("默认数据源不存在: " + defaultDataSourceName);
        }

        return new MultiDataSourceSessionImpl(defaultDataSourceName);
    }

    /**
     * 创建会话（使用默认数据源）
     *
     * @return 多数据源会话
     */
    public static MultiDataSourceSession createSession() {
        // 获取第一个数据源作为默认数据源
        java.util.Set<String> dataSourceNames = DataSourceManager.getDataSourceNames();
        if (dataSourceNames.isEmpty()) {
            throw new ORMException("没有配置任何数据源");
        }

        String defaultDataSourceName = dataSourceNames.iterator().next();
        return createSession(defaultDataSourceName);
    }
}
