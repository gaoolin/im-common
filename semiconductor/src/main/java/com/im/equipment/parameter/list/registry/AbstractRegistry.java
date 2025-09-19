package com.im.equipment.parameter.list.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 抽象注册表基类
 * <p>
 * 特性：
 * - 通用性：支持各种类型的注册和管理
 * - 规范性：统一的注册表实现标准
 * - 灵活性：可配置的注册和获取策略
 * - 可靠性：完善的管理流程和错误处理机制
 * - 安全性：线程安全的缓存管理
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/25
 */
public abstract class AbstractRegistry<K, V> implements Registry<K, V> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 实体缓存，保证线程安全
    protected final Map<K, V> cache = new ConcurrentHashMap<>();

    // 实体提供者函数
    protected Function<K, V> provider;

    /**
     * 设置实体提供者函数
     *
     * @param provider 实体提供者函数
     */
    public void setProvider(Function<K, V> provider) {
        this.provider = provider;
    }

    /**
     * 注册实体
     *
     * @param key   键
     * @param value 值
     * @return 是否注册成功
     */
    @Override
    public boolean register(K key, V value) {
        if (key == null) {
            logger.warn("Invalid key provided for registration");
            return false;
        }

        if (value == null) {
            logger.warn("Null value provided for registration with key: {}", key);
            return false;
        }

        try {
            cache.put(key, value);
            logger.debug("Registered entity with key: {}", key);
            return true;
        } catch (Exception e) {
            logger.error("Failed to register entity with key: {}", key, e);
            return false;
        }
    }

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 值，如果未找到则返回null
     */
    @Override
    public V get(K key) {
        if (key == null) {
            logger.warn("Invalid key provided for retrieval");
            return null;
        }

        try {
            // 首先从缓存中获取
            V cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }

            // 如果缓存中没有且提供了实体提供者，则尝试获取
            if (provider != null) {
                V newValue = provider.apply(key);
                if (newValue != null) {
                    cache.put(key, newValue);
                    logger.debug("Loaded and cached entity with key: {}", key);
                    return newValue;
                }
            }

            logger.warn("Entity not found for key: {}", key);
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while getting entity with key: {}", key, e);
            return null;
        }
    }

    /**
     * 移除实体
     *
     * @param key 键
     * @return 是否移除成功
     */
    @Override
    public boolean remove(K key) {
        if (key == null) {
            logger.warn("Invalid key provided for removal");
            return false;
        }

        try {
            V removedValue = cache.remove(key);
            if (removedValue != null) {
                logger.debug("Removed entity with key: {}", key);
                return true;
            } else {
                logger.debug("No entity found to remove for key: {}", key);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error occurred while removing entity with key: {}", key, e);
            return false;
        }
    }

    /**
     * 检查是否存在指定键的实体
     *
     * @param key 键
     * @return 是否存在
     */
    @Override
    public boolean contains(K key) {
        if (key == null) {
            return false;
        }
        return cache.containsKey(key);
    }

    /**
     * 获取已注册的实体数量
     *
     * @return 实体数量
     */
    @Override
    public int size() {
        return cache.size();
    }

    /**
     * 清空所有已注册的实体
     */
    @Override
    public void clear() {
        try {
            cache.clear();
            logger.debug("Cleared all entities");
        } catch (Exception e) {
            logger.error("Error occurred while clearing entities", e);
        }
    }

    /**
     * 获取所有已注册的键
     *
     * @return 键集合
     */
    @Override
    public Set<K> getKeys() {
        return new java.util.HashSet<>(cache.keySet());
    }
}
