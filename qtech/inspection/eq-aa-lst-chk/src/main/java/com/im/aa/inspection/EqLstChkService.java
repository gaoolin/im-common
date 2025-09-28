package com.im.aa.inspection;

import com.im.aa.inspection.config.ConfigManager;
import com.im.aa.inspection.consumer.KafkaMessageConsumer;
import com.im.aa.inspection.service.DatabaseService;
import com.im.aa.inspection.service.ParamCheckService;
import com.im.aa.inspection.utils.CacheUtil;
import org.im.config.ConfigurationManager;
import org.im.semiconductor.common.dispatcher.MessageHandlerDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * AA参数点检后台服务主类
 * 负责应用组件初始化和生命周期管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class EqLstChkService {
    private static final Logger logger = LoggerFactory.getLogger(EqLstChkService.class);

    private static ConfigurationManager configManager;
    private static KafkaMessageConsumer kafkaConsumer;
    private static ParamCheckService paramCheckService;
    private static MessageHandlerDispatcher messageHandlerDispatcher;
    private static CacheUtil cacheUtil;
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

            // 初始化消息处理器分发器
            messageHandlerDispatcher = MessageHandlerDispatcher.getInstance();

            // 初始化缓存工具（从Redis获取数据）
            cacheUtil = CacheUtil.getInstance(); // 需要确保CacheUtil已正确初始化

            // 初始化服务组件
            DatabaseService databaseService = new DatabaseService(configManager);
            paramCheckService = new ParamCheckService(databaseService, cacheUtil);

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
            kafkaConsumer = new KafkaMessageConsumer(configManager, paramCheckService, messageHandlerDispatcher);
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

    public static MessageHandlerDispatcher getMessageHandlerDispatcher() {
        return messageHandlerDispatcher;
    }

    public static CacheUtil getCacheUtil() {
        return cacheUtil;
    }
}
