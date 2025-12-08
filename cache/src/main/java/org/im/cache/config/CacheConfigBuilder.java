package org.im.cache.config;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置构建器
 * <p>
 * 提供流畅的API用于构建和配置CacheConfig实例
 * 使用建造者模式，支持链式调用
 * </p>
 *
 * @author gaozhilin
 * @author lingma
 * @email gaoolin@gmail.com
 * @date 2025/08/27
 */
public class CacheConfigBuilder {
    /**
     * 要构建的缓存配置实例
     */
    private final CacheConfig config;

    /**
     * 私有构造函数
     * <p>初始化一个新的CacheConfig实例用于构建</p>
     */
    private CacheConfigBuilder() {
        this.config = new CacheConfig();
    }

    /**
     * 创建一个新的缓存配置构建器实例
     *
     * @return 新的CacheConfigBuilder实例
     */
    public static CacheConfigBuilder newBuilder() {
        return new CacheConfigBuilder();
    }

    /**
     * 设置缓存名称前缀
     *
     * @param cacheName 缓存名称前缀
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setCacheName(String)
     */
    public CacheConfigBuilder cacheName(String cacheName) {
        config.setCacheName(cacheName);
        return this;
    }

    /**
     * 设置最大缓存大小
     *
     * @param maximumSize 最大缓存大小
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setMaximumSize(int)
     */
    public CacheConfigBuilder maximumSize(int maximumSize) {
        config.setMaximumSize(maximumSize);
        return this;
    }

    /**
     * 设置写入后过期时间
     *
     * @param duration 过期时间长度
     * @param unit     时间单位
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setExpireAfterWrite(long)
     */
    public CacheConfigBuilder expireAfterWrite(long duration, TimeUnit unit) {
        config.setExpireAfterWrite(unit.toMillis(duration));
        return this;
    }

    /**
     * 设置访问后过期时间
     *
     * @param duration 过期时间长度
     * @param unit     时间单位
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setExpireAfterAccess(long)
     */
    public CacheConfigBuilder expireAfterAccess(long duration, TimeUnit unit) {
        config.setExpireAfterAccess(unit.toMillis(duration));
        return this;
    }

    /**
     * 设置是否启用统计
     *
     * @param recordStats true表示启用统计，false表示禁用
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setRecordStats(boolean)
     */
    public CacheConfigBuilder recordStats(boolean recordStats) {
        config.setRecordStats(recordStats);
        return this;
    }

    /**
     * 设置是否启用缓存穿透保护
     *
     * @param enable true表示启用缓存穿透保护，false表示禁用
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setEnableNullValueProtection(boolean)
     */
    public CacheConfigBuilder nullValueProtection(boolean enable) {
        config.setEnableNullValueProtection(enable);
        return this;
    }

    /**
     * 设置是否启用缓存击穿保护
     *
     * @param enable true表示启用缓存击穿保护，false表示禁用
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setEnableBreakdownProtection(boolean)
     */
    public CacheConfigBuilder breakdownProtection(boolean enable) {
        config.setEnableBreakdownProtection(enable);
        return this;
    }

    /**
     * 设置是否启用缓存雪崩保护
     *
     * @param enable true表示启用缓存雪崩保护，false表示禁用
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setEnableAvalancheProtection(boolean)
     */
    public CacheConfigBuilder avalancheProtection(boolean enable) {
        config.setEnableAvalancheProtection(enable);
        return this;
    }

    /**
     * 设置缓存后端类型
     *
     * @param backendType 缓存后端类型
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setBackendType(BackendType)
     */
    public CacheConfigBuilder backendType(BackendType backendType) {
        config.setBackendType(backendType);
        return this;
    }

    /**
     * 设置定期清理间隔
     *
     * @param interval 清理间隔
     * @param unit     时间单位
     * @return 当前构建器实例，用于链式调用
     * @see CacheConfig#setCleanupInterval(long)
     */
    public CacheConfigBuilder cleanupInterval(long interval, TimeUnit unit) {
        config.setCleanupInterval(unit.toMillis(interval));
        return this;
    }

    /**
     * 构建并返回配置好的CacheConfig实例
     *
     * @return 配置完成的CacheConfig实例
     */
    public CacheConfig build() {
        return config;
    }
}
