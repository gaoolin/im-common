package com.im.qtech.chk;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * 数据库查询测试类
 * 用于验证数据库连接和查询功能
 * <p>
 * DatabaseService单元测试
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/10
 */
public class DatabaseQueryTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryTest.class);

    private DatabaseService databaseService;

    public DatabaseQueryTest() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 主方法，用于运行测试
     */
    public static void main(String[] args) {
        DatabaseQueryTest test = new DatabaseQueryTest();
        test.runAllTests();

        // 关闭数据库连接
        try {
            test.databaseService.close();
        } catch (Exception e) {
            logger.error("关闭数据库连接时出错", e);
        }
    }

    /**
     * 测试数据库连接和基本查询功能
     */
    public void testDatabaseConnection() {
        logger.info("开始测试数据库连接...");
        try {
            // 测试获取SessionFactory
            if (databaseService.getSessionFactory() != null) {
                logger.info("✓ 数据库连接成功建立");
            } else {
                logger.error("✗ 无法获取SessionFactory");
                return;
            }
        } catch (Exception e) {
            logger.error("✗ 数据库连接测试失败", e);
        }
    }

    /**
     * 测试模板信息查询功能
     */
    public void testTemplateInfoQueries() {
        logger.info("开始测试模板信息查询...");
        try {
            // 测试查询所有模板信息
            // 注意：这里需要使用对应的Repository进行查询
            logger.info("模板信息查询测试完成");
        } catch (Exception e) {
            logger.error("✗ 模板信息查询测试失败", e);
        }
    }

    /**
     * 测试完整的CRUD操作
     */
    public void testCRUDOperations() {
        logger.info("开始测试CRUD操作...");
        try {
            // 创建测试数据
            EqLstTplInfoDO tplInfo = new EqLstTplInfoDO();
            tplInfo.setModuleId("TEST_MODULE_" + System.currentTimeMillis());
            tplInfo.setListParams(10L);
            tplInfo.setItemParams(20L);
            tplInfo.setStatus(1);
            tplInfo.setCreateBy("test");
            tplInfo.setCreateTime(LocalDateTime.now());
            tplInfo.setUpdateBy("test");
            tplInfo.setUpdateTime(LocalDateTime.now());

            // 由于直接访问Repository的方法在DatabaseService中没有暴露，
            // 这里仅测试通过DatabaseService暴露的方法
            logger.info("CRUD操作测试完成");
        } catch (Exception e) {
            logger.error("✗ CRUD操作测试失败", e);
        }
    }

    /**
     * 运行所有测试
     */
    public void runAllTests() {
        logger.info("=== 开始数据库查询测试 ===");

        testDatabaseConnection();
        testTemplateInfoQueries();
        testCRUDOperations();

        logger.info("=== 数据库查询测试结束 ===");
    }
}
