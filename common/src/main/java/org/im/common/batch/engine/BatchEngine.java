package org.im.common.batch.engine;

import org.im.common.batch.core.BatchJob;
import org.im.common.batch.core.BatchJobStatus;
import org.im.common.batch.monitor.JobMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象批处理执行引擎
 * <p>
 * 定义批处理执行引擎的基础行为
 * </p>
 *
 * @param <T> 作业输入数据类型
 * @param <R> 作业输出结果类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public abstract class BatchEngine<T, R> implements BatchJob<T, R> {

    protected static final Logger logger = LoggerFactory.getLogger(BatchEngine.class);

    // 作业ID
    protected final String jobId;
    // 作业状态
    protected final AtomicReference<BatchJobStatus> status = new AtomicReference<>(BatchJobStatus.CREATED);
    // 作业指标
    protected final JobMetrics metrics;
    // 作业名称
    protected String jobName;
    // 作业描述
    protected String description;

    protected BatchEngine() {
        this.jobId = generateJobId();
        this.metrics = new JobMetrics(this.jobId);
    }

    protected BatchEngine(String jobName) {
        this();
        this.jobName = jobName;
    }

    protected BatchEngine(String jobName, String description) {
        this(jobName);
        this.description = description;
    }

    /**
     * 生成作业ID
     *
     * @return 作业ID
     */
    protected String generateJobId() {
        return "job-" + UUID.randomUUID().toString();
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BatchJobStatus getStatus() {
        return status.get();
    }

    @Override
    public JobMetrics getMetrics() {
        return metrics;
    }

    @Override
    public boolean cancel() {
        return status.compareAndSet(BatchJobStatus.RUNNING, BatchJobStatus.CANCELLED);
    }

    @Override
    public boolean pause() {
        return status.compareAndSet(BatchJobStatus.RUNNING, BatchJobStatus.PAUSED);
    }

    @Override
    public boolean resume() {
        return status.compareAndSet(BatchJobStatus.PAUSED, BatchJobStatus.RUNNING);
    }

    /**
     * 更新作业状态
     *
     * @param newStatus 新状态
     */
    protected void updateStatus(BatchJobStatus newStatus) {
        BatchJobStatus oldStatus = status.getAndSet(newStatus);
        logger.debug("Job {} status changed from {} to {}", jobId, oldStatus, newStatus);
    }
}
