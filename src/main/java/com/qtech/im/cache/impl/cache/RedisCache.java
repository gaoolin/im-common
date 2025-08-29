package com.qtech.im.cache.impl.cache;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.CacheStats;
import com.qtech.im.cache.impl.redis.RedisConnectionManager;
import com.qtech.im.cache.impl.redis.RedisMode;
import com.qtech.im.cache.impl.redis.connection.RedisConnectionManagerFactory;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis缓存实现
 * <p>
 * 基于Lettuce客户端实现的分布式缓存，具有高可用、高性能、线程安全等特性
 * 支持多种Redis部署模式：单节点、主从、哨兵、集群
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final RedisConnectionManager<K, V> connectionManager;
    private final CacheConfig config;
    private final CacheStats stats = new CacheStats();
    private final String prefix; // 键前缀，用于区分不同缓存实例

    /**
     * 构造函数
     *
     * @param config 缓存配置
     */
    public RedisCache(CacheConfig config) {
        this.config = config;
        this.prefix = config.getName() != null ? config.getName() + ":" : "";

        // 创建Redis客户端
        String redisUri = config.getRedisUri(); // 从配置中获取Redis连接URI
        if (redisUri == null || redisUri.isEmpty()) {
            redisUri = "redis://localhost:6379"; // 默认连接
        }

        // 配置连接池
        GenericObjectPoolConfig<StatefulRedisConnection<K, V>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.getMaximumSize() > 0 ? config.getMaximumSize() : 100);
        poolConfig.setMaxIdle(50);
        poolConfig.setMinIdle(10);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofMillis(60000));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);

        // 创建连接管理器
        this.connectionManager = RedisConnectionManagerFactory.createConnectionManager(redisUri, poolConfig);

        logger.info("RedisCache initialized with config: {} and mode: {}", config, connectionManager.getMode());
    }

    @Override
    public V get(K key) {
        long startTime = System.nanoTime();
        try {
            try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
                RedisCommands<K, V> commands = connection.sync();
                V value = commands.get(wrapKey(key));
                stats.recordHit();
                return value;
            }
        } catch (Exception e) {
            stats.recordMiss();
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.warn("Failed to get value from Redis for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void put(K key, V value) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            if (config.getExpireAfterWrite() > 0) {
                commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, value);
            } else {
                commands.set(wrapKey(key), value);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {}", key, e);
        }
    }

    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            long ttlSeconds = unit.toSeconds(ttl);
            if (ttlSeconds > 0) {
                commands.setex(wrapKey(key), ttlSeconds, value);
            } else {
                commands.set(wrapKey(key), value);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {}", key, e);
        }
    }

    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            long ttlSeconds = (expireTimestamp - System.currentTimeMillis()) / 1000;
            if (ttlSeconds > 0) {
                commands.setex(wrapKey(key), ttlSeconds, value);
            } else {
                commands.set(wrapKey(key), value);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {}", key, e);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();

            // 构建包装后的键数组
            K[] wrappedKeys = (K[]) new Object[keys.size()];
            Map<K, K> keyMapping = new HashMap<>(); // 包装键 -> 原始键映射
            int index = 0;
            for (K key : keys) {
                K wrappedKey = wrapKey(key);
                wrappedKeys[index] = wrappedKey;
                keyMapping.put(wrappedKey, key);
                index++;
            }

            // 执行批量获取
            Object[] values = commands.mget(wrappedKeys).toArray();

            // 构建结果映射
            for (int i = 0; i < wrappedKeys.length; i++) {
                K wrappedKey = wrappedKeys[i];
                Object value = values[i];
                if (value != null) {
                    K originalKey = keyMapping.get(wrappedKey);
                    // 由于Lettuce的KeyValue类型问题，我们直接处理Object类型
                    result.put(originalKey, (V) value);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get all values from Redis for keys: {}", keys, e);
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            Map<K, V> wrappedMap = new HashMap<>();
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                wrappedMap.put(wrapKey(entry.getKey()), entry.getValue());
            }
            commands.mset(wrappedMap);

            // 设置过期时间
            if (config.getExpireAfterWrite() > 0) {
                for (K key : map.keySet()) {
                    commands.expire(wrapKey(key), config.getExpireAfterWrite() / 1000);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to put all values to Redis", e);
        }
    }

    @Override
    public boolean remove(K key) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            Long result = commands.del(wrapKey(key));
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("Failed to remove value from Redis for key: {}", key, e);
            return false;
        }
    }

    @Override
    public int removeAll(Set<? extends K> keys) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            List<K> wrappedKeys = new ArrayList<>();
            for (K key : keys) {
                wrappedKeys.add(wrapKey(key));
            }
            Long result = commands.del(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            logger.error("Failed to remove all values from Redis for keys: {}", keys, e);
            return 0;
        }
    }

    @Override
    public boolean containsKey(K key) {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            Long result = commands.exists(wrapKey(key));
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("Failed to check key existence in Redis for key: {}", key, e);
            return false;
        }
    }

    @Override
    public long size() {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            // 注意：这个操作在Redis中可能比较耗时，生产环境中应谨慎使用
            return commands.dbsize();
        } catch (Exception e) {
            logger.error("Failed to get Redis database size", e);
            return -1;
        }
    }

    @Override
    public void clear() {
        try (StatefulRedisConnection<K, V> connection = connectionManager.getConnectionPool().borrowObject()) {
            RedisCommands<K, V> commands = connection.sync();
            commands.flushdb();
        } catch (Exception e) {
            logger.error("Failed to clear Redis database", e);
        }
    }

    @Override
    public CacheStats getStats() {
        // Redis本身有丰富的统计信息，这里返回我们自己的统计信息
        return stats;
    }

    @Override
    public CacheConfig getConfig() {
        return config;
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        // 双重检查锁定模式，防止多个线程同时加载
        synchronized (this) {
            value = get(key);
            if (value != null) {
                return value;
            }

            long startTime = System.nanoTime();
            try {
                value = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                if (value != null) {
                    put(key, value);
                }
                return value;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                logger.error("Failed to load value for key: {}", key, e);
                throw e;
            }
        }
    }

    /**
     * 获取或加载缓存值（带自动加载机制和过期时间）
     *
     * @param key    缓存键
     * @param loader 加载函数
     * @param ttl    过期时间
     * @param unit   时间单位
     * @return 缓存值
     */
    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        // 双重检查锁定模式，防止多个线程同时加载
        synchronized (this) {
            value = get(key);
            if (value != null) {
                return value;
            }

            long startTime = System.nanoTime();
            try {
                value = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                if (value != null) {
                    put(key, value, ttl, unit);
                }
                return value;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                logger.error("Failed to load value for key: {}", key, e);
                throw e;
            }
        }
    }

    /**
     * 获取或加载缓存值（带自动加载机制和绝对过期时间）
     *
     * @param key             缓存键
     * @param loader          加载函数
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     * @return 缓存值
     */
    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        // 双重检查锁定模式，防止多个线程同时加载
        synchronized (this) {
            value = get(key);
            if (value != null) {
                return value;
            }

            long startTime = System.nanoTime();
            try {
                value = loader.apply(key);
                stats.recordLoadSuccess(System.nanoTime() - startTime);
                if (value != null) {
                    putAtFixedTime(key, value, expireTimestamp);
                }
                return value;
            } catch (Exception e) {
                stats.recordLoadException(System.nanoTime() - startTime);
                logger.error("Failed to load value for key: {}", key, e);
                throw e;
            }
        }
    }

    /**
     * 刷新缓存（重新加载所有缓存项）
     */
    @Override
    public void refresh() {
        // Redis缓存不支持整体刷新
        logger.debug("Redis cache does not support refresh operation");
    }

    @Override
    public void close() {
        try {
            if (connectionManager != null) {
                connectionManager.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing Redis connection manager", e);
        }
    }

    /**
     * 手动触发缓存清理
     */
    @Override
    public void cleanUp() {
        // Redis会自动处理过期键，这里不需要特殊处理
        logger.debug("Redis cache cleanup triggered");
    }

    /**
     * 包装键，添加前缀
     *
     * @param key 原始键
     * @return 包装后的键
     */
    private K wrapKey(K key) {
        if (key instanceof String) {
            return (K) (prefix + key);
        }
        return key;
    }

    /**
     * 解包键，移除前缀
     *
     * @param key 包装后的键
     * @return 原始键
     */
    private K unwrapKey(K key) {
        if (key instanceof String && prefix != null && !prefix.isEmpty()) {
            String keyStr = (String) key;
            if (keyStr.startsWith(prefix)) {
                return (K) keyStr.substring(prefix.length());
            }
        }
        return key;
    }

    /**
     * 获取连接模式
     *
     * @return Redis部署模式
     */
    public RedisMode getMode() {
        return connectionManager != null ? connectionManager.getMode() : null;
    }
}
