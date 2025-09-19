package com.im.batch;

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
 * 通用批量处理工具类
 * <p>
 * 特性：
 * - 通用性：支持任意类型数据的批量处理
 * - 规范性：统一的处理接口和错误处理机制
 * - 专业性：基于Java并发API实现
 * - 灵活性：支持自定义批处理大小、并发处理等
 * - 可靠性：完善的异常处理和资源管理
 * - 安全性：线程安全设计
 * - 复用性：高度可复用的批处理框架
 * - 容错性：失败重试和部分失败处理机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class BatchProcessorKit {

    // 默认批处理大小
    public static final int DEFAULT_BATCH_SIZE = 1000;
    // 默认并发线程数
    public static final int DEFAULT_CONCURRENT_THREADS = 4;
    // 默认重试次数
    public static final int DEFAULT_RETRY_COUNT = 3;
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessorKit.class);

    /**
     * 分批处理数据
     *
     * @param data      待处理数据列表
     * @param processor 批处理器
     * @param config    批处理配置
     * @param <T>       数据类型
     * @return 处理结果列表
     */
    public static <T> List<BatchResult<T>> processInBatches(List<T> data,
                                                            Consumer<List<T>> processor,
                                                            BatchConfig config) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (config == null) {
            config = new BatchConfig();
        }

        List<BatchResult<T>> results = new ArrayList<>();
        int batchSize = config.getBatchSize() > 0 ? config.getBatchSize() : DEFAULT_BATCH_SIZE;
        int totalBatches = (int) Math.ceil((double) data.size() / batchSize);

        logger.info("Starting batch processing: {} items in {} batches", data.size(), totalBatches);

        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, data.size());
            List<T> batch = data.subList(start, end);

            Exception lastException = null;
            boolean success = false;
            long processingTime = 0;

            // 重试机制
            for (int retry = 0; retry <= config.getRetryCount(); retry++) {
                try {
                    long startTime = System.currentTimeMillis();
                    processor.accept(batch);
                    long endTime = System.currentTimeMillis();

                    processingTime = endTime - startTime;
                    success = true;
                    lastException = null;

                    logger.debug("Processed batch {} with {} items in {} ms", i, batch.size(), processingTime);
                    break; // 成功处理，跳出重试循环

                } catch (Exception e) {
                    lastException = e;
                    logger.warn("Failed to process batch {} (attempt {}/{})", i, retry + 1, config.getRetryCount() + 1, e);

                    if (retry < config.getRetryCount()) {
                        try {
                            Thread.sleep(config.getRetryDelay() * (retry + 1)); // 指数退避
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            BatchResult<T> result = new BatchResult<>(i, batch.size(), success, lastException, processingTime);
            results.add(result);

            // 如果配置为出错停止，且当前批次失败，则停止处理
            if (!success && !config.isContinueOnError()) {
                logger.error("Batch processing stopped due to failure in batch {}", i);
                break;
            }
        }

        return results;
    }

    /**
     * 并行处理数据
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
                Future<R> future = executor.submit(() -> {
                    try {
                        return processor.apply(item);
                    } catch (Exception e) {
                        logger.warn("Failed to process item", e);
                        return null; // 或者抛出异常
                    }
                });
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
            shutdownExecutor(executor);
        }
    }

    /**
     * 安全关闭线程池
     *
     * @param executor 线程池
     */
    private static void shutdownExecutor(ExecutorService executor) {
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

    /**
     * 批处理配置类
     */
    public static class BatchConfig {
        private int batchSize = DEFAULT_BATCH_SIZE;
        private int retryCount = DEFAULT_RETRY_COUNT;
        private long retryDelay = 1000; // 重试延迟毫秒
        private boolean continueOnError = false; // 出错是否继续

        // Getters and Setters
        public int getBatchSize() {
            return batchSize;
        }

        public BatchConfig setBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public BatchConfig setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public BatchConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public boolean isContinueOnError() {
            return continueOnError;
        }

        public BatchConfig setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
            return this;
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
                    (error != null ? ", error=" + error.getMessage() : "") +
                    '}';
        }
    }
}
