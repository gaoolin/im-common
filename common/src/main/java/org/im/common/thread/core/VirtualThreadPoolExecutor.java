package org.im.common.thread.core;

import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 虚拟线程执行器 - 支持Java 21虚拟线程
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class VirtualThreadPoolExecutor implements SmartThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadPoolExecutor.class);
    // Java 21虚拟线程支持检测
    private static final boolean VIRTUAL_THREADS_SUPPORTED;
    private static Method ofVirtualMethod;
    private static Method startMethod;

    static {
        boolean supported = false;
        try {
            Class<?> builderClass = Class.forName("java.lang.Thread$Builder");
            ofVirtualMethod = Thread.class.getMethod("ofVirtual");
            startMethod = builderClass.getMethod("start", Runnable.class);
            supported = true;
        } catch (Exception e) {
            logger.info("Virtual threads not supported in current Java version");
        }
        VIRTUAL_THREADS_SUPPORTED = supported;
    }

    private final String name;
    private final ThreadPoolFrameworkProperties properties;
    private final Semaphore concurrencyLimiter;
    private final AtomicInteger activeCount = new AtomicInteger(0);

    public VirtualThreadPoolExecutor(String name, ThreadPoolFrameworkProperties properties) {
        this.name = name;
        this.properties = properties;
        this.concurrencyLimiter = new Semaphore(properties.getVirtual().getMaxConcurrency());
    }

    @Override
    public void execute(Runnable task) {
        if (!concurrencyLimiter.tryAcquire()) {
            logger.warn("[{}] Virtual thread limit reached ({}), task rejected",
                    name, properties.getVirtual().getMaxConcurrency());
            return;
        }

        activeCount.incrementAndGet();

        Runnable wrappedTask = () -> {
            try {
                task.run();
            } finally {
                concurrencyLimiter.release();
                activeCount.decrementAndGet();
            }
        };

        if (VIRTUAL_THREADS_SUPPORTED && properties.isVirtualThreadEnabled()) {
            try {
                Object builder = ofVirtualMethod.invoke(null);
                startMethod.invoke(builder, wrappedTask);
            } catch (Exception e) {
                logger.error("[{}] Failed to create virtual thread", name, e);
                // 回退到平台线程
                createPlatformThread(wrappedTask).start();
            }
        } else {
            // Java 8兼容模式
            createPlatformThread(wrappedTask).start();
        }
    }

    private Thread createPlatformThread(Runnable task) {
        return new Thread(task, properties.getVirtual().getThreadNamePrefix() +
                name + "-" + System.currentTimeMillis());
    }

    @Override
    public void execute(Runnable task, TaskType taskType) {
        // 虚拟线程执行器对所有任务类型采用相同策略
        execute(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public <T> CompletableFuture<T> submitAsync(Supplier<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            try {
                T result = task.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public ThreadPoolStatus getStatus() {
        ThreadPoolStatus status = new ThreadPoolStatus();
        status.setName(name);
        status.setActiveCount(activeCount.get());
        status.setRemainingCapacity(concurrencyLimiter.availablePermits());
        return status;
    }

    @Override
    public void shutdown() {
        logger.info("[{}] Virtual thread executor shutdown requested", name);
    }
}