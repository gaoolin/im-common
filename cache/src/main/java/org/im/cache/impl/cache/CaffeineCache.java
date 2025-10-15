package org.im.cache.impl.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.impl.support.ExpiringValue;
import org.im.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于Caffeine的本地缓存实现
 * <p>
 * 提供高性能的本地缓存功能，支持过期策略、大小限制、统计等特性
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19
 */
public class CaffeineCache<K, V> implements Cache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);

    private final com.github.benmanes.caffeine.cache.Cache<K, Object> cache; // 使用Object以支持ExpiringValue
    private final CacheConfig config;
    private final CacheStats stats = new CacheStats();
    private ScheduledExecutorService cleanupScheduler;

    /**
     * 构造函数
     *
     * @param config 缓存配置
     */
    public CaffeineCache(CacheConfig config) {
        this.config = config;
        this.cache = buildCache(config);

        // 如果启用了统计或设置了清理间隔，则启动定期清理任务
        if (config.isRecordStats() || config.getCleanupInterval() > 0) {
            startPeriodicCleanup();
        }
    }

    /**
     * 构建Caffeine缓存实例
     *
     * @param config 缓存配置
     * @return Caffeine缓存实例
     */
    private com.github.benmanes.caffeine.cache.Cache<K, Object> buildCache(CacheConfig config) {
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
            Object value = cache.getIfPresent(key);

            // 处理过期值包装器
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked")
                ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (expiringValue.isExpired()) {
                    // 过期了，清理并记录未命中
                    cache.invalidate(key);
                    stats.recordMiss();
                    return null;
                }

                stats.recordHit();
                return expiringValue.getValue();
            } else {
                @SuppressWarnings("unchecked")
                V typedValue = (V) value;

                if (typedValue != null) {
                    stats.recordHit();
                } else {
                    stats.recordMiss();
                }
                return typedValue;
            }
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
        try {
            long expireTimestamp = System.currentTimeMillis() + unit.toMillis(ttl);
            ExpiringValue<V> expiringValue = new ExpiringValue<>(value, expireTimestamp);
            cache.put(key, expiringValue);
        } catch (Exception e) {
            logger.warn("Error putting value to cache for key: {}", key, e);
        }
    }

    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        try {
            ExpiringValue<V> expiringValue = new ExpiringValue<>(value, expireTimestamp);
            cache.put(key, expiringValue);
        } catch (Exception e) {
            logger.warn("Error putting value to cache for key: {}", key, e);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        try {
            Map<K, Object> rawValues = cache.getAllPresent(keys);
            Map<K, V> result = new HashMap<>();

            for (Map.Entry<K, Object> entry : rawValues.entrySet()) {
                K key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof ExpiringValue) {
                    @SuppressWarnings("unchecked")
                    ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                    if (expiringValue.isExpired()) {
                        // 过期了，清理
                        cache.invalidate(key);
                    } else {
                        result.put(key, expiringValue.getValue());
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    V typedValue = (V) value;
                    result.put(key, typedValue);
                }
            }

            return result;
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
            // 先检查键是否存在
            boolean existed = cache.getIfPresent(key) != null;
            // 执行删除操作
            cache.invalidate(key);
            // 返回删除前是否存在该键
            return existed;
        } catch (Exception e) {
            logger.warn("Error removing value from cache for key: {}", key, e);
            return false;
        }
    }

    @Override
    public int removeAll(Set<? extends K> keys) {
        try {
            // 计算实际删除的条目数
            int count = 0;
            for (K key : keys) {
                if (cache.getIfPresent(key) != null) {
                    count++;
                }
            }
            cache.invalidateAll(keys);
            return count;
        } catch (Exception e) {
            logger.warn("Error removing values from cache for keys: {}", keys, e);
            return 0;
        }
    }

    @Override
    public boolean containsKey(K key) {
        Object value = cache.getIfPresent(key);
        if (value instanceof ExpiringValue) {
            @SuppressWarnings("unchecked")
            ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;
            if (expiringValue.isExpired()) {
                // 过期了，清理并返回false
                cache.invalidate(key);
                return false;
            }
        }
        return value != null;
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
    public CacheStats getStats() {
        if (config.isRecordStats()) {
            com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = cache.stats();
            CacheStats caffeineStatsWrapper = new CacheStats(caffeineStats);
            // 如果需要合并本地统计和Caffeine统计
            return stats.merge(caffeineStatsWrapper);
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
            Object value = cache.getIfPresent(key);

            // 处理过期值包装器
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked")
                ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (expiringValue.isExpired()) {
                    // 过期了，重新加载
                    cache.invalidate(key);
                } else {
                    stats.recordHit();
                    return expiringValue.getValue();
                }
            } else if (value != null) {
                @SuppressWarnings("unchecked")
                V typedValue = (V) value;
                stats.recordHit();
                return typedValue;
            }

            // 缓存未命中或已过期，加载新值
            long startTime = System.nanoTime();
            try {
                V result = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                cache.put(key, result);
                return result;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        try {
            Object value = cache.getIfPresent(key);

            // 处理过期值包装器
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked")
                ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (!expiringValue.isExpired()) {
                    stats.recordHit();
                    return expiringValue.getValue();
                }
            } else if (value != null) {
                @SuppressWarnings("unchecked")
                V typedValue = (V) value;
                stats.recordHit();
                // 对于普通值，我们不应用ttl，直接返回
                return typedValue;
            }

            // 缓存未命中或已过期，加载新值
            long startTime = System.nanoTime();
            try {
                V result = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                put(key, result, ttl, unit); // 使用带ttl的put方法
                return result;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        try {
            Object value = cache.getIfPresent(key);

            // 处理过期值包装器
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked")
                ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (!expiringValue.isExpired()) {
                    stats.recordHit();
                    return expiringValue.getValue();
                }
            } else if (value != null) {
                @SuppressWarnings("unchecked")
                V typedValue = (V) value;
                stats.recordHit();
                // 对于普通值，我们不应用绝对过期时间，直接返回
                return typedValue;
            }

            // 缓存未命中或已过期，加载新值
            long startTime = System.nanoTime();
            try {
                V result = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                putAtFixedTime(key, result, expireTimestamp); // 使用带绝对过期时间的put方法
                return result;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void refresh() {
        // Caffeine缓存不支持整体刷新
        logger.debug("Caffeine cache does not support refresh operation");
    }

    @Override
    public void close() {
        cache.cleanUp();
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
        }
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    /**
     * 启动定期清理任务
     */
    private void startPeriodicCleanup() {
        cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "caffeine-cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        long interval = config.getCleanupInterval() > 0 ?
                config.getCleanupInterval() :
                Math.max(1, config.getExpireAfterWrite() / 2); // 默认间隔为过期时间的一半

        cleanupScheduler.scheduleWithFixedDelay(
                this::cleanUp,
                interval,
                interval,
                TimeUnit.MILLISECONDS
        );
    }
}
