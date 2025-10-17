package org.im.common.batch.monitor;

import org.im.common.dt.Chronos;

import java.time.LocalDateTime;

/**
 * 作业执行指标
 * <p>
 * 记录作业执行过程中的各项指标数据
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public class JobMetrics {

    // 作业ID
    private final String jobId;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 执行时长（毫秒）
    private long durationMs;

    // 是否成功
    private boolean success = false;

    // 失败异常
    private Exception failureException;

    // 处理记录数
    private long processedRecords = 0;

    // 失败记录数
    private long failedRecords = 0;

    public JobMetrics(String jobId) {
        this.jobId = jobId;
    }

    /**
     * 标记作业开始
     */
    public void start() {
        this.startTime = Chronos.now();
    }

    /**
     * 标记作业结束
     */
    public void finish() {
        this.endTime = Chronos.now();
        this.durationMs = Chronos.toTimestamp(endTime) - Chronos.toTimestamp(startTime);
        this.success = true;
    }

    /**
     * 标记作业失败
     *
     * @param exception 失败异常
     */
    public void fail(Exception exception) {
        this.endTime = Chronos.now();
        this.durationMs = Chronos.toTimestamp(endTime) - Chronos.toTimestamp(startTime);
        this.success = false;
        this.failureException = exception;
    }

    // Getters
    public String getJobId() {
        return jobId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getFailureException() {
        return failureException;
    }

    public long getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(long processedRecords) {
        this.processedRecords = processedRecords;
    }

    public long getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(long failedRecords) {
        this.failedRecords = failedRecords;
    }

    @Override
    public String toString() {
        return "JobMetrics{" +
                "jobId='" + jobId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationMs=" + durationMs +
                ", success=" + success +
                ", processedRecords=" + processedRecords +
                ", failedRecords=" + failedRecords +
                '}';
    }
}
