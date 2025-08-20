package com.qtech.im.cache.impl;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.CacheManager;
import com.qtech.im.cache.CacheManagerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:54:15
 * desc   :  im-common-IntelliJ IDEA
 */

/**
 * 本地缓存管理器实现
 * <p>
 * 负责管理本地缓存实例的创建、获取和销毁
 */
public class LocalCacheManager implements CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(LocalCacheManager.class);

    // 缓存实例映射
    private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    // 缓存名称集合
    private final Set<String> cacheNames = new CopyOnWriteArraySet<>();

    // 缓存管理器统计信息
    private final CacheManagerStats stats = new CacheManagerStats();

    public LocalCacheManager() {
        logger.info("LocalCacheManager initialized");
    }

    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }

        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }

        // 创建缓存实例
        Cache<K, V> cache = new CaffeineCache<>(config);
        caches.put(name, cache);
        cacheNames.add(name);

        logger.info("Cache '{}' created with config: {}", name, config);
        return cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) caches.get(name);
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config) {
        Cache<K, V> cache = getCache(name);
        if (cache == null) {
            synchronized (this) {
                cache = getCache(name);
                if (cache == null) {
                    cache = createCache(name, config);
                }
            }
        }
        return cache;
    }

    @Override
    public boolean removeCache(String name) {
        Cache<?, ?> cache = caches.remove(name);
        if (cache != null) {
            cacheNames.remove(name);
            try {
                cache.close();
                logger.info("Cache '{}' removed and closed", name);
                return true;
            } catch (Exception e) {
                logger.warn("Error closing cache '{}'", name, e);
            }
        }
        return false;
    }

    @Override
    public String[] getCacheNames() {
        return cacheNames.toArray(new String[0]);
    }

    @Override
    public CacheManagerStats getStats() {
        return stats;
    }

    @Override
    public void close() {
        // 关闭所有缓存实例
        for (Map.Entry<String, Cache<?, ?>> entry : caches.entrySet()) {
            try {
                entry.getValue().close();
                logger.info("Cache '{}' closed", entry.getKey());
            } catch (Exception e) {
                logger.warn("Error closing cache '{}'", entry.getKey(), e);
            }
        }

        caches.clear();
        cacheNames.clear();
        logger.info("LocalCacheManager closed");
    }
}
