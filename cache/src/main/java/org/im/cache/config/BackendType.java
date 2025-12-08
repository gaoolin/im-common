package org.im.cache.config;

/**
 * 缓存后端类型枚举
 * <p>
 * 定义支持的缓存后端类型
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/29
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
     * Redis缓存
     */
    REDIS,
    /**
     * 混合缓存
     */
    HYBRID
}