package org.im.common.batch.core;

import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.monitor.JobMetrics;
import org.im.common.lifecycle.Lifecycle;

/**
 * 批处理作业接口
 * <p>
 * 定义批处理作业的核心行为和属性
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
public interface BatchJob<T, R> extends Lifecycle {

    /**
     * 获取作业唯一标识符
     *
     * @return 作业ID
     */
    String getJobId();

    /**
     * 获取作业名称
     *
     * @return 作业名称
     */
    String getJobName();

    /**
     * 获取作业描述
     *
     * @return 作业描述
     */
    String getDescription();

    /**
     * 执行作业
     *
     * @param input 作业输入数据
     * @return 作业执行结果
     * @throws Exception 作业执行异常
     */
    R execute(T input) throws Exception;

    /**
     * 获取作业状态
     *
     * @return 当前作业状态
     */
    BatchJobStatus getStatus();

    /**
     * 获取作业配置
     *
     * @return 作业配置信息
     */
    BatchConfig getConfig();

    /**
     * 获取作业指标
     *
     * @return 作业执行指标
     */
    JobMetrics getMetrics();

    /**
     * 取消作业执行
     *
     * @return 是否成功取消
     */
    boolean cancel();

    /**
     * 暂停作业执行
     *
     * @return 是否成功暂停
     */
    boolean pause();

    /**
     * 恢复作业执行
     *
     * @return 是否成功恢复
     */
    boolean resume();
}
