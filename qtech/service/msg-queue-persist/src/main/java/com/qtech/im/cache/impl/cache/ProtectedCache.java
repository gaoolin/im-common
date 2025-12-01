package com.qtech.im.cache.impl.cache;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.support.CacheStats;
import com.qtech.im.cache.support.ExpiringValue;
import com.qtech.im.cache.support.NullValueMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 带保护机制的缓存实现
 * <p>
 * 提供缓存穿透、击穿、雪崩等保护机制
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/27
 */
public class ProtectedCache<K, V> implements Cache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(ProtectedCache.class);

    private final Cache<K, V> delegate;
    private final CacheConfig config;
    private final Map<K, Lock> locks = new ConcurrentHashMap<>();

    public ProtectedCache(Cache<K, V> delegate, CacheConfig config) {
        this.delegate = delegate;
        this.config = config;
    }

    /**
     * 获取缓存项的值
     *
     * @param key 缓存项的键
     * @return 缓存项的值
     */
    @Override
    public V get(K key) {
        try {
            // 缓存穿透保护
            if (config.isEnableNullValueProtection()) {
                Object value = delegate.get(key);
                // 检查是否是空值标记
                if (NullValueMarker.isNullValueMarker(value)) {
                    return null; // 返回实际的null值
                }

                // 检查是否是过期值
                return getV(key, value);
            }

            // 缓存击穿保护
            if (config.isEnableBreakdownProtection()) {
                return getWithBreakdownProtection(key);
            }

            Object value = delegate.get(key);

            // 处理过期值
            return getV(key, value);
        } catch (Exception e) {
            logger.warn("Error getting value from protected cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * 缓存击穿保护
     *
     * @param key 缓存项的键
     * @return 缓存项的值
     */
    private V getWithBreakdownProtection(K key) {
        Object value = delegate.get(key);

        if (value == null) {
            Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
            try {
                if (lock.tryLock(config.getBreakdownLockTimeout(), TimeUnit.MILLISECONDS)) {
                    try {
                        // 双重检查
                        value = delegate.get(key);
                        if (value == null) {
                            // 这里需要与业务逻辑结合，暂时返回null
                            return null;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Thread interrupted while waiting for cache lock", e);
                return null;
            }
        }

        // 处理过期值
        return getV(key, value);
    }

    /**
     * 存储缓存项
     * 在需要存储 ExpiringValue 的地方进行显式类型转换
     *
     * @param key   缓存项的键
     * @param value 缓存项的值
     */
    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
    }

    /**
     * 在需要存储 ExpiringValue 的地方进行显式类型转换
     *
     * @param key   缓存项的键
     * @param value 缓存项的值
     * @param ttl   缓存项的过期时间（以毫秒为单位）
     * @param unit  缓存项的过期时间单位
     */
    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        long expireTimestamp = System.currentTimeMillis() + unit.toMillis(ttl);
        ExpiringValue<V> expiringValue = new ExpiringValue<>(value, expireTimestamp);
        // 需要类型转换，因为 ExpiringValue<V> 被当作 V 存储
        @SuppressWarnings("unchecked")
        V expiringValueAsV = (V) expiringValue;
        delegate.put(key, expiringValueAsV);
    }

    /**
     * 在需要存储 ExpiringValue 的地方进行显式类型转换
     *
     * @param key             缓存项的键
     * @param value           缓存项的值
     * @param expireTimestamp 过期时间戳
     */
    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        ExpiringValue<V> expiringValue = new ExpiringValue<>(value, expireTimestamp);
        @SuppressWarnings("unchecked")
        V expiringValueAsV = (V) expiringValue;
        delegate.put(key, expiringValueAsV);
    }

    /**
     * 批量获取缓存项
     *
     * @param keys 缓存项的键集合
     * @return 缓存项的映射，键为缓存项的键，值为缓存项的值
     */
    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        try {
            // 获取原始值（可能包含ExpiringValue包装器）
            Map<K, V> rawValues = delegate.getAll(keys);

            // 创建结果映射
            Map<K, V> result = new HashMap<>(rawValues.size());

            // 处理每个值，过滤掉过期的条目
            for (Map.Entry<K, V> entry : rawValues.entrySet()) {
                K key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof ExpiringValue) {
                    @SuppressWarnings("unchecked") ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                    if (expiringValue.isExpired()) {
                        // 过期了，清理
                        delegate.remove(key);
                    } else {
                        // 未过期，添加到结果中
                        result.put(key, expiringValue.getValue());
                    }
                } else {
                    // 普通值或空值标记
                    if (config.isEnableNullValueProtection() && NullValueMarker.isNullValueMarker(value)) {
                        result.put(key, null);
                    } else {
                        @SuppressWarnings("unchecked") V typedValue = (V) value;
                        result.put(key, typedValue);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error getting values from protected cache for keys: {}", keys, e);
            return new HashMap<>();
        }
    }

    /**
     * 批量存储值
     *
     * @param map 键值对
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    /**
     * 删除缓存项
     *
     * @param key 缓存项的键
     * @return 是否成功删除缓存项
     */
    @Override
    public boolean remove(K key) {
        return delegate.remove(key);
    }

    /**
     * 批量删除缓存项
     *
     * @param keys 缓存项的键集合
     * @return 删除的缓存项数量
     */
    @Override
    public int removeAll(Set<? extends K> keys) {
        return delegate.removeAll(keys);
    }

    /**
     * 检查缓存项是否存在
     *
     * @param key 缓存项的键
     * @return 是否存在缓存项
     */
    @Override
    public boolean containsKey(K key) {
        Object value = delegate.get(key);

        if (value instanceof ExpiringValue) {
            try {
                @SuppressWarnings("unchecked")
                ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (expiringValue.isExpired()) {
                    // 过期了，清理并返回false
                    delegate.remove(key);
                    return false;
                }

                return true;
            } catch (ClassCastException e) {
                logger.warn("ExpiringValue类型转换错误，清理无效的缓存条目: {}", key);
                delegate.remove(key);
                return false;
            }
        }

        // 处理空值标记
        if (config.isEnableNullValueProtection() && NullValueMarker.isNullValueMarker(value)) {
            return true;
        }

        return value != null;
    }


    /**
     * 获取缓存项数量
     *
     * @return 缓存项数量
     */
    @Override
    public long size() {
        return delegate.size();
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @Override
    public CacheStats getStats() {
        return delegate.getStats();
    }

    /**
     * 获取缓存配置信息
     *
     * @return 缓存配置信息
     */
    @Override
    public CacheConfig getConfig() {
        return delegate.getConfig();
    }

    /**
     * 获取缓存项的值，如果缓存项不存在，则使用提供的加载函数加载并返回
     *
     * @param key    缓存项的键
     * @param loader 缓存项加载函数
     * @return 缓存项的值
     */
    @Override
    public V getOrLoad(K key, Function<K, V> loader) {
        try {
            V value = delegate.get(key);

            // 处理空值保护
            if (config.isEnableNullValueProtection() && NullValueMarker.isNullValueMarker(value)) {
                return null;
            }

            // 处理过期值
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked") ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (!expiringValue.isExpired()) {
                    return expiringValue.getValue();
                }
            } else if (value != null) {
                V typedValue = value;
                return typedValue;
            }

            // 缓存未命中或已过期，加载新值
            V result = loader.apply(key);

            // 根据配置决定是否存储null值
            if (result == null && config.isEnableNullValueProtection()) {
                // 存储空值标记
                delegate.put(key, (V) NullValueMarker.getInstance());
            } else {
                delegate.put(key, result);
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存项的值，如果缓存项不存在，则使用提供的加载函数加载并返回，并设置TTL
     *
     * @param key    缓存项的键
     * @param loader 缓存项加载函数
     * @param ttl    缓存项的TTL
     * @param unit   TTL的时间单位
     * @return 缓存项的值
     */
    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        try {
            Object value = delegate.get(key);

            // 处理空值保护
            if (config.isEnableNullValueProtection() && NullValueMarker.isNullValueMarker(value)) {
                return null;
            }

            // 处理过期值
            if (value instanceof ExpiringValue) {
                @SuppressWarnings("unchecked") ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                if (!expiringValue.isExpired()) {
                    return expiringValue.getValue();
                }
            } else if (value != null) {
                @SuppressWarnings("unchecked") V typedValue = (V) value;
                return typedValue;
            }

            // 缓存未命中或已过期，加载新值
            V result = loader.apply(key);

            // 存储带TTL的值
            if (result == null && config.isEnableNullValueProtection()) {
                // 存储带过期时间的空值标记
                long expireTimestamp = System.currentTimeMillis() + config.getNullValueExpireTime();
                ExpiringValue<Object> expiringNull = new ExpiringValue<>(NullValueMarker.getInstance(), expireTimestamp);
                delegate.put(key, (V) expiringNull);
            } else {
                put(key, result, ttl, unit);
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存项的值，如果缓存项不存在，则使用提供的加载函数加载并返回，并设置绝对过期时间
     *
     * @param key             缓存项的键
     * @param loader          缓存项加载函数
     * @param expireTimestamp 缓存项的绝对过期时间
     * @return 缓存项的值
     */
    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        try {
            Object value = delegate.get(key);

            // 处理空值保护
            if (config.isEnableNullValueProtection() && NullValueMarker.isNullValueMarker(value)) {
                return null;
            }

            // 处理过期值
            if (value instanceof ExpiringValue) {
                try {
                    @SuppressWarnings("unchecked")
                    ExpiringValue<V> expiringValue = (ExpiringValue<V>) value;

                    if (!expiringValue.isExpired()) {
                        return expiringValue.getValue();
                    }
                } catch (ClassCastException e) {
                    logger.warn("ExpiringValue类型转换错误，清理无效的缓存条目: {}", key);
                    delegate.remove(key);
                }
            } else if (value != null) {
                try {
                    @SuppressWarnings("unchecked")
                    V typedValue = (V) value;
                    return typedValue;
                } catch (ClassCastException e) {
                    logger.warn("常规值类型转换错误，清理无效的缓存条目: {}", key);
                    delegate.remove(key);
                    return null;
                }
            }

            // 缓存未命中或已过期，加载新值
            V result = loader.apply(key);

            // 存储带绝对过期时间的值
            if (result == null && config.isEnableNullValueProtection()) {
                // 存储带过期时间的空值标记
                ExpiringValue<Object> expiringNull = new ExpiringValue<>(NullValueMarker.getInstance(), expireTimestamp);
                @SuppressWarnings("unchecked")
                V expiringNullAsV = (V) expiringNull;
                delegate.put(key, expiringNullAsV);
            } else {
                putAtFixedTime(key, result, expireTimestamp);
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error loading value for key: {}", key, e);
            return null;
        }
    }


    /**
     * 刷新缓存，根据配置决定是否清理过期的缓存项
     */
    @Override
    public void refresh() {
        delegate.refresh();
    }

    /**
     * 关闭缓存，根据配置决定是否清理缓存项
     */
    @Override
    public void close() {
        delegate.close();
    }

    /**
     * 清理缓存，根据配置决定是否清理过时的缓存项
     */
    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    /**
     * 处理过期值
     *
     * @param key   缓存项的键
     * @param value 缓存项的值
     * @return 处理后的缓存项的值
     */
    private V getV(K key, Object value) {
        if (value instanceof ExpiringValue) {
            return handleExpiringValue(key, (ExpiringValue<?>) value);
        } else {
            return handleRegularValue(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private V handleExpiringValue(K key, ExpiringValue<?> expiringValue) {
        try {
            ExpiringValue<V> typedExpiringValue = (ExpiringValue<V>) expiringValue;
            if (typedExpiringValue.isExpired()) {
                // 过期了，清理并返回null
                delegate.remove(key);
                return null;
            }
            return typedExpiringValue.getValue();
        } catch (ClassCastException e) {
            logger.warn("ExpiringValue类型转换错误，清理无效的缓存条目: {}", key);
            delegate.remove(key);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private V handleRegularValue(K key, Object value) {
        if (value == null) {
            return null;
        }

        try {
            return (V) value;
        } catch (ClassCastException e) {
            logger.warn("常规值类型转换错误，清理无效的缓存条目: {}", key);
            delegate.remove(key);
            return null;
        }
    }

}
