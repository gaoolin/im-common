package org.im.cache.impl.support;

/**
 * 缓存后端类型枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */

public enum BackendType {
    /**
     * 简单内存缓存，基于 ConcurrentHashMap，无外部依赖
     */
    MEMORY,
    /**
     * 高性能内存缓存，基于 Caffeine
     */
    CAFFEINE,
    /**
     * 分布式缓存，基于 Redis
     */
    REDIS,
    /**
     * 混合缓存，结合本地内存和 Redis
     */
    HYBRID
}