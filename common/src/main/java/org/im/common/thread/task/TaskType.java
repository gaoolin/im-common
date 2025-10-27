package org.im.common.thread.task;

/**
 * 智能制造任务类型枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */
public enum TaskType {
    // 实时控制类 (最高优先级)
    DEVICE_CONTROL(1, "设备控制"),
    SENSOR_MONITORING(2, "传感器监控"),

    // 数据处理类 (高优先级)
    DATA_PROCESSING(3, "数据处理"),
    REALTIME_ANALYTICS(4, "实时分析"),

    // 批处理类 (普通优先级)
    BATCH_PROCESSING(5, "批处理"),
    REPORT_GENERATION(6, "报表生成"),

    // 系统维护类 (低优先级)
    SYSTEM_MAINTENANCE(7, "系统维护"),
    LOG_CLEANUP(8, "日志清理");

    private final int priority;
    private final String description;

    TaskType(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }
}