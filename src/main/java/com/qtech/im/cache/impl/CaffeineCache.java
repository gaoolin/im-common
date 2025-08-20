package com.qtech.im.cache.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:51:34
 * desc   :
 */

/**
 * 基于Caffeine的本地缓存实现
 * <p>
 * 提供高性能的本地缓存功能，支持过期策略、大小限制、统计等特性
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class CaffeineCache<K, V> implements Cache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;
    private final CacheConfig config;
    private final com.qtech.im.cache.CacheStats stats = new com.qtech.im.cache.CacheStats();

    public CaffeineCache(CacheConfig config) {
        this.config = config;
        this.cache = buildCache(config);
    }

    /**
     * 构建Caffeine缓存实例
     *
     * @param config 缓存配置
     * @return Caffeine缓存实例
     */
    private com.github.benmanes.caffeine.cache.Cache<K, V> buildCache(CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // 设置最大大小
        if (config.getMaximumSize() > 0) {
            builder.maximumSize(config.getMaximumSize());
        }

        // 设置写入后过期时间
        if (config.getExpireAfterWrite() > 0) {
            builder.expireAfterWrite(config.getExpireAfterWrite(), TimeUnit.MILLISECONDS);
        }

        // 设置访问后过期时间
        if (config.getExpireAfterAccess() > 0) {
            builder.expireAfterAccess(config.getExpireAfterAccess(), TimeUnit.MILLISECONDS);
        }

        // 启用统计
        if (config.isRecordStats()) {
            builder.recordStats();
        }

        return builder.build();
    }

    @Override
    public V get(K key) {
        try {
            V value = cache.getIfPresent(key);
            if (value != null) {
                stats.recordHit();
            } else {
                stats.recordMiss();
            }
            return value;
        } catch (Exception e) {
            logger.warn("Error getting value from cache for key: {}", key, e);
            stats.recordMiss();
            return null;
        }
    }

    @Override
    public void put(K key, V value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            logger.warn("Error putting value to cache for key: {}", key, e);
        }
    }

    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        // Caffeine不直接支持单个键的TTL，这里简化处理
        put(key, value);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        try {
            return cache.getAllPresent(keys);
        } catch (Exception e) {
            logger.warn("Error getting values from cache for keys: {}", keys, e);
            return java.util.Collections.emptyMap();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try {
            cache.putAll(map);
        } catch (Exception e) {
            logger.warn("Error putting values to cache", e);
        }
    }

    @Override
    public boolean remove(K key) {
        try {
            cache.invalidate(key);
            return true;
        } catch (Exception e) {
            logger.warn("Error removing value from cache for key: {}", key, e);
            return false;
        }
    }

    @Override
    public int removeAll(Set<? extends K> keys) {
        try {
            cache.invalidateAll(keys);
            return keys.size();
        } catch (Exception e) {
            logger.warn("Error removing values from cache for keys: {}", keys, e);
            return 0;
        }
    }

    @Override
    public boolean containsKey(K key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public com.qtech.im.cache.CacheStats getStats() {
        if (config.isRecordStats()) {
            CacheStats caffeineStats = cache.stats();
            com.qtech.im.cache.CacheStats result = new com.qtech.im.cache.CacheStats();
            // 这里需要根据实际需求映射统计信息
            return result;
        }
        return stats;
    }

    @Override
    public CacheConfig getConfig() {
        return config;
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader) {
        try {
            long startTime = System.nanoTime();
            V value = cache.get(key, k -> {
                try {
                    V result = loader.apply(k);
                    stats.recordLoadSuccess(System.nanoTime() - startTime);
                    return result;
                } catch (Exception e) {
                    stats.recordLoadException(System.nanoTime() - startTime);
                    throw new RuntimeException(e);
                }
            });
            return value;
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        // Caffeine不直接支持单个键的TTL，这里简化处理
        return getOrLoad(key, loader);
    }

    @Override
    public void refresh() {
        // Caffeine缓存不支持整体刷新
        logger.debug("Caffeine cache does not support refresh operation");
    }

    @Override
    public void close() {
        cache.cleanUp();
    }
}