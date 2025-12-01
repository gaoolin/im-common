package com.qtech.im.cache;

import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.support.CacheStats;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 通用缓存接口
 * <p>
 * 定义缓存的基本操作，支持本地缓存和分布式缓存
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */
public interface Cache<K, V> {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，不存在返回null
     */
    V get(K key);

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void put(K key, V value);

    /**
     * 设置缓存值并指定过期时间
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     * @param unit  时间单位
     */
    void put(K key, V value, long ttl, TimeUnit unit);

    /**
     * 设置缓存值并指定绝对过期时间戳
     *
     * @param key             缓存键
     * @param value           缓存值
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     */
    void putAtFixedTime(K key, V value, long expireTimestamp);

    /**
     * 批量获取缓存值
     *
     * @param keys 缓存键集合
     * @return 缓存值映射
     */
    Map<K, V> getAll(Set<? extends K> keys);

    /**
     * 批量设置缓存值
     *
     * @param map 缓存键值映射
     */
    void putAll(Map<? extends K, ? extends V> map);

    /**
     * 删除缓存项
     *
     * @param key 缓存键
     * @return 删除成功返回true，否则返回false
     */
    boolean remove(K key);

    /**
     * 批量删除缓存项
     *
     * @param keys 缓存键集合
     * @return 删除成功的数量
     */
    int removeAll(Set<? extends K> keys);

    /**
     * 判断缓存项是否存在
     *
     * @param key 缓存键
     * @return 存在返回true，否则返回false
     */
    boolean containsKey(K key);

    /**
     * 获取缓存大小
     *
     * @return 缓存项数量
     */
    long size();

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStats getStats();

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    CacheConfig getConfig();

    /**
     * 获取或加载缓存值（带自动加载机制）
     *
     * @param key    缓存键
     * @param loader 加载函数
     * @return 缓存值
     */
    V getOrLoad(K key, Function<K, V> loader);

    /**
     * 获取或加载缓存值（带自动加载机制和过期时间）
     *
     * @param key    缓存键
     * @param loader 加载函数
     * @param ttl    过期时间
     * @param unit   时间单位
     * @return 缓存值
     */
    V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit);

    /**
     * 获取或加载缓存值（带自动加载机制和绝对过期时间）
     *
     * @param key             缓存键
     * @param loader          加载函数
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     * @return 缓存值
     */
    V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp);

    /**
     * 刷新缓存（重新加载所有缓存项）
     */
    void refresh();

    /**
     * 关闭缓存
     */
    void close();

    /**
     * 手动触发缓存清理
     */
    void cleanUp();
}
