package org.im.common.batch.example;

import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.core.BatchJobStatus;
import org.im.common.batch.engine.BatchEngine;
import org.im.common.batch.processor.BatchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 自定义批处理作业示例
 * 演示如何创建自定义的批处理作业类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 */
public class CustomBatchJobExample extends BatchEngine<List<String>, List<BatchProcessor.BatchResult<String>>> {

    private static final Logger logger = LoggerFactory.getLogger(CustomBatchJobExample.class);

    private final BatchConfig config;

    public CustomBatchJobExample(String jobName, BatchConfig config) {
        super(jobName);
        this.config = config != null ? config : new BatchConfig();
    }

    public static void main(String[] args) throws Exception {
        // 创建配置
        BatchConfig config = BatchConfig.builder()
                .batchSize(200)
                .retryCount(2)
                .build();

        // 创建自定义批处理作业
        CustomBatchJobExample job = new CustomBatchJobExample("CustomDataProcessor", config);

        // 准备测试数据
        List<String> testData = generateTestData(1000);

        // 执行作业
        List<BatchProcessor.BatchResult<String>> results = job.execute(testData);

        // 输出结果信息
        logger.info("Job ID: {}", job.getJobId());
        logger.info("Job Status: {}", job.getStatus());
        logger.info("Results count: {}", results.size());
    }

    private static List<String> generateTestData(int count) {
        List<String> data = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            data.add("Data-" + i);
        }
        return data;
    }

    @Override
    public List<BatchProcessor.BatchResult<String>> execute(List<String> input) throws Exception {
        logger.info("Starting custom batch job: {}", getJobName());
        updateStatus(BatchJobStatus.RUNNING);
        getMetrics().start();

        try {
            // 执行批处理逻辑
            List<BatchProcessor.BatchResult<String>> results = BatchProcessor.processInBatches(
                    input,
                    this::processBatch,
                    config
            );

            updateStatus(BatchJobStatus.SUCCESS);
            getMetrics().finish();
            logger.info("Custom batch job completed successfully");
            return results;
        } catch (Exception e) {
            updateStatus(BatchJobStatus.FAILED);
            getMetrics().fail(e);
            logger.error("Custom batch job failed", e);
            throw e;
        }
    }

    private void processBatch(List<String> batch) {
        logger.debug("Processing batch with {} items", batch.size());
        for (String item : batch) {
            // 实现具体的处理逻辑
            simulateProcessing(item);
        }
    }

    private void simulateProcessing(String item) {
        // 模拟处理时间
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public BatchConfig getConfig() {
        return config;
    }

    /**
     * 启动组件
     */
    @Override
    public void start() {

    }

    /**
     * 停止组件
     */
    @Override
    public void stop() {

    }

    /**
     * 检查组件是否正在运行
     */
    @Override
    public boolean isRunning() {
        return false;
    }

    /**
     * 重启组件
     */
    @Override
    public void restart() {

    }
}
