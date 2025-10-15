package com.im.aa.inspection.config;

import com.im.aa.inspection.entity.reverse.EqpReverseDO;
import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/25
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     * 初始化 Hibernate 配置并返回 SessionFactory
     */
    public SessionFactory initializeSessionFactory() {
        try {
            // 1 初始化配置管理器
            ConfigurationManager configManager = ConfigManager.initialize();

            // 2 创建 Hibernate Configuration
            Configuration configuration = new Configuration();

            // 3 强制命名策略（无条件覆盖 XML）
            // 物理命名策略
            configuration.setPhysicalNamingStrategy(new CustomPhysicalNamingStrategy());

            // 保留默认隐式命名策略
            configuration.setImplicitNamingStrategy(
                    new org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl()
            );

            // 4 加载配置文件（可选，不存在也不会报错）
            try {
                configuration.configure("hibernate.cfg.xml");
            } catch (Exception e) {
                logger.warn("hibernate.cfg.xml 加载失败，将使用 ConfigManager 配置: {}", e.getMessage());
            }

            // 5 覆盖属性（从 ConfigManager 获取）
            configuration.setProperty("hibernate.connection.driver_class",
                    configManager.getProperty("hibernate.connection.driver_class"));
            configuration.setProperty("hibernate.connection.url",
                    configManager.getProperty("hibernate.connection.url"));
            configuration.setProperty("hibernate.connection.username",
                    configManager.getProperty("hibernate.connection.username"));
            configuration.setProperty("hibernate.connection.password",
                    configManager.getProperty("hibernate.connection.password"));
            configuration.setProperty("hibernate.dialect",
                    configManager.getProperty("hibernate.dialect"));
            configuration.setProperty("hibernate.hbm2ddl.auto",
                    configManager.getProperty("hibernate.hbm2ddl.auto", "validate"));
            configuration.setProperty("hibernate.show_sql",
                    configManager.getProperty("hibernate.show_sql", "false"));
            configuration.setProperty("hibernate.format_sql",
                    configManager.getProperty("hibernate.format_sql", "true"));
            configuration.setProperty("hibernate.current_session_context_class",
                    configManager.getProperty("hibernate.current_session_context_class", "thread"));

            // 连接池配置（可选，如果 classpath 没有 c3p0 会自动忽略）
            configuration.setProperty("hibernate.c3p0.min_size",
                    configManager.getProperty("hibernate.c3p0.min_size", "5"));
            configuration.setProperty("hibernate.c3p0.max_size",
                    configManager.getProperty("hibernate.c3p0.max_size", "20"));
            configuration.setProperty("hibernate.c3p0.timeout",
                    configManager.getProperty("hibernate.c3p0.timeout", "300"));
            configuration.setProperty("hibernate.c3p0.max_statements",
                    configManager.getProperty("hibernate.c3p0.max_statements", "50"));
            configuration.setProperty("hibernate.c3p0.idle_test_period",
                    configManager.getProperty("hibernate.c3p0.idle_test_period", "3000"));
            configuration.setProperty("hibernate.c3p0.acquire_increment", "1");

            // 6 注册实体类
            configuration.addAnnotatedClass(EqLstTplInfoDO.class);
            configuration.addAnnotatedClass(EqLstTplDO.class);
            configuration.addAnnotatedClass(EqpReverseDO.class);

            // 7 构建 SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            logger.info(">>>>> Hibernate SessionFactory 初始化成功");
            return configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception e) {
            logger.error(">>>>> Hibernate SessionFactory 初始化失败", e);
            throw new RuntimeException("Hibernate SessionFactory 初始化失败", e);
        }
    }
}

