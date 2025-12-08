package org.im.cache.util;

import org.im.cache.core.Cache;
import org.im.cache.support.NullValueMarker;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 缓存工具类
 * <p>
 * 提供缓存相关的通用工具方法
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/08
 */
public class CacheUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private CacheUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 安全获取缓存值，处理空值标记
     *
     * @param value 缓存值
     * @param <T>   值类型
     * @return 实际值或null（如果是空值标记）
     */
    @SuppressWarnings("unchecked")
    public static <T> T unwrapNullValue(Object value) {
        if (NullValueMarker.isNullValue(value)) {
            return null;
        }
        return (T) value;
    }

    /**
     * 包装可能的空值
     *
     * @param value 原始值
     * @param <T>   值类型
     * @return 包装后的值
     */
    public static <T> Object wrapNullValue(T value) {
        if (value == null) {
            return NullValueMarker.getInstance();
        }
        return value;
    }

    /**
     * 检查值是否为有效的缓存值（非空且非空值标记）
     *
     * @param value 要检查的值
     * @return 如果是有效值返回true，否则返回false
     */
    public static boolean isValidCacheValue(Object value) {
        return value != null && !NullValueMarker.isNullValue(value);
    }

    /**
     * 生成随机过期时间（用于缓存雪崩保护）
     *
     * @param baseTime    基础过期时间（毫秒）
     * @param randomRange 随机范围（毫秒）
     * @return 随机过期时间
     */
    public static long generateRandomExpiration(long baseTime, long randomRange) {
        if (baseTime <= 0) {
            return baseTime;
        }
        if (randomRange <= 0) {
            return baseTime;
        }
        long randomOffset = (long) (Math.random() * randomRange * 2) - randomRange;
        return Math.max(0, baseTime + randomOffset);
    }

    /**
     * 批量获取缓存值并过滤空值
     *
     * @param cache 缓存实例
     * @param keys  键集合
     * @param <K>   键类型
     * @param <V>   值类型
     * @return 过滤后的键值对映射
     */
    public static <K, V> Map<K, V> getValidValues(Cache<K, V> cache, Set<? extends K> keys) {
        Map<K, V> result = cache.getAll(keys);
        result.entrySet().removeIf(entry -> !isValidCacheValue(entry.getValue()));
        return result;
    }

    /**
     * 安全执行加载函数，处理异常情况
     *
     * @param loader 加载函数
     * @param key    键
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 加载的值或null
     */
    public static <K, V> V safeLoad(Function<K, V> loader, K key) {
        try {
            return loader.apply(key);
        } catch (Exception e) {
            // 记录异常但不抛出，返回null
            return null;
        }
    }
}
