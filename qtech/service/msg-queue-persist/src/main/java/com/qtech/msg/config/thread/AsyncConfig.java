package com.qtech.msg.config.thread;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池配置，已废弃
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/25 15:16:25
 */

// @Configuration
// @EnableAsync
public class AsyncConfig {

    // @Bean
    public TaskExecutor imMqExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 核心线程数
        executor.setMaxPoolSize(10); // 最大线程数
        executor.initialize();
        return executor;
    }
}