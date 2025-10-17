package org.im.common.batch.example;

import org.im.common.batch.config.BatchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

/**
 * 配置管理示例
 * 演示不同的配置方式和最佳实践
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 */
public class ConfigurationExample {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationExample.class);

    public static void main(String[] args) {
        // 方式1: 使用默认构造函数
        BatchConfig config1 = new BatchConfig();
        logger.info("Default config: batch size={}, retry count={}",
                config1.getBatchSize(), config1.getRetryCount());

        // 方式2: 使用setter方法配置
        BatchConfig config2 = new BatchConfig();
        config2.setBatchSize(2000);
        config2.setRetryCount(5);
        config2.setRetryDelayMs(2000);
        config2.setContinueOnError(true);
        config2.setTimeout(Duration.ofMinutes(10));
        logger.info("Custom config: batch size={}, retry count={}, timeout={}",
                config2.getBatchSize(), config2.getRetryCount(), config2.getTimeout());

        // 方式3: 使用构建器模式（推荐）
        BatchConfig config3 = BatchConfig.builder()
                .batchSize(1500)
                .concurrentThreads(6)
                .retryCount(3)
                .retryDelayMs(1500)
                .continueOnError(false)
                .timeout(Duration.ofMinutes(5))
                .property("custom.property", "customValue")
                .build();

        logger.info("Builder config: batch size={}, threads={}, retry count={}",
                config3.getBatchSize(), config3.getConcurrentThreads(), config3.getRetryCount());
        logger.info("Custom property: {}", Optional.ofNullable(config3.getProperty("custom.property")));

        // 方式4: 基于现有配置创建新配置
        BatchConfig config4 = new BatchConfig();
        config4.setBatchSize(3000);
        config4.setRetryCount(2);

        BatchConfig derivedConfig = BatchConfig.builder()
                .batchSize(config4.getBatchSize())
                .retryCount(config4.getRetryCount())
                .retryDelayMs(3000)
                .continueOnError(true)
                .build();

        logger.info("Derived config: batch size={}, retry count={}, delay={}",
                derivedConfig.getBatchSize(), derivedConfig.getRetryCount(), derivedConfig.getRetryDelayMs());
    }
}
