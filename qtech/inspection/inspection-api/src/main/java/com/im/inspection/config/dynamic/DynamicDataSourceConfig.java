package com.im.inspection.config.dynamic;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/05/08 10:25:11
 * desc   :
 */


import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * 多数据源配置类
 */
@Configuration
public class DynamicDataSourceConfig {

    /**
     * 实例化数据源master
     *
     * @return DataSource
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.first")
    public DataSource firstDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 实例化数据源slave
     *
     * @return DataSource
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.second")
    public DataSource secondDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 实例化DynamicDataSource
     *
     * @param firstDataSource  masterDataSource
     * @param secondDataSource slaveDataSource
     * @return DynamicDataSource
     */
    @Bean
    @Primary
    public DynamicDataSource dynamicDataSource(DataSource firstDataSource, DataSource secondDataSource) {
        HashMap<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceNames.FIRST, firstDataSource);
        targetDataSources.put(DataSourceNames.SECOND, secondDataSource);

        return new DynamicDataSource(targetDataSources, firstDataSource);
    }
}
