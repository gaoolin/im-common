package com.im.aa.inspection.service;

import com.im.aa.inspection.config.AppConfig;
import com.im.aa.inspection.entity.reverse.EqpReverseDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.repository.EqLstTplInfoRepository;
import com.im.aa.inspection.repository.ReverseDataRepository;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库服务（纯 Java + Hibernate SE）(单例模式)
 */
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    /**
     * 静态内部类实现单例模式
     * 利用JVM类加载机制保证线程安全和延迟加载
     */
    private static class SingletonHolder {
        private static final DatabaseService INSTANCE = new DatabaseService();
    }

    /**
     * 获取DatabaseService单例实例
     *
     * @return DatabaseService单例实例
     */
    public static DatabaseService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Getter
    private SessionFactory sessionFactory;
    private EqLstTplInfoRepository templateInfoRepository;
    private ReverseDataRepository reverseDataRepository;

    /**
     * 私有构造函数：防止外部直接实例化
     */
    private DatabaseService() {
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        try {
            // 使用 AppConfig 初始化配置
            AppConfig appConfig = new AppConfig();
            sessionFactory = appConfig.initializeSessionFactory();

            // 初始化 Repository
            templateInfoRepository = new EqLstTplInfoRepository(sessionFactory);
            reverseDataRepository = new ReverseDataRepository(sessionFactory);

            logger.info(">>>>> 数据库服务初始化成功");
        } catch (Exception e) {
            logger.error(">>>>> 数据库服务初始化失败", e);
        }
    }

    // =======================
    // 公共接口
    // =======================

    public EqLstTplInfoDO getTplInfo(String module) {
        try {
            return templateInfoRepository.findByModuleId(module);
        } catch (Exception e) {
            logger.error(">>>>> 获取参数标准值失败: {}", module, e);
            return null;
        }
    }

    public void saveInspectionResult(EqpReverseDO result) {
        try {
            reverseDataRepository.saveEqpReverseRecord(result);
        } catch (Exception e) {
            logger.error(">>>>> 保存检查结果失败: {}", result, e);
        }
    }

    public EqLstTplInfoDO getTemplateInfoById(Long id) {
        try {
            return templateInfoRepository.findById(id);
        } catch (Exception e) {
            logger.error(">>>>> 获取模板信息失败，ID: {}", id, e);
            return null;
        }
    }

    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info(">>>>> 数据库连接已关闭");
        }
    }
}
