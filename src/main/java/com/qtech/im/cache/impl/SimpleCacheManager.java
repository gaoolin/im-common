package com.qtech.im.cache.impl;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.CacheManager;
import com.qtech.im.cache.CacheManagerStats;
import com.qtech.im.cache.builder.CacheFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */
public class SimpleCacheManager implements CacheManager {
    private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    /**
     * 创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @return 缓存实例
     */
    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        Cache<K, V> cache = CacheFactory.createCache(config);
        caches.put(name, cache);
        return cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) caches.get(name);
    }

    /**
     * 获取或创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @return 缓存实例
     */
    @Override
    public <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config) {
        Cache<K, V> cache = getCache(name);
        if (cache == null) {
            cache = createCache(name, config);
        }
        return cache;
    }

    /**
     * 删除缓存实例
     *
     * @param name 缓存名称
     * @return 删除成功返回true，否则返回false
     */
    @Override
    public boolean removeCache(String name) {
        Cache<?, ?> cache = caches.remove(name);
        if (cache != null) {
            try {
                cache.close();
                return true;
            } catch (Exception e) {
                System.err.println("Error closing cache: " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }

    /**
     * 获取缓存管理器统计信息
     *
     * @return 统计信息
     */
    @Override
    public CacheManagerStats getStats() {
        // 简单实现，可以进一步完善
        return new CacheManagerStats() {
            @Override
            public int getCacheCount() {
                return caches.size();
            }

            @Override
            public long getUptime() {
                return System.currentTimeMillis();
            }
        };
    }

    /**
     * 关闭缓存管理器
     */
    @Override
    public void close() {
        caches.values().forEach(cache -> {
            try {
                cache.close();
            } catch (Exception e) {
                // 简单记录错误，不依赖日志框架
                System.err.println("Error closing cache: " + e.getMessage());
            }
        });
        caches.clear();
    }
}
