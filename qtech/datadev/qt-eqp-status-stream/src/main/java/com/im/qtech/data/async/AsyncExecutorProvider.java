package com.im.qtech.data.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/01 09:27:57
 * desc   :  双单例模式
 * 全局统一异步线程池管理器
 * 线程数可配置，默认单线程，轻量执行异步任务
 * 线程池生命周期可控，防止线程泄漏
 * 每种类型的线程池在整个应用中只有一个实例
 * 通过方法名区分不同用途的线程池
 * 保持了单例模式的所有优点：资源节省、延迟加载、线程安全
 */
public class AsyncExecutorProvider {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutorProvider.class);

    // 单例线程池
    private static volatile ExecutorService executor;
    // 为高并发场景提供专用线程池
    private static volatile ExecutorService highConcurrencyExecutor;

    private AsyncExecutorProvider() {
        // 私有构造
    }

    /**
     * 获取高并发线程池
     *
     * @return ExecutorService
     */
    public static ExecutorService getHighConcurrencyExecutor() {
        if (highConcurrencyExecutor == null) {
            synchronized (AsyncExecutorProvider.class) {
                if (highConcurrencyExecutor == null) {
                    highConcurrencyExecutor = createExecutor(HIGH_CONCURRENCY_THREAD_POOL_SIZE); // 更大线程池
                    logger.info("AsyncExecutorProvider initialized high concurrency thread pool with size 10");
                }
            }
        }
        return highConcurrencyExecutor;
    }

    /**
     * 获取线程池实例（懒加载）
     *
     * @return ExecutorService
     */
    public static ExecutorService getExecutor() {
        if (executor == null) {
            synchronized (AsyncExecutorProvider.class) {
                if (executor == null) {
                    executor = createExecutor(DEFAULT_THREAD_POOL_SIZE);
                    logger.info("AsyncExecutorProvider initialized thread pool with size {}", DEFAULT_THREAD_POOL_SIZE);
                }
            }
        }
        return executor;
    }

    /**
     * 获取指定大小的线程池
     *
     * @param threadCount 线程数
     * @return ExecutorService
     */
    public static ExecutorService getExecutor(int threadCount) {
        if (threadCount <= DEFAULT_THREAD_POOL_SIZE) {
            return getExecutor();
        }
        return createExecutor(threadCount);
    }

    /**
     * 按需创建线程池
     *
     * @param threadCount 线程数
     * @return ExecutorService
     */
    public static ExecutorService createExecutor(int threadCount) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000), runnable -> {
            Thread t = new Thread(runnable, "AsyncExecutorProvider-Thread");
            t.setDaemon(true);
            return t;
        }, new ThreadPoolExecutor.CallerRunsPolicy());

        // 添加监控
        logger.info("Created thread pool with core size: {}, max size: {}", executor.getCorePoolSize(), executor.getMaximumPoolSize());

        return executor;
    }


    /**
     * 关闭线程池
     */
    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            executor = null;
            logger.info("AsyncExecutorProvider thread pool shutdown");
        }
    }
}