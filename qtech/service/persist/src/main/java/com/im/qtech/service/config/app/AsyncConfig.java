package com.im.qtech.service.config.app;

import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.im.common.thread.core.ThreadPoolSingleton;
import org.im.common.thread.task.HybridTaskDispatcher;
import org.im.common.thread.task.TaskPriority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/15
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public SmartThreadPoolExecutor taskExecutor() {
        // 使用您的线程池框架创建执行器
        HybridTaskDispatcher dispatcher = ThreadPoolSingleton.getInstance().getTaskDispatcher();
        // 获取普通优先级的执行器作为默认异步执行器
        return dispatcher.getExecutor(TaskPriority.NORMAL);
    }

    @Bean("importantTaskExecutor")
    public SmartThreadPoolExecutor importantTaskExecutor() {
        // 提供重要任务执行器
        HybridTaskDispatcher dispatcher = ThreadPoolSingleton.getInstance().getTaskDispatcher();
        return dispatcher.getExecutor(TaskPriority.IMPORTANT);
    }
}
