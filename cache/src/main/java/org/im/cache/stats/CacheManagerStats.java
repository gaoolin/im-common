package org.im.cache.stats;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存管理器统计信息
 * <p>
 * 记录所有缓存实例的统计信息
 * 缓存管理器统计信息类
 * 收集和统计缓存管理器级别的使用情况
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/29
 */

public abstract class CacheManagerStats implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 缓存创建次数
     */
    private final AtomicLong cacheCreateCount = new AtomicLong(0);

    /**
     * 缓存移除次数
     */
    private final AtomicLong cacheRemoveCount = new AtomicLong(0);

    /**
     * 缓存获取次数
     */
    private final AtomicLong cacheGetCount = new AtomicLong(0);

    /**
     * 缓存操作总数
     */
    private final AtomicLong totalOperationCount = new AtomicLong(0);

    /**
     * 启动时间戳
     */
    private final long startTime = System.currentTimeMillis();

    /**
     * 默认构造函数
     */
    public CacheManagerStats() {
    }

    /**
     * 记录缓存创建
     */
    public void recordCacheCreate() {
        cacheCreateCount.incrementAndGet();
        totalOperationCount.incrementAndGet();
    }

    /**
     * 记录缓存移除
     */
    public void recordCacheRemove() {
        cacheRemoveCount.incrementAndGet();
        totalOperationCount.incrementAndGet();
    }

    /**
     * 记录缓存获取
     */
    public void recordCacheGet() {
        cacheGetCount.incrementAndGet();
        totalOperationCount.incrementAndGet();
    }

    /**
     * 获取缓存创建次数
     *
     * @return 缓存创建次数
     */
    public long getCacheCreateCount() {
        return cacheCreateCount.get();
    }

    /**
     * 获取缓存移除次数
     *
     * @return 缓存移除次数
     */
    public long getCacheRemoveCount() {
        return cacheRemoveCount.get();
    }

    /**
     * 获取缓存获取次数
     *
     * @return 缓存获取次数
     */
    public long getCacheGetCount() {
        return cacheGetCount.get();
    }

    /**
     * 获取总操作次数
     *
     * @return 总操作次数
     */
    public long getTotalOperationCount() {
        return totalOperationCount.get();
    }

    public abstract int getCacheCount();

    /**
     * 获取运行时间（毫秒）
     *
     * @return 运行时间（毫秒）
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 获取运行时间（格式化字符串）
     *
     * @return 运行时间（格式化字符串）
     */
    public String getFormattedUptime() {
        long uptime = getUptime();
        long hours = uptime / 3600000;
        long minutes = (uptime % 3600000) / 60000;
        long seconds = (uptime % 60000) / 1000;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "CacheManagerStats{" +
                "cacheCreateCount=" + cacheCreateCount.get() +
                ", cacheRemoveCount=" + cacheRemoveCount.get() +
                ", cacheGetCount=" + cacheGetCount.get() +
                ", totalOperationCount=" + totalOperationCount.get() +
                ", uptime=" + getFormattedUptime() +
                '}';
    }

    public abstract CacheStats getAggregatedStats();
}