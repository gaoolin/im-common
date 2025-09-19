package com.im.equipment.parameter.list.registry;

import java.util.Set;

/**
 * 通用注册表接口
 * <p>
 * 特性：
 * - 通用性：支持各种类型的注册和管理
 * - 规范性：统一的注册表接口和数据结构
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
public interface Registry<K, V> {

    /**
     * 注册实体
     *
     * @param key   键
     * @param value 值
     * @return 是否注册成功
     */
    boolean register(K key, V value);

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 值，如果未找到则返回null
     */
    V get(K key);

    /**
     * 移除实体
     *
     * @param key 键
     * @return 是否移除成功
     */
    boolean remove(K key);

    /**
     * 检查是否存在指定键的实体
     *
     * @param key 键
     * @return 是否存在
     */
    boolean contains(K key);

    /**
     * 获取已注册的实体数量
     *
     * @return 实体数量
     */
    int size();

    /**
     * 清空所有已注册的实体
     */
    void clear();

    /**
     * 获取所有已注册的键
     *
     * @return 键集合
     */
    Set<K> getKeys();
}