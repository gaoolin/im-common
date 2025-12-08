package org.im.cache.config;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * <p>
 * 定义缓存的各种配置参数，用于控制缓存的行为和特性
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19
 */
public class CacheConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==================== 基础配置 ====================

    /**
     * 缓存名称前缀
     * <p>用于标识不同的缓存实例，便于管理和区分</p>
     */
    private String cacheName;

    /**
     * 最大缓存大小
     * <p>控制缓存中最多能存储多少个条目，超出时会根据淘汰策略进行清理</p>
     * <p>默认值: 1000</p>
     */
    private int maximumSize = 1000;

    /**
     * 写入后过期时间（毫秒）
     * <p>缓存条目在写入后经过指定时间会被自动淘汰</p>
     * <p>默认值: 30分钟</p>
     */
    private long expireAfterWrite = TimeUnit.MINUTES.toMillis(30);

    /**
     * 访问后过期时间（毫秒）
     * <p>缓存条目在最后一次访问后经过指定时间会被自动淘汰</p>
     * <p>默认值: 0（禁用）</p>
     */
    private long expireAfterAccess = 0;

    // ==================== 统计配置 ====================

    /**
     * 是否启用统计
     * <p>启用后可以收集缓存的命中率、加载时间等统计信息</p>
     * <p>默认值: true</p>
     */
    private boolean recordStats = true;

    // ==================== 缓存保护机制配置 ====================

    /**
     * 是否启用缓存穿透保护
     * <p>防止查询不存在的数据导致数据库压力过大</p>
     * <p>默认值: true</p>
     */
    private boolean enableNullValueProtection = true;

    /**
     * 缓存穿透保护的空值过期时间（毫秒）
     * <p>当查询结果为空时，将空值缓存一段时间</p>
     * <p>默认值: 1分钟</p>
     */
    private long nullValueExpireTime = TimeUnit.MINUTES.toMillis(1);

    /**
     * 是否启用缓存击穿保护
     * <p>防止热点数据失效瞬间大量请求打到数据库</p>
     * <p>默认值: true</p>
     */
    private boolean enableBreakdownProtection = true;

    /**
     * 缓存击穿保护的锁超时时间（毫秒）
     * <p>当缓存失效需要重新加载时，加锁的超时时间</p>
     * <p>默认值: 10秒</p>
     */
    private long breakdownLockTimeout = TimeUnit.SECONDS.toMillis(10);

    /**
     * 是否启用缓存雪崩保护
     * <p>防止大量缓存同时失效导致系统崩溃</p>
     * <p>默认值: true</p>
     */
    private boolean enableAvalancheProtection = true;

    /**
     * 缓存雪崩保护的随机过期时间范围（毫秒）
     * <p>为过期时间添加随机偏移量，避免大量缓存同时过期</p>
     * <p>默认值: 5分钟</p>
     */
    private long avalancheProtectionRange = TimeUnit.MINUTES.toMillis(5);

    // ==================== 缓存类型配置 ====================

    /**
     * 缓存后端类型
     * <p>指定使用的缓存实现类型</p>
     * <p>默认值: BackendType.MEMORY</p>
     */
    private BackendType backendType = BackendType.MEMORY;

    /**
     * 缓存实现类全限定名
     * <p>自定义缓存实现类的完全限定类名</p>
     */
    private String cacheImplementation;

    // ==================== 清理配置 ====================

    /**
     * 定期清理间隔（毫秒）
     * <p>自动清理过期缓存条目的时间间隔</p>
     * <p>默认值: 5分钟</p>
     */
    private long cleanupInterval = TimeUnit.MINUTES.toMillis(5);

    // ==================== Redis配置 ====================

    /**
     * Redis连接URI
     * <p>Redis服务器的连接地址</p>
     * <p>默认值: "redis://localhost:6379"</p>
     */
    private String redisUri = "redis://localhost:6379";

    /**
     * Redis连接超时时间(毫秒)
     * <p>建立Redis连接的最大等待时间</p>
     * <p>默认值: 2000毫秒</p>
     */
    private int redisConnectionTimeout = 2000;

    /**
     * Redis Socket超时时间(毫秒)
     * <p>Redis命令执行的最大等待时间</p>
     * <p>默认值: 2000毫秒</p>
     */
    private int redisSoTimeout = 2000;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public CacheConfig() {
    }

    /**
     * 带名称前缀的构造函数
     *
     * @param cacheName 缓存名称前缀
     */
    public CacheConfig(String cacheName) {
        this.cacheName = cacheName;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取定期清理间隔
     *
     * @return 清理间隔（毫秒）
     */
    public long getCleanupInterval() {
        return cleanupInterval;
    }

    /**
     * 设置定期清理间隔
     *
     * @param cleanupInterval 清理间隔（毫秒）
     */
    public void setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    /**
     * 获取缓存名称前缀
     *
     * @return 缓存名称前缀
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * 设置缓存名称前缀
     *
     * @param cacheName 缓存名称前缀
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * 获取最大缓存大小
     *
     * @return 最大缓存大小
     */
    public int getMaximumSize() {
        return maximumSize;
    }

    /**
     * 设置最大缓存大小
     *
     * @param maximumSize 最大缓存大小
     */
    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    /**
     * 获取写入后过期时间
     *
     * @return 写入后过期时间（毫秒）
     */
    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * 设置写入后过期时间
     *
     * @param expireAfterWrite 写入后过期时间（毫秒）
     */
    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    /**
     * 获取访问后过期时间
     *
     * @return 访问后过期时间（毫秒）
     */
    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * 设置访问后过期时间
     *
     * @param expireAfterAccess 访问后过期时间（毫秒）
     */
    public void setExpireAfterAccess(long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    /**
     * 是否启用统计
     *
     * @return true表示启用统计，false表示禁用
     */
    public boolean isRecordStats() {
        return recordStats;
    }

    /**
     * 设置是否启用统计
     *
     * @param recordStats true表示启用统计，false表示禁用
     */
    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    /**
     * 是否启用缓存穿透保护
     *
     * @return true表示启用缓存穿透保护，false表示禁用
     */
    public boolean isEnableNullValueProtection() {
        return enableNullValueProtection;
    }

    /**
     * 设置是否启用缓存穿透保护
     *
     * @param enableNullValueProtection true表示启用缓存穿透保护，false表示禁用
     */
    public void setEnableNullValueProtection(boolean enableNullValueProtection) {
        this.enableNullValueProtection = enableNullValueProtection;
    }

    /**
     * 获取缓存穿透保护的空值过期时间
     *
     * @return 空值过期时间（毫秒）
     */
    public long getNullValueExpireTime() {
        return nullValueExpireTime;
    }

    /**
     * 设置缓存穿透保护的空值过期时间
     *
     * @param nullValueExpireTime 空值过期时间（毫秒）
     */
    public void setNullValueExpireTime(long nullValueExpireTime) {
        this.nullValueExpireTime = nullValueExpireTime;
    }

    /**
     * 是否启用缓存击穿保护
     *
     * @return true表示启用缓存击穿保护，false表示禁用
     */
    public boolean isEnableBreakdownProtection() {
        return enableBreakdownProtection;
    }

    /**
     * 设置是否启用缓存击穿保护
     *
     * @param enableBreakdownProtection true表示启用缓存击穿保护，false表示禁用
     */
    public void setEnableBreakdownProtection(boolean enableBreakdownProtection) {
        this.enableBreakdownProtection = enableBreakdownProtection;
    }

    /**
     * 获取缓存击穿保护的锁超时时间
     *
     * @return 锁超时时间（毫秒）
     */
    public long getBreakdownLockTimeout() {
        return breakdownLockTimeout;
    }

    /**
     * 设置缓存击穿保护的锁超时时间
     *
     * @param breakdownLockTimeout 锁超时时间（毫秒）
     */
    public void setBreakdownLockTimeout(long breakdownLockTimeout) {
        this.breakdownLockTimeout = breakdownLockTimeout;
    }

    /**
     * 是否启用缓存雪崩保护
     *
     * @return true表示启用缓存雪崩保护，false表示禁用
     */
    public boolean isEnableAvalancheProtection() {
        return enableAvalancheProtection;
    }

    /**
     * 设置是否启用缓存雪崩保护
     *
     * @param enableAvalancheProtection true表示启用缓存雪崩保护，false表示禁用
     */
    public void setEnableAvalancheProtection(boolean enableAvalancheProtection) {
        this.enableAvalancheProtection = enableAvalancheProtection;
    }

    /**
     * 获取缓存雪崩保护的随机过期时间范围
     *
     * @return 随机过期时间范围（毫秒）
     */
    public long getAvalancheProtectionRange() {
        return avalancheProtectionRange;
    }

    /**
     * 设置缓存雪崩保护的随机过期时间范围
     *
     * @param avalancheProtectionRange 随机过期时间范围（毫秒）
     */
    public void setAvalancheProtectionRange(long avalancheProtectionRange) {
        this.avalancheProtectionRange = avalancheProtectionRange;
    }

    /**
     * 获取缓存后端类型
     *
     * @return 缓存后端类型
     */
    public BackendType getBackendType() {
        return backendType;
    }

    /**
     * 设置缓存后端类型
     *
     * @param backendType 缓存后端类型
     */
    public void setBackendType(BackendType backendType) {
        this.backendType = backendType;
    }

    /**
     * 获取缓存实现类全限定名
     *
     * @return 缓存实现类全限定名
     */
    public String getCacheImplementation() {
        return cacheImplementation;
    }

    /**
     * 设置缓存实现类全限定名
     *
     * @param cacheImplementation 缓存实现类全限定名
     */
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

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "CacheConfig{" +
                "cacheName='" + cacheName + '\'' +
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
