package com.qtech.msg.config.thread;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * 统一封装：任务调度中心
 * 对于分析类任务、日志类任务，可使用 LOW；核心业务用 IMPORTANT
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

    public void dispatch(Runnable task, TaskPriority priority) {
        switch (priority) {
            case IMPORTANT:
                importantExecutor.execute(task);
                break;
            case NORMAL:
                normalExecutor.execute(task);
                break;
            case LOW:
                lowExecutor.execute(task);
                break;
            default:
                normalExecutor.execute(task);
        }
    }

    public Executor getExecutor(TaskPriority priority) {
        if (priority == TaskPriority.IMPORTANT) {
            return importantExecutor;
        } else if (priority == TaskPriority.LOW) {
            return lowExecutor;
        } else {
            return normalExecutor;
        }
    }

    public enum TaskPriority {
        IMPORTANT,
        NORMAL,
        LOW
    }
}
