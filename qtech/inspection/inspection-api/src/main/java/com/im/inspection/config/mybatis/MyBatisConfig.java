package com.im.inspection.config.mybatis;

import com.im.inspection.config.dynamic.DynamicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 配置数据源：使用Druid数据源，并通过@ConfigurationProperties注解从配置文件中读取属性。
 * 扫描Mapper接口：使用@MapperScan注解来自动扫描指定包下的Mapper接口。
 * 创建SqlSessionFactory：配置SqlSessionFactory，设置数据源，并指定Mapper XML文件的位置。
 * 配置事务管理器：使用DataSourceTransactionManager来管理事务。
 * <p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/16 14:56:37
 */

@Configuration
@MapperScan(basePackages = "com.im.inspection.mapper") // 指定Mapper接口的包路径
public class MyBatisConfig {

    // 如果需要使用动态数据源，可以参考之前提供的动态数据源配置(动态数据源配置)
    @Bean
    public SqlSessionFactory sqlSessionFactory(DynamicDataSource dynamicDataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dynamicDataSource);

        // 设置Mapper XML文件的位置
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/**/*.xml")); // 根据实际情况调整路径

        // 如果有配置文件，也可以在这里设置
        factory.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));

        // 硬编码类型别名的包
        factory.setTypeAliasesPackage("com.im.inspection.entity.*"); // 替换为实际包路径

        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("firstDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
