package org.im.common.thread.core;

import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.policy.SmartRejectionHandler;
import org.im.common.thread.task.TaskPriority;
import org.im.common.thread.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 原生Java线程池执行器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class NativeThreadPoolExecutor implements SmartThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(NativeThreadPoolExecutor.class);

    private final String name;
    private final TaskPriority priority;
    private final ThreadPoolFrameworkProperties properties;
    private final ThreadPoolExecutor executor;
    private final String threadNamePrefix;

    public NativeThreadPoolExecutor(String name, TaskPriority priority,
                                    ThreadPoolFrameworkProperties properties) {
        this.name = name;
        this.priority = priority;
        this.properties = properties;
        this.executor = createExecutor();
        this.threadNamePrefix = getThreadNamePrefix();
    }

    private ThreadPoolExecutor createExecutor() {
        // 根据优先级获取配置
        ThreadPoolFrameworkProperties.PoolConfig config = getPoolConfig();

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, threadNamePrefix + name + "-" + threadNumber.getAndIncrement());
                        if (thread.isDaemon()) {
                            thread.setDaemon(false);
                        }
                        if (thread.getPriority() != Thread.NORM_PRIORITY) {
                            thread.setPriority(Thread.NORM_PRIORITY);
                        }
                        return thread;
                    }
                },
                new SmartRejectionHandler(name, priority)
        );

        return executor;
    }

    private ThreadPoolFrameworkProperties.PoolConfig getPoolConfig() {
        switch (priority) {
            case IMPORTANT:
                return properties.getImportant();
            case NORMAL:
                return properties.getNormal();
            case LOW:
                return properties.getLow();
            default:
                return properties.getNormal();
        }
    }

    private String getThreadNamePrefix() {
        switch (priority) {
            case IMPORTANT:
                return properties.getImportant().getThreadNamePrefix();
            case NORMAL:
                return properties.getNormal().getThreadNamePrefix();
            case LOW:
                return properties.getLow().getThreadNamePrefix();
            default:
                return "thread-";
        }
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void execute(Runnable task, TaskType taskType) {
        // 根据任务类型采用不同的执行策略
        switch (taskType) {
            case DEVICE_CONTROL:
            case SENSOR_MONITORING:
                // 高优先级实时任务，确保执行
                executeWithRetry(task, 3);
                break;
            case BATCH_PROCESSING:
            case REPORT_GENERATION:
                // 批处理任务，可适当延迟
                if (shouldUseVirtualThread()) {
                    executeInVirtualThread(task);
                } else {
                    executor.execute(task);
                }
                break;
            default:
                executor.execute(task);
        }
    }

    private void executeWithRetry(Runnable task, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                executor.execute(task);
                return;
            } catch (RejectedExecutionException e) {
                if (i == maxRetries - 1) {
                    logger.error("[{}] Task rejected after {} retries", name, maxRetries, e);
                    throw e;
                }
                try {
                    Thread.sleep(100 * (i + 1)); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }

    private boolean shouldUseVirtualThread() {
        return properties.isVirtualThreadEnabled() &&
                "BATCH_PROCESSING".equals(Thread.currentThread().getName());
    }

    private void executeInVirtualThread(Runnable task) {
        try {
            Object builder = Thread.class.getMethod("ofVirtual").invoke(null);
            builder.getClass().getMethod("start", Runnable.class).invoke(builder, task);
        } catch (Exception e) {
            // 回退到普通线程池
            executor.execute(task);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
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
        status.setCorePoolSize(executor.getCorePoolSize());
        status.setMaximumPoolSize(executor.getMaximumPoolSize());
        status.setActiveCount(executor.getActiveCount());
        status.setPoolSize(executor.getPoolSize());
        status.setCompletedTaskCount(executor.getCompletedTaskCount());
        status.setTaskCount(executor.getTaskCount());
        status.setQueueSize(executor.getQueue().size());
        status.setRemainingCapacity(executor.getQueue().remainingCapacity());
        status.setShutdown(executor.isShutdown());
        status.setTerminated(executor.isTerminated());
        return status;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
