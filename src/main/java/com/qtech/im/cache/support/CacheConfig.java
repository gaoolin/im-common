package com.qtech.im.cache.support;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * <p>
 * 定义缓存的各种配置参数
 * 1. 缓存名称
 * 2. 最大缓存大小，默认1000
 * 3. 缓存过期时间，默认30分钟
 * 4. 缓存统计，默认开启
 * 5. 缓存穿透保护，默认开启
 * 6. 缓存击穿保护，默认开启
 * 7. 缓存雪崩保护，默认开启
 * 8. 缓存类型，默认内存
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */

public class CacheConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // 缓存名称
    private String name;

    // 最大缓存大小
    private int maximumSize = 1000;

    // 默认过期时间（毫秒）
    private long expireAfterWrite = TimeUnit.MINUTES.toMillis(30);

    // 访问后过期时间（毫秒）
    private long expireAfterAccess = 0;

    // 是否启用统计
    private boolean recordStats = true;

    // 是否启用缓存穿透保护
    private boolean enableNullValueProtection = true;

    // 缓存穿透保护的空值过期时间（毫秒）
    private long nullValueExpireTime = TimeUnit.MINUTES.toMillis(1);

    // 是否启用缓存击穿保护
    private boolean enableBreakdownProtection = true;

    // 缓存击穿保护的锁超时时间（毫秒）
    private long breakdownLockTimeout = TimeUnit.SECONDS.toMillis(10);

    // 是否启用缓存雪崩保护
    private boolean enableAvalancheProtection = true;

    // 缓存雪崩保护的随机过期时间范围（毫秒）
    private long avalancheProtectionRange = TimeUnit.MINUTES.toMillis(5);

    // 缓存类型（本地、分布式等）
    private BackendType backendType = BackendType.MEMORY;

    // 缓存实现类
    private String cacheImplementation;

    // 定期清理间隔（毫秒），默认5分钟
    private long cleanupInterval = TimeUnit.MINUTES.toMillis(5);
    // Redis相关配置
    private String redisUri = "redis://localhost:6379";
    private int redisConnectionTimeout = 2000; // 连接超时时间(毫秒)
    private int redisSoTimeout = 2000; // Socket超时时间(毫秒)

    public CacheConfig() {
    }

    public CacheConfig(String name) {
        this.name = name;
    }

    public long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    public boolean isEnableNullValueProtection() {
        return enableNullValueProtection;
    }

    public void setEnableNullValueProtection(boolean enableNullValueProtection) {
        this.enableNullValueProtection = enableNullValueProtection;
    }

    public long getNullValueExpireTime() {
        return nullValueExpireTime;
    }

    public void setNullValueExpireTime(long nullValueExpireTime) {
        this.nullValueExpireTime = nullValueExpireTime;
    }

    public boolean isEnableBreakdownProtection() {
        return enableBreakdownProtection;
    }

    public void setEnableBreakdownProtection(boolean enableBreakdownProtection) {
        this.enableBreakdownProtection = enableBreakdownProtection;
    }

    public long getBreakdownLockTimeout() {
        return breakdownLockTimeout;
    }

    public void setBreakdownLockTimeout(long breakdownLockTimeout) {
        this.breakdownLockTimeout = breakdownLockTimeout;
    }

    public boolean isEnableAvalancheProtection() {
        return enableAvalancheProtection;
    }

    public void setEnableAvalancheProtection(boolean enableAvalancheProtection) {
        this.enableAvalancheProtection = enableAvalancheProtection;
    }

    public long getAvalancheProtectionRange() {
        return avalancheProtectionRange;
    }

    public void setAvalancheProtectionRange(long avalancheProtectionRange) {
        this.avalancheProtectionRange = avalancheProtectionRange;
    }

    public BackendType getBackendType() {
        return backendType;
    }

    public void setBackendType(BackendType backendType) {
        this.backendType = backendType;
    }

    public String getCacheImplementation() {
        return cacheImplementation;
    }

    public void setCacheImplementation(String cacheImplementation) {
        this.cacheImplementation = cacheImplementation;
    }

    /**
     * 获取Redis连接URI
     *
     * @return Redis连接URI
     */
    public String getRedisUri() {
        return redisUri;
    }

    /**
     * 设置Redis连接URI
     *
     * @param redisUri Redis连接URI
     * @return CacheConfig实例
     */
    public CacheConfig setRedisUri(String redisUri) {
        this.redisUri = redisUri;
        return this;
    }

    /**
     * 获取Redis连接超时时间
     *
     * @return 连接超时时间(毫秒)
     */
    public int getRedisConnectionTimeout() {
        return redisConnectionTimeout;
    }

    /**
     * 设置Redis连接超时时间
     *
     * @param redisConnectionTimeout 连接超时时间(毫秒)
     * @return CacheConfig实例
     */
    public CacheConfig setRedisConnectionTimeout(int redisConnectionTimeout) {
        this.redisConnectionTimeout = redisConnectionTimeout;
        return this;
    }

    /**
     * 获取Redis Socket超时时间
     *
     * @return Socket超时时间(毫秒)
     */
    public int getRedisSoTimeout() {
        return redisSoTimeout;
    }

    /**
     * 设置Redis Socket超时时间
     *
     * @param redisSoTimeout Socket超时时间(毫秒)
     * @return CacheConfig实例
     */
    public CacheConfig setRedisSoTimeout(int redisSoTimeout) {
        this.redisSoTimeout = redisSoTimeout;
        return this;
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "name='" + name + '\'' +
                ", maximumSize=" + maximumSize +
                ", expireAfterWrite=" + expireAfterWrite +
                ", expireAfterAccess=" + expireAfterAccess +
                ", recordStats=" + recordStats +
                ", enableNullValueProtection=" + enableNullValueProtection +
                ", nullValueExpireTime=" + nullValueExpireTime +
                ", enableBreakdownProtection=" + enableBreakdownProtection +
                ", breakdownLockTimeout=" + breakdownLockTimeout +
                ", enableAvalancheProtection=" + enableAvalancheProtection +
                ", avalancheProtectionRange=" + avalancheProtectionRange +
                ", backendType=" + backendType +
                ", cacheImplementation='" + cacheImplementation + '\'' +
                ", cleanupInterval=" + cleanupInterval +
                ", redisUri='" + redisUri + '\'' +
                ", redisConnectionTimeout=" + redisConnectionTimeout +
                ", redisSoTimeout=" + redisSoTimeout +
                '}';
    }
}