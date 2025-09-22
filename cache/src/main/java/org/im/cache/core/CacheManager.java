package org.im.cache.core;

import org.im.cache.config.CacheConfig;
import org.im.cache.stats.CacheManagerStats;

import java.util.Collection;

/**
 * Cache manager interface for managing cache instances
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */
public interface CacheManager {
    /**
     * Create a cache with the specified name and configuration
     *
     * @param name   Cache name
     * @param config Cache configuration
     * @param <K>    Key type
     * @param <V>    Value type
     * @return Created cache instance
     */
    <K, V> Cache<K, V> createCache(String name, CacheConfig config);

    /**
     * Get a cache by name
     *
     * @param name Cache name
     * @param <K>  Key type
     * @param <V>  Value type
     * @return Cache instance, or null if not found
     */
    <K, V> Cache<K, V> getCache(String name);

    /**
     * Get or create a cache with the specified name and configuration
     *
     * @param name   Cache name
     * @param config Cache configuration
     * @param <K>    Key type
     * @param <V>    Value type
     * @return Cache instance
     */
    <K, V> Cache<K, V> getOrCreateCache(String name, CacheConfig config);

    /**
     * Remove a cache by name
     *
     * @param name Cache name
     * @return true if removed, false otherwise
     */
    boolean removeCache(String name);

    /**
     * Get all cache names
     *
     * @return Collection of cache names
     */
    Collection<String> getCacheNames();

    /**
     * Get cache manager statistics
     *
     * @return Cache manager statistics
     */
    CacheManagerStats getStats();

    /**
     * Close the cache manager and all managed caches
     */
    void close();

    /**
     * Register an existing cache with the manager
     *
     * @param name  Cache name
     * @param cache Cache instance
     * @throws IllegalArgumentException if name or cache is null/empty
     * @throws IllegalStateException    if cache with name already exists
     */
    void registerCache(String name, Cache<?, ?> cache);
}