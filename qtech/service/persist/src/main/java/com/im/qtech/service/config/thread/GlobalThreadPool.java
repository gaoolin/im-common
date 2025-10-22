package com.im.qtech.service.config.thread;

import com.im.qtech.service.config.thread.config.ThreadPoolProperties;
import com.im.qtech.service.config.thread.policy.ImportantTaskRejectedPolicy;
import com.im.qtech.service.config.thread.policy.LowPriorityTaskRejectedPolicy;
import com.im.qtech.service.config.thread.policy.NormalTaskRejectedPolicy;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局线程池配置 - 支持Java 21虚拟线程
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class GlobalThreadPool {

    private static final Logger logger = LoggerFactory.getLogger(GlobalThreadPool.class);

    private final ThreadPoolProperties properties;

    public GlobalThreadPool(@Qualifier("threadPoolProperties") ThreadPoolProperties properties) {
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
        // 创建带并发控制的虚拟线程执行器
        return new TaskExecutor() {
            private final Semaphore semaphore = new Semaphore(properties.getVirtual().getPermits());

            @Override
            public void execute(@NonNull Runnable task) {
                if (semaphore.tryAcquire()) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            task.run();
                        } finally {
                            semaphore.release();
                        }
                    });
                } else {
                    // 虚拟线程池满时的处理策略
                    // 可以根据实际需求选择丢弃或回退到其他执行器
                    logger.warn(">>>>> Virtual thread limit reached, falling back to low executor");
                    // normalTaskExecutor().execute(task);
                    importantTaskExecutor().execute(task);
                }
            }
        };
    }

    //  buildExecutor 方法确实没有处理虚拟线程的情况。这个方法是专门为传统的 ThreadPoolTaskExecutor 设计的，而虚拟线程执行器是在 virtualThreadTaskExecutor 方法中单独创建的
    private TaskExecutor buildExecutor(ThreadPoolProperties.PoolConfig config, String type) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix() + "-" + type + "-");

        // 根据不同类型设置不同的拒绝策略
        switch (type) {
            case "important":
                executor.setRejectedExecutionHandler(new ImportantTaskRejectedPolicy());
                break;
            case "normal":
                executor.setRejectedExecutionHandler(new NormalTaskRejectedPolicy());
                break;
            case "low":
                executor.setRejectedExecutionHandler(new LowPriorityTaskRejectedPolicy());
                break;
            default:
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                break;
        }

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
