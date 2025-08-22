package com.qtech.im.common.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 通用批量数据处理工具类
 * <p>
 * 特性：
 * - 通用化：支持任意类型数据的批量处理
 * - 规范化：统一的处理接口和错误处理机制
 * - 灵活性：支持自定义批处理大小、并发处理等
 * - 可靠性：完善的异常处理和资源管理
 * - 容错性：失败重试和部分失败处理机制
 * - 专业性：基于Java并发API实现
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class BatchProcessor {

    // 默认批处理大小
    public static final int DEFAULT_BATCH_SIZE = 1000;
    // 默认并发线程数
    public static final int DEFAULT_CONCURRENT_THREADS = 4;
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessor.class);

    /**
     * 分批处理数据
     *
     * @param data      待处理数据列表
     * @param batchSize 批处理大小
     * @param processor 批处理器
     * @param <T>       数据类型
     * @return 处理结果列表
     */
    public static <T> List<BatchResult<T>> processInBatches(List<T> data, int batchSize,
                                                            Consumer<List<T>> processor) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (batchSize <= 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        }

        List<BatchResult<T>> results = new ArrayList<>();
        int totalBatches = (int) Math.ceil((double) data.size() / batchSize);

        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, data.size());
            List<T> batch = data.subList(start, end);

            try {
                long startTime = System.currentTimeMillis();
                processor.accept(batch);
                long endTime = System.currentTimeMillis();

                results.add(new BatchResult<>(i, batch.size(), true, null, endTime - startTime));
                logger.debug("Processed batch {} with {} items in {} ms", i, batch.size(), endTime - startTime);
            } catch (Exception e) {
                logger.error("Failed to process batch {}", i, e);
                results.add(new BatchResult<>(i, batch.size(), false, e, 0));
            }
        }

        return results;
    }

    /**
     * 并行处理数据
     *
     * @param data      待处理数据列表
     * @param processor 数据处理器
     * @param <T>       输入数据类型
     * @param <R>       输出数据类型
     * @return 处理结果列表
     */
    public static <T, R> List<R> processParallel(List<T> data, Function<T, R> processor) {
        return processParallel(data, processor, DEFAULT_CONCURRENT_THREADS);
    }

    /**
     * 并行处理数据（指定线程数）
     *
     * @param data        待处理数据列表
     * @param processor   数据处理器
     * @param threadCount 线程数
     * @param <T>         输入数据类型
     * @param <R>         输出数据类型
     * @return 处理结果列表
     */
    public static <T, R> List<R> processParallel(List<T> data, Function<T, R> processor, int threadCount) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (threadCount <= 0) {
            threadCount = DEFAULT_CONCURRENT_THREADS;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<R>> futures = new ArrayList<>();

        try {
            // 提交任务
            for (T item : data) {
                Future<R> future = executor.submit(() -> processor.apply(item));
                futures.add(future);
            }

            // 收集结果
            List<R> results = new ArrayList<>();
            for (Future<R> future : futures) {
                try {
                    R result = future.get();
                    results.add(result);
                } catch (Exception e) {
                    logger.warn("Failed to get result from future", e);
                    results.add(null); // 或者抛出异常
                }
            }

            return results;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 批处理结果类
     */
    public static class BatchResult<T> {
        private final int batchIndex;
        private final int batchSize;
        private final boolean success;
        private final Exception error;
        private final long processingTime;

        public BatchResult(int batchIndex, int batchSize, boolean success, Exception error, long processingTime) {
            this.batchIndex = batchIndex;
            this.batchSize = batchSize;
            this.success = success;
            this.error = error;
            this.processingTime = processingTime;
        }

        // Getters
        public int getBatchIndex() {
            return batchIndex;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public boolean isSuccess() {
            return success;
        }

        public Exception getError() {
            return error;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        @Override
        public String toString() {
            return "BatchResult{" +
                    "batchIndex=" + batchIndex +
                    ", batchSize=" + batchSize +
                    ", success=" + success +
                    ", processingTime=" + processingTime + "ms" +
                    '}';
        }
    }
}
