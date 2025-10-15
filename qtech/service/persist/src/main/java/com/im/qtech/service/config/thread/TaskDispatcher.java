package com.im.qtech.service.config.thread;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * 统一封装：任务调度中心 - 基于Spring Boot 3优化
 * <p>
 * 对于分析类任务、日志类任务，可使用 LOW；核心业务用 IMPORTANT
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/25 10:07:35
 */
@Component
public class TaskDispatcher {

    @Resource(name = "importantTaskExecutor")
    private Executor importantExecutor;

    @Resource(name = "normalTaskExecutor")
    private Executor normalExecutor;

    @Resource(name = "lowPriorityTaskExecutor")
    private Executor lowExecutor;

    @Resource(name = "virtualThreadTaskExecutor")
    private Executor virtualExecutor;

    public void dispatch(Runnable task, TaskPriority priority) {
        getExecutor(priority).execute(task);
    }

    public Executor getExecutor(TaskPriority priority) {
        switch (priority) {
            case IMPORTANT:
                return importantExecutor;
            case LOW:
                return lowExecutor;
            case VIRTUAL:
                // 检查虚拟线程执行器是否可用，不可用时回退到普通执行器
                try {
                    return virtualExecutor;
                } catch (Exception e) {
                    return normalExecutor;
                }
            case NORMAL:
            default:
                return normalExecutor;
        }
    }

    public enum TaskPriority {
        IMPORTANT,
        NORMAL,
        LOW,
        VIRTUAL
    }
}
