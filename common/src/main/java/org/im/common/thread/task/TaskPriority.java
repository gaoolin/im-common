package org.im.common.thread.task;

/**
 * 任务优先级枚举 - 保持原有的重要性分类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public enum TaskPriority {
    IMPORTANT,  // 重要任务
    NORMAL,     // 普通任务
    LOW,        // 低优先级任务
    VIRTUAL     // 虚拟线程任务
}