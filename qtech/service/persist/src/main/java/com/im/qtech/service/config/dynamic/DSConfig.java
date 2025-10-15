package com.im.qtech.service.config.dynamic;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;
/**
 * 多数据源配置
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

/**
 * 多数据源 + 动态路由 + MyBatis-Plus 配置
 */
@EnableConfigurationProperties(MybatisPlusProperties.class)
@org.springframework.context.annotation.Configuration
@MapperScan(basePackages = "com.im.qtech.service.msg.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class DSConfig {

    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;

    // ==================== 数据源配置 ====================

    @Bean
    @ConfigurationProperties("spring.datasource.druid.first")
    public DataSource firstDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.second")
    public DataSource secondDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.third")
    public DataSource thirdDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    // ==================== 动态路由数据源 ====================

    @Bean
    @Primary
    public RoutingDataSource routingDataSource(
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

    // ==================== SqlSessionTemplate (MyBatis-Plus) ====================

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("routingDataSource") DataSource routingDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(routingDataSource);

        // typeAliasesPackage
        if (mybatisPlusProperties.getTypeAliasesPackage() != null) {
            factoryBean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());
        }

        // mapperLocations
        if (mybatisPlusProperties.getMapperLocations() != null && mybatisPlusProperties.getMapperLocations().length > 0) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            factoryBean.setMapperLocations(resolver.getResources(String.join(",", mybatisPlusProperties.getMapperLocations())));
        }

        // MyBatis Configuration
        if (mybatisPlusProperties.getConfiguration() != null) {
            Configuration configuration = getConfiguration();
            factoryBean.setConfiguration(configuration);

        }

        return new SqlSessionTemplate(factoryBean.getObject());
    }

    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setCacheEnabled(mybatisPlusProperties.getConfiguration().getCacheEnabled());
        configuration.setCallSettersOnNulls(mybatisPlusProperties.getConfiguration().getCallSettersOnNulls());
        configuration.setUseGeneratedKeys(mybatisPlusProperties.getConfiguration().getUseGeneratedKeys());
        configuration.setUseColumnLabel(mybatisPlusProperties.getConfiguration().getUseColumnLabel());
        configuration.setDefaultStatementTimeout(mybatisPlusProperties.getConfiguration().getDefaultStatementTimeout());
        configuration.setMapUnderscoreToCamelCase(mybatisPlusProperties.getConfiguration().getMapUnderscoreToCamelCase());
        configuration.setLogImpl(mybatisPlusProperties.getConfiguration().getLogImpl());
        configuration.setJdbcTypeForNull(mybatisPlusProperties.getConfiguration().getJdbcTypeForNull());
        return configuration;
    }

    // ==================== JdbcTemplate & 事务管理器 ====================

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("routingDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("routingDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}
