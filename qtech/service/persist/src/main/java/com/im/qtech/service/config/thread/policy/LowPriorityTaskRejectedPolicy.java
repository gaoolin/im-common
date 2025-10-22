package com.im.qtech.service.config.thread.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 低优先级任务拒绝策略 - 直接丢弃任务并记录日志
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/23
 */

public class LowPriorityTaskRejectedPolicy implements RejectedExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(LowPriorityTaskRejectedPolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn(">>>>> Low priority task rejected and discarded. Queue size: {}",
                executor.getQueue().size());
        // 直接丢弃任务，仅记录日志
    }
}