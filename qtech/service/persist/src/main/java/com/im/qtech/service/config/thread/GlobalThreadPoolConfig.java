package com.im.qtech.service.config.thread;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局线程池配置 - 支持Java 21虚拟线程
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class GlobalThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(GlobalThreadPoolConfig.class);

    private final ThreadPoolProperties properties;

    public GlobalThreadPoolConfig(@Qualifier("threadPoolProperties") ThreadPoolProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "importantTaskExecutor")
    public TaskExecutor importantTaskExecutor() {
        return buildExecutor(properties.getImportant(), "important");
    }

    @Bean(name = "normalTaskExecutor")
    public TaskExecutor normalTaskExecutor() {
        return buildExecutor(properties.getNormal(), "normal");
    }

    @Bean(name = "lowPriorityTaskExecutor")
    public TaskExecutor lowPriorityTaskExecutor() {
        return buildExecutor(properties.getLow(), "low");
    }

    /**
     * 全局虚拟线程执行器 - 确保Spring Boot 3.3依赖的Bean存在
     */
    @Bean(name = "virtualThreadTaskExecutor")
    @ConditionalOnProperty(name = "thread-pool.virtual.enabled", havingValue = "true", matchIfMissing = true)
    public TaskExecutor virtualThreadTaskExecutor() {
        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        return new TaskExecutorAdapter(virtualThreadExecutor);
    }

    private TaskExecutor buildExecutor(ThreadPoolProperties.PoolConfig config, String type) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix() + "-" + type + "-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @PostConstruct
    public void logThreadPoolConfigurations() {
        logger.info(">>>>> importantTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}",
                properties.getImportant().getCorePoolSize(),
                properties.getImportant().getMaxPoolSize(),
                properties.getImportant().getQueueCapacity());
        logger.info(">>>>> normalTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}",
                properties.getNormal().getCorePoolSize(),
                properties.getNormal().getMaxPoolSize(),
                properties.getNormal().getQueueCapacity());
        logger.info(">>>>> lowPriorityTaskExecutor CorePoolSize: {}, MaxPoolSize: {}, QueueCapacity: {}",
                properties.getLow().getCorePoolSize(),
                properties.getLow().getMaxPoolSize(),
                properties.getLow().getQueueCapacity());
        logger.info(">>>>> virtualThreadTaskExecutor enabled with Java 21 Virtual Threads");
    }
}
