package com.im.aa.inspection.service;

import com.im.aa.inspection.config.AppConfig;
import com.im.aa.inspection.entity.reverse.EqpReverseRecord;
import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.repository.EqLstTplInfoRepository;
import com.im.aa.inspection.repository.ReverseDataRepository;
import com.im.aa.inspection.repository.TemplateDataRepository;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库服务（纯 Java + Hibernate SE）
 */
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Getter
    private SessionFactory sessionFactory;
    private EqLstTplInfoRepository templateInfoRepository;
    private TemplateDataRepository templateDataRepository;
    private ReverseDataRepository reverseDataRepository;

    /**
     * 构造函数：初始化数据库服务
     */
    public DatabaseService() {
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
            templateDataRepository = new TemplateDataRepository(sessionFactory);
            reverseDataRepository = new ReverseDataRepository(sessionFactory);

            logger.info("数据库服务初始化成功");
        } catch (Exception e) {
            logger.error("数据库服务初始化失败", e);
            throw new RuntimeException("数据库服务初始化失败", e);
        }
    }

    // =======================
    // 公共接口
    // =======================

    public EqLstTplInfoDO getTplInfo(String module) {
        try {
            return templateInfoRepository.findByModule(module);
        } catch (Exception e) {
            logger.error("获取参数标准值失败: {}", module, e);
            return null;
        }
    }

    public EqLstTplDO getTpl(String module) {
        try {
            return templateDataRepository.findByModule(module);
        } catch (Exception e) {
            logger.error("获取检查结果模板失败: {}", module, e);
            return new EqLstTplDO();
        }
    }

    public void saveInspectionResult(EqpReverseRecord result) {
        try {
            reverseDataRepository.saveEqpReverseRecord(result);
        } catch (Exception e) {
            logger.error("保存检查结果失败: {}", result, e);
            throw new RuntimeException("保存检查结果失败", e);
        }
    }

    public EqLstTplInfoDO getTemplateInfoById(Long id) {
        try {
            return templateInfoRepository.findById(id);
        } catch (Exception e) {
            logger.error("获取模板信息失败，ID: {}", id, e);
            return null;
        }
    }

    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("数据库连接已关闭");
        }
    }
}
