package org.im.cache.stats;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache statistics
 * 缓存统计信息类
 * <p>
 * Records cache hit rate, request count, and other statistics
 * 收集和统计缓存使用情况，包括命中率、加载时间等关键指标
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19
 */
public class CacheStats implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 命中次数
     */
    private final AtomicLong hitCount = new AtomicLong(0);

    /**
     * 未命中次数
     */
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 加载成功次数
     */
    private final AtomicLong loadSuccessCount = new AtomicLong(0);

    /**
     * 加载异常次数
     */
    private final AtomicLong loadExceptionCount = new AtomicLong(0);

    /**
     * 驱逐次数
     */
    private final AtomicLong evictionCount = new AtomicLong(0);

    /**
     * 存储次数
     */
    private final AtomicLong putCount = new AtomicLong(0);

    /**
     * 总加载时间（纳秒）
     */
    private final AtomicLong totalLoadTime = new AtomicLong(0);

    /**
     * 默认构造函数
     */
    public CacheStats() {
    }

    /**
     * 记录命中
     */
    public void recordHit() {
        hitCount.incrementAndGet();
    }

    /**
     * 记录未命中
     */
    public void recordMiss() {
        missCount.incrementAndGet();
    }

    /**
     * 记录加载成功
     *
     * @param loadTime 加载时间（纳秒）
     */
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    /**
     * 记录加载异常
     *
     * @param loadTime 加载时间（纳秒）
     */
    public void recordLoadException(long loadTime) {
        loadExceptionCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    /**
     * 记录驱逐
     */
    public void recordEviction() {
        evictionCount.incrementAndGet();
    }

    /**
     * 记录存储
     */
    public void recordPut() {
        putCount.incrementAndGet();
    }

    /**
     * 获取命中次数
     *
     * @return 命中次数
     */
    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * 获取未命中次数
     *
     * @return 未命中次数
     */
    public long getMissCount() {
        return missCount.get();
    }

    /**
     * 获取加载成功次数
     *
     * @return 加载成功次数
     */
    public long getLoadSuccessCount() {
        return loadSuccessCount.get();
    }

    /**
     * 获取加载异常次数
     *
     * @return 加载异常次数
     */
    public long getLoadExceptionCount() {
        return loadExceptionCount.get();
    }

    /**
     * 获取驱逐次数
     *
     * @return 驱逐次数
     */
    public long getEvictionCount() {
        return evictionCount.get();
    }

    /**
     * 获取存储次数
     *
     * @return 存储次数
     */
    public long getPutCount() {
        return putCount.get();
    }

    /**
     * 获取总加载时间（纳秒）
     *
     * @return 总加载时间（纳秒）
     */
    public long getTotalLoadTime() {
        return totalLoadTime.get();
    }

    /**
     * 获取命中率
     *
     * @return 命中率 (0.0 to 1.0)
     */
    public double getHitRate() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    /**
     * 获取平均加载时间（毫秒）
     *
     * @return 平均加载时间（毫秒）
     */
    public double getAverageLoadTime() {
        long loads = loadSuccessCount.get() + loadExceptionCount.get();
        return loads == 0 ? 0.0 : totalLoadTime.get() / (1000000.0 * loads);
    }

    /**
     * 合并另一个统计对象的数据
     *
     * @param other 另一个统计对象
     */
    public void merge(CacheStats other) {
        hitCount.addAndGet(other.getHitCount());
        missCount.addAndGet(other.getMissCount());
        loadSuccessCount.addAndGet(other.getLoadSuccessCount());
        loadExceptionCount.addAndGet(other.getLoadExceptionCount());
        evictionCount.addAndGet(other.getEvictionCount());
        putCount.addAndGet(other.getPutCount());
        totalLoadTime.addAndGet(other.getTotalLoadTime());
    }

    /**
     * 重置统计数据
     */
    public void reset() {
        hitCount.set(0);
        missCount.set(0);
        loadSuccessCount.set(0);
        loadExceptionCount.set(0);
        evictionCount.set(0);
        putCount.set(0);
        totalLoadTime.set(0);
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "CacheStats{" +
                "hitCount=" + hitCount.get() +
                ", missCount=" + missCount.get() +
                ", loadSuccessCount=" + loadSuccessCount.get() +
                ", loadExceptionCount=" + loadExceptionCount.get() +
                ", evictionCount=" + evictionCount.get() +
                ", putCount=" + putCount.get() +
                ", totalLoadTime=" + totalLoadTime.get() +
                ", hitRate=" + String.format("%.2f%%", getHitRate() * 100) +
                ", averageLoadTime=" + String.format("%.2fms", getAverageLoadTime()) +
                '}';
    }
}