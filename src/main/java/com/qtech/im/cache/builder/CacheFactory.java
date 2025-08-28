package com.qtech.im.cache.builder;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.CacheManager;
import com.qtech.im.cache.impl.CaffeineCache;
import com.qtech.im.cache.impl.ProtectedCache;
import com.qtech.im.cache.impl.RedisCache;
import com.qtech.im.cache.impl.SimpleCacheManager;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */
public class CacheFactory {
    private static final CacheManager cacheManager = new SimpleCacheManager();

    public static <K, V> Cache<K, V> createCache(CacheConfig config) {
        Cache<K, V> cache;
        switch (config.getCacheType()) {
            case LOCAL:
                cache = new CaffeineCache<>(config);
                break;
            case DISTRIBUTED:
                // 使用Redis实现分布式缓存
                cache = new RedisCache<>(config);
                break;
            case HYBRID:
                throw new UnsupportedOperationException("Please provide hybrid cache implementation");
            default:
                cache = new CaffeineCache<>(config);
        }

        // 应用保护机制
        if (config.isEnableNullValueProtection() ||
                config.isEnableBreakdownProtection() ||
                config.isEnableAvalancheProtection()) {
            cache = new ProtectedCache<>(cache, config);
        }

        // 注册到管理器 - 使用接口中已有的方法
        if (config.getName() != null) {
            cacheManager.createCache(config.getName(), config);
        }

        return cache;
    }
}