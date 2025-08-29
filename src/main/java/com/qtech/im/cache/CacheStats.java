package com.qtech.im.cache;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache statistics
 * <p>
 * Records cache hit rate, request count, and other statistics
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */
public class CacheStats implements Serializable {
    private static final long serialVersionUID = 1L;

    // Request count
    private final AtomicLong requestCount = new AtomicLong();
    // Hit count
    private final AtomicLong hitCount = new AtomicLong();
    // Miss count
    private final AtomicLong missCount = new AtomicLong();
    // Load success count
    private final AtomicLong loadSuccessCount = new AtomicLong();
    // Load failure count
    private final AtomicLong loadExceptionCount = new AtomicLong();
    // Total load time (nanoseconds)
    private final AtomicLong totalLoadTime = new AtomicLong();
    // Eviction count
    private final AtomicLong evictionCount = new AtomicLong();
    // Put count
    private final AtomicLong putCount = new AtomicLong();

    public CacheStats() {
    }

    // Constructor for Caffeine stats
    public CacheStats(com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats) {
        this.requestCount.set(caffeineStats.requestCount());
        this.hitCount.set(caffeineStats.hitCount());
        this.missCount.set(caffeineStats.missCount());
        this.loadSuccessCount.set(caffeineStats.loadSuccessCount());
        this.loadExceptionCount.set(caffeineStats.loadFailureCount());
        this.totalLoadTime.set(caffeineStats.totalLoadTime());
        this.evictionCount.set(caffeineStats.evictionCount());
        this.putCount.set(0); // Caffeine stats don’t track puts explicitly
    }

    /**
     * Records a cache hit
     */
    public void recordHit() {
        requestCount.incrementAndGet();
        hitCount.incrementAndGet();
    }

    /**
     * Records a cache miss
     */
    public void recordMiss() {
        requestCount.incrementAndGet();
        missCount.incrementAndGet();
    }

    /**
     * Records a successful load
     *
     * @param loadTime Load time (nanoseconds)
     */
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    /**
     * Records a failed load
     *
     * @param loadTime Load time (nanoseconds)
     */
    public void recordLoadException(long loadTime) {
        loadExceptionCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    /**
     * Records a cache eviction
     */
    public void recordEviction() {
        evictionCount.incrementAndGet();
    }

    /**
     * Records a cache put operation
     */
    public void recordPut() {
        putCount.incrementAndGet();
    }

    /**
     * Resets all statistics counters to zero
     */
    public void reset() {
        requestCount.set(0);
        hitCount.set(0);
        missCount.set(0);
        loadSuccessCount.set(0);
        loadExceptionCount.set(0);
        totalLoadTime.set(0);
        evictionCount.set(0);
        putCount.set(0);
    }

    // Getters
    public long getRequestCount() {
        return requestCount.get();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    public long getLoadSuccessCount() {
        return loadSuccessCount.get();
    }

    public long getLoadExceptionCount() {
        return loadExceptionCount.get();
    }

    public long getTotalLoadTime() {
        return totalLoadTime.get();
    }

    public long getEvictionCount() {
        return evictionCount.get();
    }

    public long getPutCount() {
        return putCount.get();
    }

    /**
     * Gets the hit rate
     *
     * @return Hit rate (0-1)
     */
    public double getHitRate() {
        long total = requestCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }

    /**
     * Gets the average load time (milliseconds)
     *
     * @return Average load time
     */
    public double getAverageLoadTime() {
        long totalLoadCount = loadSuccessCount.get() + loadExceptionCount.get();
        return totalLoadCount == 0 ? 0.0 : (double) totalLoadTime.get() / totalLoadCount / 1_000_000.0;
    }

    /**
     * Merges another CacheStats object
     *
     * @param other Other CacheStats object
     * @return Merged CacheStats object
     */
    public CacheStats merge(CacheStats other) {
        CacheStats merged = new CacheStats();
        merged.requestCount.set(this.requestCount.get() + other.requestCount.get());
        merged.hitCount.set(this.hitCount.get() + other.hitCount.get());
        merged.missCount.set(this.missCount.get() + other.missCount.get());
        merged.loadSuccessCount.set(this.loadSuccessCount.get() + other.loadSuccessCount.get());
        merged.loadExceptionCount.set(this.loadExceptionCount.get() + other.loadExceptionCount.get());
        merged.totalLoadTime.set(this.totalLoadTime.get() + other.totalLoadTime.get());
        merged.evictionCount.set(this.evictionCount.get() + other.evictionCount.get());
        merged.putCount.set(this.putCount.get() + other.putCount.get());
        return merged;
    }

    /**
     * Adds Caffeine CacheStats
     */
    public void add(com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats) {
        this.requestCount.addAndGet(caffeineStats.requestCount());
        this.hitCount.addAndGet(caffeineStats.hitCount());
        this.missCount.addAndGet(caffeineStats.missCount());
        this.loadSuccessCount.addAndGet(caffeineStats.loadSuccessCount());
        this.loadExceptionCount.addAndGet(caffeineStats.loadFailureCount());
        this.totalLoadTime.addAndGet(caffeineStats.totalLoadTime());
        this.evictionCount.addAndGet(caffeineStats.evictionCount());
        // putCount not updated from Caffeine stats
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "requestCount=" + requestCount.get() +
                ", hitCount=" + hitCount.get() +
                ", missCount=" + missCount.get() +
                ", hitRate=" + String.format("%.2f", getHitRate() * 100) + "%" +
                ", loadSuccessCount=" + loadSuccessCount.get() +
                ", loadExceptionCount=" + loadExceptionCount.get() +
                ", averageLoadTime=" + String.format("%.2f", getAverageLoadTime()) + "ms" +
                ", evictionCount=" + evictionCount.get() +
                ", putCount=" + putCount.get() +
                '}';
    }
}