package com.im.qtech.service.config.thread;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired(required = false)
    @Qualifier("virtualThreadTaskExecutor")
    private Executor virtualExecutor;

    public void dispatch(Runnable task, TaskPriority priority) {
        getExecutor(priority).execute(task);
    }

    public Executor getExecutor(TaskPriority priority) {
        // 检查虚拟线程执行器是否可用，不可用时回退到普通执行器
        return switch (priority) {
            case IMPORTANT -> importantExecutor;
            case LOW -> lowExecutor;
            case VIRTUAL -> {
                if (virtualExecutor != null) {
                    yield virtualExecutor;
                }
                yield normalExecutor;
            }
            default -> normalExecutor;
        };
    }

    public enum TaskPriority {
        IMPORTANT,
        NORMAL,
        LOW,
        VIRTUAL
    }
}
