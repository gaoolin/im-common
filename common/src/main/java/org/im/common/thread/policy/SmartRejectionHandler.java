package org.im.common.thread.policy;

import org.im.common.thread.task.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 智能拒绝处理器 - 根据优先级采用不同策略
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class SmartRejectionHandler implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SmartRejectionHandler.class);

    private final String poolName;
    private final TaskPriority priority;

    public SmartRejectionHandler(String poolName, TaskPriority priority) {
        this.poolName = poolName;
        this.priority = priority;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        switch (priority) {
            case IMPORTANT:
                handleImportantTaskRejection(r, executor);
                break;
            case NORMAL:
                handleNormalTaskRejection(r, executor);
                break;
            case LOW:
                handleLowTaskRejection(r, executor);
                break;
        }
    }

    private void handleImportantTaskRejection(Runnable r, ThreadPoolExecutor executor) {
        logger.warn("[{}] Important task rejected, will block until accepted. Queue size: {}",
                poolName, executor.getQueue().size());
        // 重要任务阻塞等待
        try {
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[{}] Thread interrupted while waiting to put important task", poolName, e);
        }
    }

    private void handleNormalTaskRejection(Runnable r, ThreadPoolExecutor executor) {
        logger.warn("[{}] Normal task rejected, executing in caller thread. Queue size: {}",
                poolName, executor.getQueue().size());
        // 普通任务在调用者线程执行
        if (!executor.isShutdown()) {
            r.run();
        }
    }

    private void handleLowTaskRejection(Runnable r, ThreadPoolExecutor executor) {
        logger.warn("[{}] Low priority task rejected and discarded. Queue size: {}",
                poolName, executor.getQueue().size());
        // 低优先级任务直接丢弃
    }
}