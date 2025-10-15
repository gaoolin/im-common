package com.im.qtech.service.config.dynamic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态路由数据源
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

public class RoutingDataSource extends AbstractRoutingDataSource {

    public RoutingDataSource(DataSource defaultDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DSContextHolder.get();
    }
}
