package org.im.common.thread.core;

import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.task.TaskPriority;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 线程池管理器
 * <p>
 * 功能重叠：
 * ThreadPoolManager 提供了线程池的统一管理功能
 * 与新设计的混合模式调度器功能有所重叠
 * 可以保留的原因：
 * 提供了线程池的缓存和复用机制
 * 支持按名称获取线程池实例
 * 统一的关闭管理
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class ThreadPoolManager {

    private final ThreadPoolFrameworkProperties properties;
    private final ConcurrentMap<String, SmartThreadPoolExecutor> executors =
            new ConcurrentHashMap<>();

    public ThreadPoolManager(ThreadPoolFrameworkProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取指定名称的线程池
     */
    public SmartThreadPoolExecutor getExecutor(String name) {
        return executors.computeIfAbsent(name, this::createExecutor);
    }

    /**
     * 获取默认线程池
     */
    public SmartThreadPoolExecutor getDefaultExecutor() {
        return getExecutor("default");
    }

    private SmartThreadPoolExecutor createExecutor(String name) {
        // 根据名称和优先级创建相应的执行器
        switch (name) {
            case "important":
                return new NativeThreadPoolExecutor(name, TaskPriority.IMPORTANT, properties);
            case "low":
                return new NativeThreadPoolExecutor(name, TaskPriority.LOW, properties);
            default:
                return new NativeThreadPoolExecutor(name, TaskPriority.NORMAL, properties);
        }
    }

    public void shutdown() {
        executors.values().forEach(SmartThreadPoolExecutor::shutdown);
        executors.clear();
    }
}