package com.qtech.im.cache.impl.manage;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheManager;
import com.qtech.im.cache.CacheManagerStats;
import com.qtech.im.cache.builder.CacheFactory;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.support.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Default cache manager implementation
 * <p>
 * A general, standardized, flexible, fault-tolerant, and reusable cache manager
 * supporting multiple backends with thread-safe operations
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */
public class DefaultCacheManager implements CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheManager.class);

    // Cache instance map (thread-safe)
    private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    // Cache name set (thread-safe)
    private final CopyOnWriteArraySet<String> cacheNames = new CopyOnWriteArraySet<>();

    // Start time for uptime calculation
    private final long startTime = System.currentTimeMillis();

    // Cache manager statistics
    private final CacheManagerStats managerStats = new CacheManagerStats() {
        @Override
        public int getCacheCount() {
            return caches.size();
        }

        @Override
        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }

        @Override
        public CacheStats getAggregatedStats() {
            CacheStats aggregated = new CacheStats();
            caches.values().forEach(cache -> aggregated.merge(cache.getStats()));
            return aggregated;
        }
    };

    public DefaultCacheManager() {
        logger.info("DefaultCacheManager initialized");
    }

    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Cache config cannot be null");
        }
        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }

        try {
            Cache<K, V> cache = new CacheFactory(this).create(config);
            registerCache(name, cache); // 直接注册
            logger.info("Cache '{}' created with config: {}", name, config);
            return cache;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create cache '{}'", name, e);
            throw new RuntimeException("Failed to create cache: " + name, e);
        }
    }

    @Override
    public void registerCache(String name, Cache<?, ?> cache) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }
        if (cache == null) {
            throw new IllegalArgumentException("Cache instance cannot be null");
        }
        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }
        caches.put(name, cache);
        cacheNames.add(name);
        logger.info("Cache '{}' registered", name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        if (name == null) {
            return null;
        }
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
        if (name == null) {
            return false;
        }
        Cache<?, ?> cache = caches.remove(name);
        if (cache != null) {
            cacheNames.remove(name);
            try {
                cache.close();
                logger.info("Cache '{}' removed and closed", name);
                return true;
            } catch (Exception e) {
                logger.warn("Error closing cache '{}'", name, e);
                return false;
            }
        }
        return false;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheNames);
    }

    @Override
    public CacheManagerStats getStats() {
        return managerStats;
    }

    @Override
    public void close() {
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
        logger.info("DefaultCacheManager closed");
    }
}