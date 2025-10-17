package org.im.common.batch.example;

import org.im.common.batch.processor.BatchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 并行处理示例
 * 演示如何使用 BatchProcessor 进行并行数据处理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 */
public class ParallelProcessingExample {

    private static final Logger logger = LoggerFactory.getLogger(ParallelProcessingExample.class);

    public static void main(String[] args) {
        // 准备大量测试数据
        List<Integer> data = IntStream.range(1, 10000)
                .boxed()
                .collect(Collectors.toList());

        logger.info("Starting parallel processing with {} items", data.size());

        // 定义处理函数
        Function<Integer, ProcessResult> processor = item -> {
            // 模拟复杂的处理逻辑
            String processedData = performComplexCalculation(item);
            return new ProcessResult(item, processedData, System.currentTimeMillis());
        };

        // 使用默认线程数进行并行处理
        long startTime = System.currentTimeMillis();
        List<ProcessResult> results1 = BatchProcessor.processParallel(data, processor);
        long endTime = System.currentTimeMillis();

        logger.info("Parallel processing with default threads completed in {} ms", endTime - startTime);
        logger.info("Processed {} items", results1.size());

        // 使用指定线程数进行并行处理
        startTime = System.currentTimeMillis();
        List<ProcessResult> results2 = BatchProcessor.processParallel(data, processor, 8);
        endTime = System.currentTimeMillis();

        logger.info("Parallel processing with 8 threads completed in {} ms", endTime - startTime);
        logger.info("Processed {} items", results2.size());

        // 分析处理结果
        analyzeParallelResults(results2);
    }

    private static String performComplexCalculation(Integer item) {
        // 模拟复杂计算
        try {
            Thread.sleep(1); // 模拟处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 执行一些计算操作
        long result = item;
        for (int i = 0; i < 10; i++) {
            result = (result * 31) % 1000000;
        }

        return "Processed-" + item + "-Result-" + result;
    }

    private static void analyzeParallelResults(List<ProcessResult> results) {
        long successCount = results.stream().filter(r -> r != null).count();
        long nullCount = results.stream().filter(r -> r == null).count();

        logger.info("Parallel processing analysis:");
        logger.info("  Successful results: {}", successCount);
        logger.info("  Null results: {}", nullCount);
        logger.info("  Success rate: {}%", (successCount * 100.0 / results.size()));
    }

    /**
     * 处理结果类
     */
    static class ProcessResult {
        private final int originalValue;
        private final String processedValue;
        private final long timestamp;

        public ProcessResult(int originalValue, String processedValue, long timestamp) {
            this.originalValue = originalValue;
            this.processedValue = processedValue;
            this.timestamp = timestamp;
        }

        // Getters
        public int getOriginalValue() {
            return originalValue;
        }

        public String getProcessedValue() {
            return processedValue;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "ProcessResult{" +
                    "originalValue=" + originalValue +
                    ", processedValue='" + processedValue + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}

