package com.qtech.im.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理器统计信息
 * <p>
 * 记录所有缓存实例的统计信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */

public abstract class CacheManagerStats implements Serializable {
    private static final long serialVersionUID = 1L;

    // 各缓存实例的统计信息
    private final Map<String, CacheStats> cacheStats = new ConcurrentHashMap<>();

    // 缓存管理器启动时间
    private final long startTime = System.currentTimeMillis();

    public CacheManagerStats() {
    }

    /**
     * 添加缓存统计信息
     *
     * @param cacheName 缓存名称
     * @param stats     缓存统计信息
     */
    public void addCacheStats(String cacheName, CacheStats stats) {
        cacheStats.put(cacheName, stats);
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        return cacheStats.get(cacheName);
    }

    /**
     * 获取所有缓存统计信息
     *
     * @return 缓存统计信息映射
     */
    public Map<String, CacheStats> getAllCacheStats() {
        return new ConcurrentHashMap<>(cacheStats);
    }

    /**
     * 获取缓存管理器运行时间（毫秒）
     *
     * @return 运行时间
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 获取缓存实例数量
     *
     * @return 缓存实例数量
     */
    public int getCacheCount() {
        return cacheStats.size();
    }

    /**
     * 获取所有缓存统计信息
     *
     * @return 缓存统计信息映射
     */
    public Map<String, CacheStats> getCacheStatsMap() {
        return cacheStats;
    }

    /**
     * 获取请求总数
     *
     * @return 请求总数
     */
    public long getRequestCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getRequestCount).sum();
    }

    /**
     * 获取命中次数
     *
     * @return 命中次数
     */
    public long getHitCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getHitCount).sum();
    }

    /**
     * 获取未命中次数
     *
     * @return 未命中次数
     */
    public long getMissCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getMissCount).sum();
    }

    /**
     * 获取加载成功次数
     *
     * @return 加载成功次数
     */
    public long getLoadSuccessCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getLoadSuccessCount).sum();
    }

    /**
     * 获取加载失败次数
     *
     * @return 加载失败次数
     */
    public long getLoadExceptionCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getLoadExceptionCount).sum();
    }

    /**
     * 获取总加载时间（纳秒）
     *
     * @return 总加载时间
     */
    public long getTotalLoadTime() {
        return cacheStats.values().stream().mapToLong(CacheStats::getTotalLoadTime).sum();
    }

    /**
     * 获取驱逐次数
     *
     * @return 驱逐次数
     */
    public long getEvictionCount() {
        return cacheStats.values().stream().mapToLong(CacheStats::getEvictionCount).sum();
    }

    /**
     * 获取命中率
     *
     * @return 命中率（0-1之间）
     */
    public double getHitRate() {
        long requestCount = getRequestCount();
        return requestCount == 0 ? 0.0 : (double) getHitCount() / requestCount;
    }

    /**
     * 获取平均加载时间（毫秒）
     *
     * @return 平均加载时间
     */
    public double getAverageLoadTime() {
        long totalLoadCount = getLoadSuccessCount() + getLoadExceptionCount();
        return totalLoadCount == 0 ? 0.0 : (double) getTotalLoadTime() / totalLoadCount / 1000000.0;
    }

    @Override
    public String toString() {
        return "CacheManagerStats{" +
                "cacheCount=" + cacheStats.size() +
                ", requestCount=" + getRequestCount() +
                ", hitCount=" + getHitCount() +
                ", missCount=" + getMissCount() +
                ", hitRate=" + String.format("%.2f", getHitRate() * 100) + "%" +
                ", loadSuccessCount=" + getLoadSuccessCount() +
                ", loadExceptionCount=" + getLoadExceptionCount() +
                ", averageLoadTime=" + String.format("%.2f", getAverageLoadTime()) + "ms" +
                ", evictionCount=" + getEvictionCount() +

                '}';
    }

    public abstract CacheStats getAggregatedStats();
}