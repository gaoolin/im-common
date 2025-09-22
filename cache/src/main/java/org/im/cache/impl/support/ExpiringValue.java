package org.im.cache.impl.support;

/**
 * 支持绝对过期时间的缓存值包装器
 *
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */

public class ExpiringValue<V> {
    private final V value;
    private final long expireTimestamp; // 绝对过期时间戳（毫秒）

    public ExpiringValue(V value, long expireTimestamp) {
        this.value = value;
        this.expireTimestamp = expireTimestamp;
    }

    /**
     * 获取缓存值
     *
     * @return 缓存值，如果已过期则返回null
     */
    public V getValue() {
        if (isExpired()) {
            return null;
        }
        return value;
    }

    /**
     * 检查是否已过期
     *
     * @return 已过期返回true，否则返回false
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTimestamp;
    }

    /**
     * 获取绝对过期时间戳
     *
     * @return 过期时间戳（毫秒）
     */
    public long getExpireTimestamp() {
        return expireTimestamp;
    }

    /**
     * 获取剩余生存时间
     *
     * @return 剩余生存时间（毫秒），已过期返回负数
     */
    public long getTtl() {
        return expireTimestamp - System.currentTimeMillis();
    }
}