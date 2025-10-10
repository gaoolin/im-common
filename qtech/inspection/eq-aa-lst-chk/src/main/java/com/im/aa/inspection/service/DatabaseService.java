package com.im.aa.inspection.service;

import com.im.aa.inspection.entity.reverse.EqpReverseRecord;
import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import com.im.aa.inspection.repository.EqLstTplInfoRepository;
import com.im.aa.inspection.repository.ReverseDataRepository;
import com.im.aa.inspection.repository.TemplateDataRepository;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库服务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private SessionFactory sessionFactory;
    private EqLstTplInfoRepository templateInfoRepository;
    private TemplateDataRepository templateDataRepository;
    private ReverseDataRepository reverseDataRepository;
    private Configuration cfg;

    public DatabaseService() {
        init();
    }

    /**
     * 初始化数据库连接和仓库
     */
    private void init() {
        try {
            // 加载Hibernate配置
            Configuration configuration = new Configuration();

            // 通过配置文件加载
            configuration.configure("hibernate.cfg.xml");

            // 或者通过代码方式配置（如果配置文件不可用）
            if (!configureFromCode(configuration)) {
                logger.warn("无法加载Hibernate配置文件，使用默认配置");
            }

            // 构建SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            // 初始化各个仓库
            templateInfoRepository = new EqLstTplInfoRepository(sessionFactory);
            templateDataRepository = new TemplateDataRepository(sessionFactory);
            reverseDataRepository = new ReverseDataRepository(sessionFactory);

            logger.info("数据库服务初始化成功");
            cfg = configuration;
        } catch (Exception e) {
            logger.error("数据库服务初始化失败", e);
            throw new RuntimeException("数据库服务初始化失败", e);
        }
    }

    /**
     * 通过代码配置Hibernate
     */
    private boolean configureFromCode(Configuration configuration) {
        try {
            // 数据库连接配置
            configuration.addAnnotatedClass(EqLstTplInfoPO.class);
            configuration.addAnnotatedClass(EqLstTplDO.class);
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            configuration.setProperty("hibernate.connection.url", getDatabaseUrl());
            configuration.setProperty("hibernate.connection.username", getDatabaseUsername());
            configuration.setProperty("hibernate.connection.password", getDatabasePassword());

            // Hibernate 配置
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.current_session_context_class", "thread");

            // 添加实体类映射
            configuration.addAnnotatedClass(EqLstTplInfoPO.class);
            configuration.addAnnotatedClass(EqLstTplDO.class);
            configuration.addAnnotatedClass(EqpReverseRecord.class);

            return true;
        } catch (Exception e) {
            logger.error("代码配置Hibernate失败", e);
            return false;
        }
    }

    /**
     * 从配置管理器获取数据库URL
     */
    private String getDatabaseUrl() {
        // 从配置管理器获取数据库连接信息
        return cfg.getProperty("database.url");
    }

    /**
     * 从配置管理器获取数据库用户名
     */
    private String getDatabaseUsername() {
        return cfg.getProperty("database.username");
    }

    /**
     * 从配置管理器获取数据库密码
     */
    private String getDatabasePassword() {
        return cfg.getProperty("database.password");
    }

    /**
     * 获取参数标准值
     */
    public EqLstTplInfoPO getTplInfo(String module) {
        logger.debug(">>>>> 从数据库获取参数标准值: {}", module);
        try {
            // 根据module查询对应的模板信息
            // 这里需要具体的查询逻辑，示例：
            return templateInfoRepository.findByModule(module);
        } catch (Exception e) {
            logger.error("获取参数标准值失败: {}", module, e);
            return null;
        }
    }

    /**
     * 获取检查结果模板
     */
    public EqLstTplDO getTpl(String module) {
        logger.debug(">>>>> 从数据库获取检查结果模板: {}", module);
        try {
            // 根据module查询对应的检查模板
            return templateDataRepository.findByModule(module);
        } catch (Exception e) {
            logger.error("获取检查结果模板失败: {}", module, e);
            return new EqLstTplDO();
        }
    }

    /**
     * 保存检查结果
     */
    public void saveInspectionResult(EqpReverseRecord result) {
        logger.debug(">>>>> 保存检查结果到数据库: {}", result);
        try {
            reverseDataRepository.saveEqpReverseRecord(result);
        } catch (Exception e) {
            logger.error("保存检查结果失败: {}", result, e);
            throw new RuntimeException("保存检查结果失败", e);
        }
    }

    /**
     * 根据ID获取模板信息
     */
    public EqLstTplInfoPO getTemplateInfoById(Long id) {
        try {
            return templateInfoRepository.findById(id);
        } catch (Exception e) {
            logger.error("获取模板信息失败，ID: {}", id, e);
            return null;
        }
    }

    /**
     * 获取SessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("数据库连接已关闭");
        }
    }
}
