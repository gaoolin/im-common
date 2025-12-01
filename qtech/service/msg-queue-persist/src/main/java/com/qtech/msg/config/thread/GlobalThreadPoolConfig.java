package com.qtech.msg.config.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局线程池注册器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/25 10:06:35
 */

@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class GlobalThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(GlobalThreadPoolConfig.class);

    private final ThreadPoolProperties properties;

    public GlobalThreadPoolConfig(@Qualifier("thread-pool-com.qtech.msg.config.thread.ThreadPoolProperties") ThreadPoolProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "importantTaskExecutor")
    public ThreadPoolTaskExecutor importantTaskExecutor() {
        return buildExecutor(properties.getImportant());
    }

    @Bean(name = "normalTaskExecutor")
    public ThreadPoolTaskExecutor normalTaskExecutor() {
        return buildExecutor(properties.getNormal());
    }

    @Bean(name = "lowPriorityTaskExecutor")
    public ThreadPoolTaskExecutor lowPriorityTaskExecutor() {
        return buildExecutor(properties.getLow());
    }

    private ThreadPoolTaskExecutor buildExecutor(ThreadPoolProperties.PoolConfig config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @PostConstruct
    public void logThreadPoolConfigurations() {
        logger.info(">>>>> importantTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}", properties.getImportant().getCorePoolSize(), properties.getImportant().getMaxPoolSize(), properties.getImportant().getQueueCapacity());
        logger.info(">>>>> normalTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}", properties.getNormal().getCorePoolSize(), properties.getNormal().getMaxPoolSize(), properties.getNormal().getQueueCapacity());
        logger.info(">>>>> lowPriorityTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}", properties.getLow().getCorePoolSize(), properties.getLow().getMaxPoolSize(), properties.getLow().getQueueCapacity());
    }
}