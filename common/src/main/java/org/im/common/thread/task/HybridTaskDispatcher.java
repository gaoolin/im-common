package org.im.common.thread.task;


import org.im.common.thread.config.ThreadPoolFrameworkProperties;
import org.im.common.thread.core.NativeThreadPoolExecutor;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.im.common.thread.core.VirtualThreadPoolExecutor;
import org.im.common.thread.factory.ThreadPoolFactory;

/**
 * 混合模式任务调度器 - 结合重要性分类和任务类型
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class HybridTaskDispatcher {

    private final NativeThreadPoolExecutor importantExecutor;
    private final NativeThreadPoolExecutor normalExecutor;
    private final NativeThreadPoolExecutor lowExecutor;
    private final VirtualThreadPoolExecutor virtualExecutor;

    public HybridTaskDispatcher(ThreadPoolFactory factory, ThreadPoolFrameworkProperties properties) {
        this.importantExecutor = factory.createImportantExecutor();
        this.normalExecutor = factory.createNormalExecutor();
        this.lowExecutor = factory.createLowExecutor();
        this.virtualExecutor = properties.isVirtualThreadEnabled() ?
                new VirtualThreadPoolExecutor("virtual", properties) : null;
    }

    /**
     * 按优先级调度任务
     */
    public void dispatch(Runnable task, TaskPriority priority) {
        if (priority == TaskPriority.VIRTUAL && virtualExecutor != null) {
            virtualExecutor.execute(task);
        } else {
            getExecutor(priority).execute(task);
        }
    }

    /**
     * 按优先级和任务类型调度任务
     */
    public void dispatch(Runnable task, TaskPriority priority, TaskType taskType) {
        if (priority == TaskPriority.VIRTUAL && virtualExecutor != null) {
            virtualExecutor.execute(task, taskType);
        } else {
            getExecutor(priority).execute(task, taskType);
        }
    }

    /**
     * 获取指定优先级的执行器
     */
    public SmartThreadPoolExecutor getExecutor(TaskPriority priority) {
        switch (priority) {
            case IMPORTANT:
                return importantExecutor;
            case LOW:
                return lowExecutor;
            case VIRTUAL:
                if (virtualExecutor != null) {
                    return virtualExecutor;
                }
                // 虚拟线程不可用时回退到普通执行器
                return normalExecutor;
            default:
                return normalExecutor;
        }
    }

    /**
     * 关闭所有执行器
     */
    public void shutdown() {
        importantExecutor.shutdown();
        normalExecutor.shutdown();
        lowExecutor.shutdown();
        if (virtualExecutor != null) {
            virtualExecutor.shutdown();
        }
    }
}