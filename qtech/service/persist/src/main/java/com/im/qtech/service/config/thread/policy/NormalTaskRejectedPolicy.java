package com.im.qtech.service.config.thread.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 正常任务拒绝策略 - 调用者运行策略
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/23
 */

public class NormalTaskRejectedPolicy implements RejectedExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(NormalTaskRejectedPolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn(">>>>> Normal task rejected, executing in caller thread. Queue size: {}",
                executor.getQueue().size());
        // 如果执行器未关闭，则在调用者线程中执行任务
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}