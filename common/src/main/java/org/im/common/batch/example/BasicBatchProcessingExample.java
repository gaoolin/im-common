package org.im.common.batch.example;

import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.processor.BatchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 基本批处理示例
 * 演示如何使用 BatchProcessor 进行基本的批处理操作
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 */
public class BasicBatchProcessingExample {

    private static final Logger logger = LoggerFactory.getLogger(BasicBatchProcessingExample.class);

    public static void main(String[] args) {
        // 准备测试数据
        List<String> data = generateTestData(5000);

        // 创建批处理配置
        BatchConfig config = BatchConfig.builder()
                .batchSize(500)
                .retryCount(3)
                .retryDelayMs(1000)
                .continueOnError(true)
                .build();

        // 定义批处理器
        Consumer<List<String>> processor = batch -> {
            logger.info("Processing batch with {} items", batch.size());
            for (String item : batch) {
                // 模拟处理逻辑
                processItem(item);
            }
        };

        // 执行批处理
        List<BatchProcessor.BatchResult<String>> results =
                BatchProcessor.processInBatches(data, processor, config);

        // 分析处理结果
        analyzeResults(results);
    }

    private static List<String> generateTestData(int count) {
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            data.add("Item-" + i);
        }
        return data;
    }

    private static void processItem(String item) {
        // 模拟处理时间
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟偶尔出现的处理失败
        if (Math.random() < 0.01) { // 1% 的失败概率
            throw new RuntimeException("Random processing failure for item: " + item);
        }
    }

    private static void analyzeResults(List<BatchProcessor.BatchResult<String>> results) {
        int successCount = 0;
        int failureCount = 0;
        long totalProcessingTime = 0;

        for (BatchProcessor.BatchResult<String> result : results) {
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
            totalProcessingTime += result.getProcessingTime();
        }

        logger.info("Batch processing completed:");
        logger.info("  Total batches: {}", results.size());
        logger.info("  Successful batches: {}", successCount);
        logger.info("  Failed batches: {}", failureCount);
        logger.info("  Average processing time per batch: {} ms",
                results.isEmpty() ? 0 : totalProcessingTime / results.size());
    }
}
