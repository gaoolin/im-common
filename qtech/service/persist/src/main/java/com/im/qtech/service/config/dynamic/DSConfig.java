package com.im.qtech.service.config.dynamic;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 多数据源 + 动态路由 + MyBatis-Plus 配置
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */
@Configuration
@MapperScan(basePackages = "com.im.qtech.service.msg.mapper")
public class DSConfig {

    // ==================== 数据源配置 ====================

    @Bean
    @ConfigurationProperties("spring.datasource.first")
    public DataSource firstDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.second")
    public DataSource secondDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.third")
    public DataSource thirdDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    // ==================== 动态路由数据源 ====================

    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("firstDataSource") DataSource first,
            @Qualifier("secondDataSource") DataSource second,
            @Qualifier("thirdDataSource") DataSource third) {

        Map<Object, Object> targetDataSources = Map.of(
                DSName.FIRST, first,
                DSName.SECOND, second,
                DSName.THIRD, third
        );
        return new RoutingDataSource(first, targetDataSources);
    }

    // ==================== 事务管理器 ====================

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("routingDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    // ==================== MyBatis-Plus 插件 ====================

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.ORACLE));
        return interceptor;
    }
}