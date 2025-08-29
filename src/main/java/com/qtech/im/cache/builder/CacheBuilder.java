package com.qtech.im.cache.builder;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.impl.cache.CaffeineCache;
import com.qtech.im.cache.impl.cache.SimpleMemoryCache;
import com.qtech.im.cache.support.BackendType;

import java.util.concurrent.TimeUnit;

/**
 * 缓存构建器
 * <p>
 * 提供链式调用方式创建缓存实例
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/27
 */

public class CacheBuilder {
    private final CacheConfig config;

    private CacheBuilder() {
        this.config = new CacheConfig();
    }

    public static CacheBuilder newBuilder() {
        return new CacheBuilder();
    }

    public CacheBuilder name(String name) {
        config.setName(name);
        return this;
    }

    public CacheBuilder maximumSize(int size) {
        config.setMaximumSize(size);
        return this;
    }

    public CacheBuilder expireAfterWrite(long duration, TimeUnit unit) {
        config.setExpireAfterWrite(unit.toMillis(duration));
        return this;
    }

    public CacheBuilder expireAfterAccess(long duration, TimeUnit unit) {
        config.setExpireAfterAccess(unit.toMillis(duration));
        return this;
    }

    public CacheBuilder recordStats(boolean record) {
        config.setRecordStats(record);
        return this;
    }

    public CacheBuilder cacheType(BackendType type) {
        config.setBackendType(type);
        return this;
    }

    public <K, V> Cache<K, V> build() {
        switch (config.getBackendType()) {
            case MEMORY:
                return new SimpleMemoryCache<>(config);
            case CAFFEINE:
                return new CaffeineCache<>(config);
            case REDIS:
                // 可以返回默认的分布式实现或抛出异常提示用户自定义实现
                throw new UnsupportedOperationException("Distributed cache not implemented");
            case HYBRID:
                throw new UnsupportedOperationException("Hybrid cache not implemented");
            default:
                return new CaffeineCache<>(config);
        }
    }
}
