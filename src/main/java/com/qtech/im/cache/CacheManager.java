package com.qtech.im.cache;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:50:08
 * desc   :  im-common-IntelliJ IDEA
 */

/**
 * 缓存管理器接口
 * <p>
 * 负责缓存实例的创建、管理和销毁
 */
public interface CacheManager {

    /**
     * 创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> createCache(String name, CacheConfig config);

    /**
     * 获取缓存实例
     *
     * @param name 缓存名称
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 缓存实例，不存在返回null
     */
    <K, V> Cache<K, V> getCache(String name);

    /**
     * 获取或创建缓存实例
     *
     * @param name   缓存名称
     * @param config 缓存配置
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config);

    /**
     * 删除缓存实例
     *
     * @param name 缓存名称
     * @return 删除成功返回true，否则返回false
     */
    boolean removeCache(String name);

    /**
     * 获取所有缓存名称
     *
     * @return 缓存名称数组
     */
    String[] getCacheNames();

    /**
     * 获取缓存管理器统计信息
     *
     * @return 统计信息
     */
    CacheManagerStats getStats();

    /**
     * 关闭缓存管理器
     */
    void close();
}