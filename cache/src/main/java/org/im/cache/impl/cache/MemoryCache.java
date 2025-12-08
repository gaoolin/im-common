package org.im.cache.impl.cache;

import org.im.cache.config.CacheConfig;
import org.im.cache.support.ExpiringValue;
import org.im.cache.util.CacheUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于内存的简单缓存实现
 * <p>
 * 提供基本的内存缓存功能，支持过期时间和大小限制
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/08
 */
public class MemoryCache<K, V> extends AbstractCache<K, V> {

    /**
     * 缓存数据存储
     */
    private final Map<K, ExpiringValue<Object>> cache = new ConcurrentHashMap<>();

    /**
     * 定期清理调度器
     */
    private ScheduledExecutorService cleanupScheduler;

    /**
     * 构造函数
     *
     * @param config 缓存配置
     */
    public MemoryCache(CacheConfig config) {
        super(config);

        // 如果配置了清理间隔，则启动定期清理任务
        if (config.getCleanupInterval() > 0) {
            this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
            this.cleanupScheduler.scheduleWithFixedDelay(
                    this::cleanup,
                    config.getCleanupInterval(),
                    config.getCleanupInterval(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 清理过期数据
     */
    private void cleanup() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> {
            ExpiringValue<Object> expiringValue = entry.getValue();
            if (expiringValue.isExpired()) {
                stats.recordEviction();
                return true;
            }
            return false;
        });
    }

    /**
     * 获取数据
     *
     * @param key 键
     * @return 值
     */
    @Override
    protected V do_get(K key) {
        ExpiringValue<Object> expiringValue = cache.get(key);
        if (expiringValue == null) {
            stats.recordMiss();
            return null;
        }

        if (expiringValue.isExpired()) {
            cache.remove(key);
            stats.recordMiss();
            stats.recordEviction();
            return null;
        }

        stats.recordHit();
        return CacheUtils.unwrapNullValue(expiringValue.getValue());
    }

    /**
     * 存储数据
     *
     * @param key   键
     * @param value 值
     */
    @Override
    protected void do_put(K key, V value) {
        long expireTime = calculateExpireTime(config.getExpireAfterWrite());
        ExpiringValue<Object> expiringValue = new ExpiringValue<>(
                CacheUtils.wrapNullValue(value), expireTime);
        cache.put(key, expiringValue);
        stats.recordPut();

        // 检查大小限制
        evictIfNeeded();
    }

    /**
     * 删除数据
     *
     * @param key 键
     * @return 是否删除成功
     */
    @Override
    protected boolean do_remove(K key) {
        return cache.remove(key) != null;
    }

    /**
     * 批量获取数据
     *
     * @param keys 键集合
     * @return 键值对映射
     */
    @Override
    protected Map<K, V> do_getAll(Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 批量存储数据
     *
     * @param map 键值对映射
     */
    @Override
    protected void do_putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 批量删除数据
     *
     * @param keys 键集合
     */
    @Override
    protected void do_removeAll(Set<? extends K> keys) {
        for (K key : keys) {
            remove(key);
        }
    }

    /**
     * 存储数据并指定过期时间
     *
     * @param key   键
     * @param value 值
     * @param ttl   过期时间
     * @param unit  时间单位
     */
    @Override
    protected void do_putWithTtl(K key, V value, long ttl, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(ttl);
        ExpiringValue<Object> expiringValue = new ExpiringValue<>(
                CacheUtils.wrapNullValue(value), expireTime);
        cache.put(key, expiringValue);
        stats.recordPut();

        // 检查大小限制
        evictIfNeeded();
    }

    /**
     * 计算过期时间
     *
     * @param baseExpireTime 基础过期时间
     * @return 计算后的过期时间
     */
    private long calculateExpireTime(long baseExpireTime) {
        if (baseExpireTime <= 0) {
            return Long.MAX_VALUE; // 永不过期
        }

        long expireTime = System.currentTimeMillis() + baseExpireTime;

        // 如果启用了雪崩保护，添加随机偏移
        if (config.isEnableAvalancheProtection() && config.getAvalancheProtectionRange() > 0) {
            long randomOffset = (long) (Math.random() * config.getAvalancheProtectionRange() * 2)
                    - config.getAvalancheProtectionRange();
            expireTime += randomOffset;
        }

        return Math.max(System.currentTimeMillis(), expireTime);
    }

    /**
     * 根据大小限制驱逐数据
     */
    private void evictIfNeeded() {
        if (config.getMaximumSize() > 0 && cache.size() > config.getMaximumSize()) {
            // 简单的LRU驱逐策略：随机移除一些条目
            int excess = (int) (cache.size() - config.getMaximumSize());
            if (excess > 0) {
                Set<K> keysToRemove = new HashSet<>();
                int count = 0;

                for (K key : cache.keySet()) {
                    if (count++ >= excess) {
                        break;
                    }
                    keysToRemove.add(key);
                }

                for (K key : keysToRemove) {
                    cache.remove(key);
                    stats.recordEviction();
                }
            }
        }
    }

    /**
     * 设置缓存值并指定绝对过期时间戳
     *
     * @param key             缓存键
     * @param value           缓存值
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     */
    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {

    }

    /**
     * 判断缓存项是否存在
     *
     * @param key 缓存键
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean containsKey(K key) {
        return false;
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    @Override
    public long size() {
        return cache.size();
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    @Override
    public CacheConfig getConfig() {
        return null;
    }

    /**
     * 获取或加载缓存值（带自动加载机制和绝对过期时间）
     *
     * @param key             缓存键
     * @param loader          加载函数
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     * @return 缓存值
     */
    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        return null;
    }

    /**
     * 关闭缓存
     */
    @Override
    public void close() {
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
        }
        cache.clear();
    }

    /**
     * 手动触发缓存清理
     */
    @Override
    public void cleanUp() {

    }
}

