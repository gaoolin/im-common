package org.im.cache.builder;

import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.impl.cache.CaffeineCache;
import org.im.cache.impl.cache.RedisCache;
import org.im.cache.impl.cache.SimpleMemoryCache;
import org.im.cache.impl.support.BackendType;

import java.util.concurrent.TimeUnit;

/**
 * 缓存构建器
 * <p>
 * 提供链式调用方式创建缓存实例。支持多种缓存后端类型，包括内存缓存、Caffeine缓存和Redis缓存。
 * 通过流畅的API设计，可以方便地配置缓存的各种参数，如最大大小、过期时间等。
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * Cache<String, String> cache = CacheBuilder.newBuilder()
 *     .name("myCache")
 *     .maximumSize(1000)
 *     .expireAfterWrite(10, TimeUnit.MINUTES)
 *     .cacheType(BackendType.CAFFEINE)
 *     .build();
 * </pre>
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/27
 */
public class CacheBuilder {
    private final CacheConfig config;

    /**
     * 私有构造函数，初始化默认配置
     */
    private CacheBuilder() {
        this.config = new CacheConfig();
    }

    /**
     * 创建一个新的缓存构建器实例
     *
     * @return 新的缓存构建器实例
     */
    public static CacheBuilder newBuilder() {
        return new CacheBuilder();
    }

    /**
     * 设置缓存名称
     *
     * @param name 缓存名称
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder name(String name) {
        config.setName(name);
        return this;
    }

    /**
     * 设置缓存最大大小
     *
     * @param size 缓存最大条目数
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder maximumSize(int size) {
        config.setMaximumSize(size);
        return this;
    }

    /**
     * 设置写入后过期时间
     *
     * @param duration 过期时间长度
     * @param unit     时间单位
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder expireAfterWrite(long duration, TimeUnit unit) {
        config.setExpireAfterWrite(unit.toMillis(duration));
        return this;
    }

    /**
     * 设置访问后过期时间
     *
     * @param duration 过期时间长度
     * @param unit     时间单位
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder expireAfterAccess(long duration, TimeUnit unit) {
        config.setExpireAfterAccess(unit.toMillis(duration));
        return this;
    }

    /**
     * 设置是否记录统计信息
     *
     * @param record true表示记录统计信息，false表示不记录
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder recordStats(boolean record) {
        config.setRecordStats(record);
        return this;
    }

    /**
     * 设置缓存后端类型
     *
     * @param type 缓存后端类型
     * @return 当前构建器实例，用于链式调用
     */
    public CacheBuilder cacheType(BackendType type) {
        config.setBackendType(type);
        return this;
    }

    /**
     * 构建并返回缓存实例
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 构建的缓存实例
     */
    public <K, V> Cache<K, V> build() {
        switch (config.getBackendType()) {
            case MEMORY:
                return new SimpleMemoryCache<>(config);
            case CAFFEINE:
                return new CaffeineCache<>(config);
            case REDIS:
                // 可以返回默认的分布式实现或抛出异常提示用户自定义实现
                return new RedisCache<>(config);
            case HYBRID:
                throw new UnsupportedOperationException("Hybrid cache not implemented");
            default:
                return new CaffeineCache<>(config);
        }
    }
}
