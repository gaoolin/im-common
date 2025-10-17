package org.im.common.batch.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 批处理作业配置类
 * <p>
 * 定义批处理作业的配置参数
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public class BatchConfig {

    // 默认批处理大小
    public static final int DEFAULT_BATCH_SIZE = 1000;

    // 默认并发线程数
    public static final int DEFAULT_CONCURRENT_THREADS = 4;

    // 默认重试次数
    public static final int DEFAULT_RETRY_COUNT = 3;

    // 默认超时时间（毫秒）
    public static final long DEFAULT_TIMEOUT_MS = 300000L; // 5分钟

    // 批处理大小
    private int batchSize = DEFAULT_BATCH_SIZE;

    // 并发线程数
    private int concurrentThreads = DEFAULT_CONCURRENT_THREADS;

    // 重试次数
    private int retryCount = DEFAULT_RETRY_COUNT;

    // 重试延迟（毫秒）
    private long retryDelayMs = 1000L;

    // 超时时间
    private Duration timeout = Duration.ofMillis(DEFAULT_TIMEOUT_MS);

    // 出错是否继续
    private boolean continueOnError = false;

    // 自定义配置参数
    private Map<String, Object> customProperties = new HashMap<>();

    // 私有构造函数
    public BatchConfig() {
    }

    /**
     * 创建配置构建器
     *
     * @return 配置构建器
     */
    public static BatchConfigBuilder builder() {
        return new BatchConfigBuilder();
    }

    // Getters and Setters
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getConcurrentThreads() {
        return concurrentThreads;
    }

    public void setConcurrentThreads(int concurrentThreads) {
        this.concurrentThreads = concurrentThreads;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * 设置自定义属性
     *
     * @param key   属性键
     * @param value 属性值
     * @return 当前配置实例
     */
    public BatchConfig setProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    /**
     * 获取自定义属性
     *
     * @param key 属性键
     * @param <T> 属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) this.customProperties.get(key);
    }

    /**
     * 获取自定义属性（带默认值）
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @param <T>          属性值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        return (T) this.customProperties.getOrDefault(key, defaultValue);
    }
}
