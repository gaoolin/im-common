package org.im.common.batch.config;

import java.time.Duration;

/**
 * 批处理配置构建器
 * <p>
 * 提供链式调用方式构建批处理配置
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public class BatchConfigBuilder {

    private final BatchConfig config;

    BatchConfigBuilder() {
        this.config = new BatchConfig();
    }

    /**
     * 设置批处理大小
     *
     * @param batchSize 批处理大小
     * @return 配置构建器
     */
    public BatchConfigBuilder batchSize(int batchSize) {
        config.setBatchSize(batchSize);
        return this;
    }

    /**
     * 设置并发线程数
     *
     * @param concurrentThreads 并发线程数
     * @return 配置构建器
     */
    public BatchConfigBuilder concurrentThreads(int concurrentThreads) {
        config.setConcurrentThreads(concurrentThreads);
        return this;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount 重试次数
     * @return 配置构建器
     */
    public BatchConfigBuilder retryCount(int retryCount) {
        config.setRetryCount(retryCount);
        return this;
    }

    /**
     * 设置重试延迟
     *
     * @param retryDelayMs 重试延迟（毫秒）
     * @return 配置构建器
     */
    public BatchConfigBuilder retryDelayMs(long retryDelayMs) {
        config.setRetryDelayMs(retryDelayMs);
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间
     * @return 配置构建器
     */
    public BatchConfigBuilder timeout(Duration timeout) {
        config.setTimeout(timeout);
        return this;
    }

    /**
     * 设置出错是否继续
     *
     * @param continueOnError 出错是否继续
     * @return 配置构建器
     */
    public BatchConfigBuilder continueOnError(boolean continueOnError) {
        config.setContinueOnError(continueOnError);
        return this;
    }

    /**
     * 设置自定义属性
     *
     * @param key   属性键
     * @param value 属性值
     * @return 配置构建器
     */
    public BatchConfigBuilder property(String key, Object value) {
        config.setProperty(key, value);
        return this;
    }

    /**
     * 构建配置实例
     *
     * @return 配置实例
     */
    public BatchConfig build() {
        return config;
    }
}

