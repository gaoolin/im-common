package org.im.common.batch.core;

import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.monitor.JobMetrics;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 批处理作业管理器接口
 * <p>
 * 提供批处理作业的统一管理功能
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public interface BatchJobManager {

    /**
     * 提交作业执行
     *
     * @param job 作业实例
     * @param <T> 作业输入数据类型
     * @param <R> 作业输出结果类型
     * @return 作业执行Future
     */
    <T, R> Future<R> submitJob(BatchJob<T, R> job);

    /**
     * 提交作业执行（带输入数据）
     *
     * @param job   作业实例
     * @param input 作业输入数据
     * @param <T>   作业输入数据类型
     * @param <R>   作业输出结果类型
     * @return 作业执行Future
     */
    <T, R> Future<R> submitJob(BatchJob<T, R> job, T input);

    /**
     * 取消作业执行
     *
     * @param jobId 作业ID
     * @return 是否成功取消
     */
    boolean cancelJob(String jobId);

    /**
     * 暂停作业执行
     *
     * @param jobId 作业ID
     * @return 是否成功暂停
     */
    boolean pauseJob(String jobId);

    /**
     * 恢复作业执行
     *
     * @param jobId 作业ID
     * @return 是否成功恢复
     */
    boolean resumeJob(String jobId);

    /**
     * 获取作业状态
     *
     * @param jobId 作业ID
     * @return 作业状态
     */
    BatchJobStatus getJobStatus(String jobId);

    /**
     * 获取作业实例
     *
     * @param jobId 作业ID
     * @param <T>   作业输入数据类型
     * @param <R>   作业输出结果类型
     * @return 作业实例
     */
    <T, R> BatchJob<T, R> getJob(String jobId);

    /**
     * 获取所有作业
     *
     * @return 作业列表
     */
    List<BatchJob<?, ?>> getAllJobs();

    /**
     * 获取作业执行指标
     *
     * @param jobId 作业ID
     * @return 作业指标
     */
    JobMetrics getJobMetrics(String jobId);

    /**
     * 获取作业管理器配置
     *
     * @return 管理器配置
     */
    BatchConfig getManagerConfig();

    /**
     * 关闭作业管理器
     */
    void shutdown();

    /**
     * 强制关闭作业管理器
     */
    void shutdownNow();
}
