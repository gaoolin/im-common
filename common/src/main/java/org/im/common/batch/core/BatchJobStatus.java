package org.im.common.batch.core;

/**
 * 批处理作业状态枚举
 * <p>
 * 定义批处理作业的生命周期状态
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public enum BatchJobStatus {

    /**
     * 作业已创建但未提交
     */
    CREATED("CREATED", "作业已创建"),

    /**
     * 作业已提交等待执行
     */
    SUBMITTED("SUBMITTED", "作业已提交"),

    /**
     * 作业正在运行
     */
    RUNNING("RUNNING", "作业运行中"),

    /**
     * 作业已暂停
     */
    PAUSED("PAUSED", "作业已暂停"),

    /**
     * 作业执行成功
     */
    SUCCESS("SUCCESS", "作业执行成功"),

    /**
     * 作业执行失败
     */
    FAILED("FAILED", "作业执行失败"),

    /**
     * 作业已被取消
     */
    CANCELLED("CANCELLED", "作业已取消");

    private final String code;
    private final String description;

    BatchJobStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}

