package org.im.common.thread.factory;

import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.core.NativeThreadPoolExecutor;
import org.im.common.thread.task.TaskPriority;

/**
 * 线程池工厂类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class ThreadPoolFactory {

    private final ThreadPoolFrameworkProperties properties;

    public ThreadPoolFactory(ThreadPoolFrameworkProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建重要任务线程池
     */
    public NativeThreadPoolExecutor createImportantExecutor() {
        return new NativeThreadPoolExecutor("important", TaskPriority.IMPORTANT, properties);
    }

    /**
     * 创建普通任务线程池
     */
    public NativeThreadPoolExecutor createNormalExecutor() {
        return new NativeThreadPoolExecutor("normal", TaskPriority.NORMAL, properties);
    }

    /**
     * 创建低优先级任务线程池
     */
    public NativeThreadPoolExecutor createLowExecutor() {
        return new NativeThreadPoolExecutor("low", TaskPriority.LOW, properties);
    }
}