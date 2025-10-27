package org.im.common.thread.core;

import org.im.common.thread.task.TaskType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * 智能线程池执行器接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */
public interface SmartThreadPoolExecutor extends Executor {

    /**
     * 执行任务
     */
    void execute(Runnable task);

    /**
     * 按任务类型执行
     */
    void execute(Runnable task, TaskType taskType);

    /**
     * 提交带返回值的任务
     */
    <T> Future<T> submit(java.util.concurrent.Callable<T> task);

    /**
     * 异步提交任务
     */
    <T> CompletableFuture<T> submitAsync(Supplier<T> task);

    /**
     * 获取线程池状态
     */
    ThreadPoolStatus getStatus();

    /**
     * 关闭线程池
     */
    void shutdown();
}