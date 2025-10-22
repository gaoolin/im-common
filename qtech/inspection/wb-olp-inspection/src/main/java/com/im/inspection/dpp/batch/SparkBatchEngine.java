package com.im.inspection.dpp.batch;

import org.apache.spark.sql.SparkSession;
import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.core.BatchJobStatus;
import org.im.common.batch.engine.BatchEngine;
import org.im.common.lifecycle.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spark批处理引擎
 * <p>
 * 专门用于处理基于Apache Spark的批处理任务
 * </p>
 *
 * @param <T> 输入数据类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public abstract class SparkBatchEngine<T> extends BatchEngine<T, Void> implements Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(SparkBatchEngine.class);

    private final SparkSession sparkSession;
    private final BatchConfig config;
    private final String appName;

    // 生命周期状态
    private volatile boolean running = false;

    /**
     * 构造函数
     *
     * @param appName      应用名称
     * @param sparkSession Spark会话
     * @param config       批处理配置
     */
    public SparkBatchEngine(String appName, SparkSession sparkSession, BatchConfig config) {
        super(appName);
        this.appName = appName;
        this.sparkSession = sparkSession;
        this.config = config != null ? config : new BatchConfig();
    }

    /**
     * 构造函数（使用默认配置）
     *
     * @param appName      应用名称
     * @param sparkSession Spark会话
     */
    public SparkBatchEngine(String appName, SparkSession sparkSession) {
        this(appName, sparkSession, null);
    }

    @Override
    public Void execute(T input) throws Exception {
        updateStatus(BatchJobStatus.RUNNING);
        getMetrics().start();
        running = true;

        try {
            logger.info("Starting Spark batch job: {}", getJobName());

            // 执行Spark任务逻辑
            processSparkJob(input);

            updateStatus(BatchJobStatus.SUCCESS);
            getMetrics().finish();
            running = false;

            logger.info("Spark batch job completed successfully: {}", getJobName());
            return null;
        } catch (Exception e) {
            updateStatus(BatchJobStatus.FAILED);
            getMetrics().fail(e);
            running = false;

            logger.error("Spark batch job failed: {}", getJobName(), e);
            throw e;
        }
    }

    /**
     * 处理Spark作业逻辑
     *
     * @param input 输入数据
     * @throws Exception 处理异常
     */
    protected abstract void processSparkJob(T input) throws Exception;

    @Override
    public BatchConfig getConfig() {
        return config;
    }

    /**
     * 获取Spark会话
     *
     * @return SparkSession实例
     */
    public SparkSession getSparkSession() {
        return sparkSession;
    }

    /**
     * 获取应用名称
     *
     * @return 应用名称
     */
    public String getAppName() {
        return appName;
    }

    // Lifecycle 接口实现
    @Override
    public void start() {
        // 启动逻辑可以在这里实现，或者由子类实现具体的启动逻辑
        running = true;
        logger.info("Spark batch engine started: {}", appName);
    }

    @Override
    public void stop() {
        // 停止逻辑
        running = false;
        sparkSession.close();
        logger.info("Spark batch engine stopped: {}", appName);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void restart() {
        stop();
        start();
        logger.info("Spark batch engine restarted: {}", appName);
    }
}
