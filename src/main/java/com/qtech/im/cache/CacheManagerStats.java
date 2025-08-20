package com.qtech.im.cache;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:50:29
 * desc   :  im-common-IntelliJ IDEA
 */

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理器统计信息
 * <p>
 * 记录所有缓存实例的统计信息
 */
public class CacheManagerStats implements Serializable {
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

    @Override
    public String toString() {
        return "CacheManagerStats{" +
                "cacheCount=" + getCacheCount() +
                ", uptime=" + getUptime() + "ms" +
                ", cacheStats=" + cacheStats +
                '}';
    }
}