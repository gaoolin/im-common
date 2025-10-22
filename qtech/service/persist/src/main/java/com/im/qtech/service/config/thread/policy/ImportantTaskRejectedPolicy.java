package com.im.qtech.service.config.thread.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 重要任务拒绝策略 - 阻塞调用线程直到任务被接受
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/23
 */

public class ImportantTaskRejectedPolicy implements RejectedExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ImportantTaskRejectedPolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn(">>>>> Important task rejected, will block until accepted. Queue size: {}",
                executor.getQueue().size());
        // 阻塞直到任务被接受
        try {
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(">>>>> Thread interrupted while waiting to put task into queue", e);
        }
    }
}
