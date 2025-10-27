package org.im.common.thread.core;

import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.factory.ThreadPoolFactory;
import org.im.common.thread.task.HybridTaskDispatcher;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class ThreadPoolSingleton {
    private static volatile ThreadPoolSingleton instance;
    private final ThreadPoolManager threadPoolManager;
    private final HybridTaskDispatcher taskDispatcher;

    private ThreadPoolSingleton() {
        ThreadPoolFrameworkProperties properties = new ThreadPoolFrameworkProperties();
        this.threadPoolManager = new ThreadPoolManager(properties);
        ThreadPoolFactory factory = new ThreadPoolFactory(properties);
        this.taskDispatcher = new HybridTaskDispatcher(factory, properties);
    }

    public static ThreadPoolSingleton getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolSingleton.class) {
                if (instance == null) {
                    instance = new ThreadPoolSingleton();
                }
            }
        }
        return instance;
    }

    public ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public HybridTaskDispatcher getTaskDispatcher() {
        return taskDispatcher;
    }
}