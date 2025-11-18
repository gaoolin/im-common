package com.im.inspection.config.dynamic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * 重写determineCurrentLookupKey()，实现选择数据源的方式
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/08 10:17:17
 */


public class DynamicDataSource extends AbstractRoutingDataSource {

    // 每个线程指定各自需要使用的数据源名称
    private static final ThreadLocal<DataSourceNames> CONTEXT_HOLDER = new ThreadLocal<>();

    public DynamicDataSource(Map<Object, Object> targetDataSources, Object defaultTargetDataSource) {
        // 设置默认的数据源
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        // 设置配置了的所用数据源
        super.setTargetDataSources(targetDataSources);
        // 将targetDataSources复制给resolvedDataSources;
        // 将defaultTargetDataSource复制给resolvedDefaultDataSource;
        super.afterPropertiesSet();
    }

    static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSource();
    }

    private DataSourceNames getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    static void setDataSource(DataSourceNames dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }
}
