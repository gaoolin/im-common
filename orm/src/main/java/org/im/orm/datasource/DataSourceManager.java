package org.im.orm.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源管理器
 * 负责多数据源的注册、获取和管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public class DataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
    private static final Map<String, ConnectionProvider> dataSourceMap = new ConcurrentHashMap<>();

    /**
     * 注册数据源
     *
     * @param name     数据源名称
     * @param provider 连接提供者
     */
    public static void registerDataSource(String name, ConnectionProvider provider) {
        dataSourceMap.put(name, provider);
    }

    /**
     * 获取数据源
     *
     * @param name 数据源名称
     * @return 连接提供者
     */
    public static ConnectionProvider getDataSource(String name) {
        return dataSourceMap.get(name);
    }

    /**
     * 获取所有数据源名称
     *
     * @return 数据源名称集合
     */
    public static Set<String> getDataSourceNames() {
        return dataSourceMap.keySet();
    }

    /**
     * 注销数据源
     *
     * @param name 数据源名称
     */
    public static void unregisterDataSource(String name) {
        ConnectionProvider provider = dataSourceMap.remove(name);
        if (provider != null) {
            provider.close();
        }
    }

    /**
     * 关闭所有数据源
     */
    public static void closeAll() {
        for (ConnectionProvider provider : dataSourceMap.values()) {
            try {
                provider.close();
            } catch (Exception e) {
                logger.error("Close data source error: {}", e.getMessage(), e);
            }
        }
        dataSourceMap.clear();
    }
}