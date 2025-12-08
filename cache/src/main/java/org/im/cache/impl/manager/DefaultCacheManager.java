package org.im.cache.impl.manager;

import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.factory.CacheFactory;
import org.im.cache.stats.CacheManagerStats;
import org.im.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存管理器实现
 * <p>
 * 提供完整的缓存管理功能，支持多缓存实例管理
 * </p>
 *
 * @author gaozhilin
 * @author lingma
 */
public class DefaultCacheManager extends AbstractCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheManager.class);

    /**
     * 缓存实例映射（线程安全）
     */
    private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    /**
     * 缓存工厂
     */
    private final CacheFactory cacheFactory;

    /**
     * 启动时间戳
     */
    private final long startTime = System.currentTimeMillis();

    /**
     * 缓存管理器统计信息
     */
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

    /**
     * 默认构造函数
     */
    public DefaultCacheManager() {
        this.cacheFactory = new CacheFactory(this);
        logger.info("DefaultCacheManager initialized");
    }

    /**
     * 创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 缓存实例
     * @throws IllegalArgumentException 如果名称为空或配置为null
     * @throws IllegalStateException    如果已存在同名缓存
     */
    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config) {
        validateCacheName(name);

        if (config == null) {
            throw new IllegalArgumentException("Cache config cannot be null");
        }

        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }

        try {
            Cache<K, V> cache = cacheFactory.create(config);
            registerCache(name, cache);
            logger.info("Cache '{}' created with config: {}", name, config);
            return cache;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create cache '{}'", name, e);
            throw new RuntimeException("Failed to create cache: " + name, e);
        }
    }

    /**
     * 创建带有类型信息的缓存实例
     *
     * @param name      缓存名称
     * @param config    缓存配置
     * @param keyType   键类型
     * @param valueType 值类型
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存实例
     * @throws IllegalArgumentException 如果名称为空或配置为null
     * @throws IllegalStateException    如果已存在同名缓存
     */
    @Override
    public <K, V> Cache<K, V> createCache(String name, CacheConfig config, Class<K> keyType, Class<V> valueType) {
        validateCacheName(name);

        if (config == null) {
            throw new IllegalArgumentException("Cache config cannot be null");
        }

        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }

        try {
            Cache<K, V> cache = cacheFactory.create(config, keyType, valueType);
            registerCache(name, cache);
            logger.info("Cache '{}' created with config: {}", name, config);
            return cache;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create cache '{}'", name, e);
            throw new RuntimeException("Failed to create cache: " + name, e);
        }
    }

    /**
     * 注册缓存实例
     *
     * @param name  缓存名称
     * @param cache 缓存实例
     * @throws IllegalArgumentException 如果名称为空或缓存实例为null
     * @throws IllegalStateException    如果已存在同名缓存
     */
    @Override
    public void registerCache(String name, Cache<?, ?> cache) {
        validateCacheName(name);

        if (cache == null) {
            throw new IllegalArgumentException("Cache instance cannot be null");
        }

        if (caches.containsKey(name)) {
            throw new IllegalStateException("Cache with name '" + name + "' already exists");
        }

        caches.put(name, cache);
        cacheNames.add(name);
        recordCacheCreate();
        logger.info("Cache '{}' registered", name);
    }

    /**
     * 获取缓存实例
     *
     * @param name 缓存名称
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 缓存实例，如果不存在返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        if (name == null) {
            return null;
        }

        Cache<?, ?> cache = caches.get(name);
        recordCacheGet();

        return (Cache<K, V>) cache;
    }

    /**
     * 获取或创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 缓存实例
     */
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

    /**
     * 获取或创建带有类型信息的缓存实例
     *
     * @param name      缓存名称
     * @param config    缓存配置
     * @param keyType   键类型
     * @param valueType 值类型
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 缓存实例
     */
    @Override
    public <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config, Class<K> keyType, Class<V> valueType) {
        Cache<K, V> cache = getCache(name);
        if (cache == null) {
            synchronized (this) {
                cache = getCache(name);
                if (cache == null) {
                    cache = createCache(name, config, keyType, valueType);
                }
            }
        }
        return cache;
    }

    /**
     * 移除缓存实例
     *
     * @param name 缓存名称
     * @return 如果成功移除返回true，否则返回false
     */
    @Override
    public boolean removeCache(String name) {
        if (name == null) {
            return false;
        }

        Cache<?, ?> cache = caches.remove(name);
        if (cache != null) {
            cacheNames.remove(name);
            recordCacheRemove();

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

    /**
     * 获取缓存名称列表
     *
     * @return 缓存名称列表
     */
    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheNames);
    }

    /**
     * 获取缓存管理器统计信息
     *
     * @return 缓存管理器统计信息
     */
    @Override
    public CacheManagerStats getStats() {
        return managerStats;
    }

    /**
     * 关闭缓存管理器
     */
    @Override
    public void close() {
        logger.info("Closing DefaultCacheManager with {} caches", caches.size());

        // 关闭所有缓存实例
        for (Map.Entry<String, Cache<?, ?>> entry : caches.entrySet()) {
            try {
                entry.getValue().close();
                logger.debug("Closed cache '{}'", entry.getKey());
            } catch (Exception e) {
                logger.error("Error closing cache '{}'", entry.getKey(), e);
            }
        }

        // 清理资源
        caches.clear();
        cacheNames.clear();

        super.close();
    }
}
