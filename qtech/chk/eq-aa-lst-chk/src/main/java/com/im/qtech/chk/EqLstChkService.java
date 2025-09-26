package com.im.qtech.chk;

import com.im.qtech.chk.config.ConfigManager;
import com.im.qtech.chk.consumer.KafkaMessageConsumer;
import com.im.qtech.chk.service.CacheService;
import com.im.qtech.chk.service.DatabaseService;
import com.im.qtech.chk.service.ParamCheckService;
import org.im.cache.core.CacheManager;
import org.im.cache.impl.manager.DefaultCacheManager;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Web应用主类
 * 启动嵌入式Jetty服务器并初始化应用组件
 * <p>
 * AA参数点检后台服务主类
 * 专注于Kafka消息消费和数据处理
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class EqLstChkService {
    private static final Logger logger = LoggerFactory.getLogger(EqLstChkService.class);

    private static ConfigurationManager configManager;
    private static CacheManager cacheManager;
    private static KafkaMessageConsumer kafkaConsumer;
    private static ParamCheckService paramCheckService;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        logger.info("启动AA参数点检后台服务...");

        try {
            // 初始化核心组件
            initializeComponents();

            // 启动Kafka消费者
            startKafkaConsumer();

            // 注册关闭钩子
            registerShutdownHook();

            // 保持应用运行
            keepRunning();

        } catch (Exception e) {
            logger.error("服务启动失败", e);
            System.exit(1);
        }
    }

    /**
     * 初始化应用组件
     */
    private static void initializeComponents() {
        try {
            // 初始化配置管理器
            configManager = ConfigManager.initialize();
            configManager.setActiveProfile(System.getProperty("env", "dev"));

            // 初始化缓存管理器
            cacheManager = new DefaultCacheManager();

            // 初始化服务组件
            DatabaseService databaseService = new DatabaseService(configManager);
            CacheService cacheService = new CacheService(cacheManager);
            paramCheckService = new ParamCheckService(databaseService, cacheService);

            logger.info("应用组件初始化完成");
        } catch (Exception e) {
            logger.error("组件初始化失败", e);
            throw new RuntimeException("初始化失败", e);
        }
    }

    /**
     * 启动Kafka消费者
     */
    private static void startKafkaConsumer() {
        try {
            kafkaConsumer = new KafkaMessageConsumer(configManager, paramCheckService);
            kafkaConsumer.startConsuming();
            logger.info("Kafka消费者启动完成");
        } catch (Exception e) {
            logger.error("Kafka消费者启动失败", e);
            throw new RuntimeException("Kafka消费者启动失败", e);
        }
    }

    /**
     * 注册关闭钩子
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("正在关闭服务...");
            shutdown();
        }));
    }

    /**
     * 保持应用运行
     */
    private static void keepRunning() {
        logger.info("服务已启动，按Ctrl+C关闭服务");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 关闭服务
     */
    private static void shutdown() {
        running = false;

        try {
            if (kafkaConsumer != null) {
                kafkaConsumer.stopConsuming();
            }

            logger.info("服务已关闭");
        } catch (Exception e) {
            logger.error("关闭服务时出错", e);
        }
    }

    // Getter方法
    public static ConfigurationManager getConfigManager() {
        return configManager;
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }
}