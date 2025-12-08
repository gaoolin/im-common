package org.im.cache.impl.cache;

import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.stats.CacheStats;
import org.im.cache.util.ValidationUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 抽象缓存实现类
 * <p>
 * 提供缓存实现的基础功能
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/08
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    /**
     * 缓存配置
     */
    protected final CacheConfig config;

    /**
     * 缓存统计信息
     */
    protected final CacheStats stats = new CacheStats();

    /**
     * 构造函数
     *
     * @param config 缓存配置
     */
    protected AbstractCache(CacheConfig config) {
        ValidationUtils.checkNotNull(config, "Cache config cannot be null");
        this.config = config;
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @Override
    public CacheStats getStats() {
        return stats;
    }

    /**
     * 获取或加载数据
     *
     * @param key    键
     * @param loader 加载函数
     * @return 值
     */
    @Override
    public V getOrLoad(K key, Function<K, V> loader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        long startTime = System.nanoTime();
        try {
            value = loader.apply(key);
            if (value != null) {
                put(key, value);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
            } else {
                stats.recordLoadException(System.nanoTime() - startTime);
            }
            return value;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            throw e;
        }
    }

    /**
     * 获取或加载数据并设置过期时间
     *
     * @param key    键
     * @param loader 加载函数
     * @param ttl    过期时间
     * @param unit   时间单位
     * @return 值
     */
    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        long startTime = System.nanoTime();
        try {
            value = loader.apply(key);
            if (value != null) {
                put(key, value, ttl, unit);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
            } else {
                stats.recordLoadException(System.nanoTime() - startTime);
            }
            return value;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            throw e;
        }
    }

    /**
     * 批量获取数据
     *
     * @param keys 键集合
     * @return 键值对映射
     */
    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        ValidationUtils.checkNotNull(keys, "Keys cannot be null");
        return do_getAll(keys);
    }

    /**
     * 批量存储数据
     *
     * @param map 键值对映射
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        ValidationUtils.checkNotNull(map, "Map cannot be null");
        do_putAll(map);
    }

    /**
     * 批量删除数据
     *
     * @param keys 键集合
     * @return
     */
    @Override
    public int removeAll(Set<? extends K> keys) {
        ValidationUtils.checkNotNull(keys, "Keys cannot be null");
        do_removeAll(keys);
        return keys.size();
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
    public void put(K key, V value, long ttl, TimeUnit unit) {
        ValidationUtils.checkNotNull(key, "Key cannot be null");
        do_putWithTtl(key, value, ttl, unit);
    }

    /**
     * 刷新缓存
     */
    @Override
    public void refresh() {
        // 默认实现为空，子类可重写
    }

    /**
     * 关闭缓存
     */
    @Override
    public void close() {
        // 默认实现为空，子类可重写
    }

    // ==================== 抽象方法 ====================

    /**
     * 获取数据（抽象方法）
     *
     * @param key 键
     * @return 值
     */
    protected abstract V do_get(K key);

    /**
     * 存储数据（抽象方法）
     *
     * @param key   键
     * @param value 值
     */
    protected abstract void do_put(K key, V value);

    /**
     * 删除数据（抽象方法）
     *
     * @param key 键
     * @return 是否删除成功
     */
    protected abstract boolean do_remove(K key);

    /**
     * 批量获取数据（抽象方法）
     *
     * @param keys 键集合
     * @return 键值对映射
     */
    protected abstract Map<K, V> do_getAll(Set<? extends K> keys);

    /**
     * 批量存储数据（抽象方法）
     *
     * @param map 键值对映射
     */
    protected abstract void do_putAll(Map<? extends K, ? extends V> map);

    /**
     * 批量删除数据（抽象方法）
     *
     * @param keys 键集合
     */
    protected abstract void do_removeAll(Set<? extends K> keys);

    /**
     * 存储数据并指定过期时间（抽象方法）
     *
     * @param key   键
     * @param value 值
     * @param ttl   过期时间
     * @param unit  时间单位
     */
    protected abstract void do_putWithTtl(K key, V value, long ttl, TimeUnit unit);

    // ==================== 公共实现方法 ====================

    /**
     * 获取数据
     *
     * @param key 键
     * @return 值
     */
    @Override
    public V get(K key) {
        ValidationUtils.checkNotNull(key, "Key cannot be null");
        return do_get(key);
    }

    /**
     * 存储数据
     *
     * @param key   键
     * @param value 值
     */
    @Override
    public void put(K key, V value) {
        ValidationUtils.checkNotNull(key, "Key cannot be null");
        do_put(key, value);
    }

    /**
     * 删除数据
     *
     * @param key 键
     * @return 是否删除成功
     */
    @Override
    public boolean remove(K key) {
        ValidationUtils.checkNotNull(key, "Key cannot be null");
        return do_remove(key);
    }
}

