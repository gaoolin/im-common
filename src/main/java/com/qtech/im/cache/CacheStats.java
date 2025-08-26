package com.qtech.im.cache;

import java.io.Serializable;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:49:28
 */

/**
 * 缓存统计信息
 * <p>
 * 记录缓存的命中率、请求数等统计信息
 */
public class CacheStats implements Serializable {
    private static final long serialVersionUID = 1L;

    // 请求总数
    private long requestCount = 0;

    // 命中次数
    private long hitCount = 0;

    // 未命中次数
    private long missCount = 0;

    // 加载成功次数
    private long loadSuccessCount = 0;

    // 加载失败次数
    private long loadExceptionCount = 0;

    // 总加载时间（纳秒）
    private long totalLoadTime = 0;

    // 删除次数
    private long evictionCount = 0;

    public CacheStats() {
    }

    /**
     * 记录缓存命中
     */
    public void recordHit() {
        requestCount++;
        hitCount++;
    }

    /**
     * 记录缓存未命中
     */
    public void recordMiss() {
        requestCount++;
        missCount++;
    }

    /**
     * 记录加载成功
     *
     * @param loadTime 加载时间（纳秒）
     */
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount++;
        totalLoadTime += loadTime;
    }

    /**
     * 记录加载失败
     *
     * @param loadTime 加载时间（纳秒）
     */
    public void recordLoadException(long loadTime) {
        loadExceptionCount++;
        totalLoadTime += loadTime;
    }

    /**
     * 记录缓存项被驱逐
     */
    public void recordEviction() {
        evictionCount++;
    }

    // Getters
    public long getRequestCount() {
        return requestCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public long getLoadSuccessCount() {
        return loadSuccessCount;
    }

    public long getLoadExceptionCount() {
        return loadExceptionCount;
    }

    public long getTotalLoadTime() {
        return totalLoadTime;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    /**
     * 获取命中率
     *
     * @return 命中率（0-1之间）
     */
    public double getHitRate() {
        return requestCount == 0 ? 0.0 : (double) hitCount / requestCount;
    }

    /**
     * 获取平均加载时间（毫秒）
     *
     * @return 平均加载时间
     */
    public double getAverageLoadTime() {
        long totalLoadCount = loadSuccessCount + loadExceptionCount;
        return totalLoadCount == 0 ? 0.0 : (double) totalLoadTime / totalLoadCount / 1000000.0;
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "requestCount=" + requestCount +
                ", hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", hitRate=" + String.format("%.2f", getHitRate() * 100) + "%" +
                ", loadSuccessCount=" + loadSuccessCount +
                ", loadExceptionCount=" + loadExceptionCount +
                ", averageLoadTime=" + String.format("%.2f", getAverageLoadTime()) + "ms" +
                ", evictionCount=" + evictionCount +
                '}';
    }
}
