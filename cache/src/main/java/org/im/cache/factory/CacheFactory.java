package org.im.cache.factory;

import org.im.cache.builder.CacheConfigBuilder;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.core.CacheManager;
import org.im.cache.impl.cache.CaffeineCache;
import org.im.cache.impl.cache.ProtectedCache;
import org.im.cache.impl.cache.RedisCache;
import org.im.cache.impl.cache.SimpleMemoryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.im.cache.impl.support.BackendType.MEMORY;

/**
 * Cache factory for creating cache instances based on configuration.
 * <p>
 * Use instance methods to create caches with a specific CacheManager. For default MEMORY cache,
 * call {@code createDefault()}. For custom configurations, call {@code create(CacheConfig)}.
 * Cache registration is handled by the CacheManager.
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */
public class CacheFactory {
    private static final Logger logger = LoggerFactory.getLogger(CacheFactory.class);

    private final CacheManager cacheManager;

    /**
     * Constructor with injectable CacheManager
     *
     * @param cacheManager The cache manager to manage created caches
     * @throws IllegalArgumentException if cacheManager is null
     */
    public CacheFactory(CacheManager cacheManager) {
        if (cacheManager == null) {
            throw new IllegalArgumentException("CacheManager cannot be null");
        }
        this.cacheManager = cacheManager;
        logger.info("CacheFactory initialized with CacheManager: {}", cacheManager.getClass().getSimpleName());
    }

    /**
     * Create a cache instance based on the provided configuration
     *
     * @param config Cache configuration
     * @param <K>    Key type
     * @param <V>    Value type
     * @return Configured cache instance
     * @throws IllegalArgumentException if config is invalid
     * @throws RuntimeException         if cache creation fails
     */
    public <K, V> Cache<K, V> create(CacheConfig config) {
        if (config == null) {
            logger.warn("CacheConfig is null, using default MEMORY config");
            config = CacheConfigBuilder.create().withBackendType(MEMORY).build();
        }

        Cache<K, V> cache;
        try {
            switch (config.getBackendType()) {
                case MEMORY:
                    cache = new SimpleMemoryCache<>(config);
                    break;
                case CAFFEINE:
                    cache = new CaffeineCache<>(config);
                    break;
                case REDIS:
                    cache = new RedisCache<>(config);
                    break;
                case HYBRID:
                    throw new UnsupportedOperationException("Hybrid cache not implemented");
                default:
                    logger.warn("Unknown BackendType {}, defaulting to MEMORY", config.getBackendType());
                    cache = new SimpleMemoryCache<>(config);
                    break;
            }
            logger.info("Created cache with backend: {} for config: {}", config.getBackendType(), config.getName());
        } catch (Exception e) {
            logger.error("Failed to create cache with backend: {}", config.getBackendType(), e);
            throw new RuntimeException("Failed to create cache for backend: " + config.getBackendType(), e);
        }

        // Apply protection mechanisms
        if (config.isEnableNullValueProtection() ||
                config.isEnableBreakdownProtection() ||
                config.isEnableAvalancheProtection()) {
            cache = new ProtectedCache<>(cache, config);
            logger.debug("Applied ProtectedCache wrapper to cache: {}", config.getName());
        }

        return cache; // 不负责注册，交给 CacheManager
    }

    /**
     * Create a cache with default MEMORY backend
     *
     * @param <K> Key type
     * @param <V> Value type
     * @return Default cache instance
     */
    public <K, V> Cache<K, V> createDefault() {
        CacheConfig config = CacheConfigBuilder.create().withBackendType(MEMORY).build();
        return create(config);
    }
}