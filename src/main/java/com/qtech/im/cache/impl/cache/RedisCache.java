package com.qtech.im.cache.impl.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtech.im.cache.Cache;
import com.qtech.im.cache.impl.redis.RedisConnectionManager;
import com.qtech.im.cache.impl.redis.RedisMode;
import com.qtech.im.cache.impl.redis.connection.RedisConnectionManagerFactory;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.support.CacheStats;
import com.qtech.im.util.json.JsonMapperProvider;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
 * 支持Hash操作以适配特定场景
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
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    private final RedisConnectionManager<K, V> connectionManager;
    private final CacheConfig config;
    private final CacheStats stats = new CacheStats();
    private final String prefix;

    public RedisCache(CacheConfig config) {
        this.config = config;
        this.prefix = config.getName() != null ? config.getName() + ":" : "";
        String redisUri = config.getRedisUri();
        if (redisUri == null || redisUri.isEmpty()) {
            redisUri = "redis://localhost:6379";
        }

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

        this.connectionManager = RedisConnectionManagerFactory.createConnectionManager(redisUri, poolConfig);
        logger.info("RedisCache initialized with config: {} and mode: {}", config, connectionManager.getMode());
    }

    @Override
    public V get(K key) {
        long startTime = System.nanoTime();
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                V value = commands.get(wrapKey(key));
                stats.recordHit();
                return value;
            } finally {
                returnConnection(connection);
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
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                String serializedValue = serialize(value);
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
                stats.recordPut();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {}", key, e);
        }
    }

    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                String serializedValue = serialize(value);
                long ttlSeconds = unit.toSeconds(ttl);
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
                stats.recordPut();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {} with ttl", key, e);
        }
    }

    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                String serializedValue = serialize(value);
                long ttlSeconds = (expireTimestamp - System.currentTimeMillis()) / 1000;
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
                stats.recordPut();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to put value to Redis for key: {} with fixed time", key, e);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                List<K> wrappedKeys = new ArrayList<>();
                for (K key : keys) {
                    wrappedKeys.add(wrapKey(key));
                }
                List<KeyValue<K, V>> values = commands.mget(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
                Map<K, V> result = new HashMap<>();
                Iterator<? extends K> keyIterator = keys.iterator();
                for (KeyValue<K, V> keyValue : values) {
                    K originalKey = keyIterator.next();
                    if (keyValue.hasValue()) {
                        result.put(originalKey, keyValue.getValue());
                    }
                }
                return result;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to get all values from Redis for keys: {}", keys, e);
            return new HashMap<>();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                Map<K, V> serializedMap = new HashMap<>();
                for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                    K key = wrapKey(entry.getKey());
                    V value = entry.getValue();
                    String serializedValue = serialize(value);
                    serializedMap.put(key, (V) serializedValue);
                }
                commands.mset(serializedMap);
                stats.recordPut();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to put all values to Redis for map: {}", map, e);
        }
    }

    @Override
    public boolean remove(K key) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                Long result = commands.del(wrapKey(key));
                return result != null && result > 0;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to remove value from Redis for key: {}", key, e);
            return false;
        }
    }

    @Override
    public int removeAll(Set<? extends K> keys) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                List<K> wrappedKeys = new ArrayList<>();
                for (K key : keys) {
                    wrappedKeys.add(wrapKey(key));
                }
                Long result = commands.del(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
                return result != null ? result.intValue() : 0;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to remove all values from Redis for keys: {}", keys, e);
            return 0;
        }
    }

    @Override
    public boolean containsKey(K key) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                Long result = commands.exists(wrapKey(key));
                return result != null && result > 0;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to check key existence in Redis for key: {}", key, e);
            return false;
        }
    }

    @Override
    public long size() {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                return commands.dbsize();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to get Redis database size", e);
            return -1;
        }
    }

    @Override
    public void clear() {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                commands.flushdb();
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to clear Redis database", e);
        }
    }

    @Override
    public CacheStats getStats() {
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

    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        V value = get(key);
        if (value != null) {
            return value;
        }
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
                logger.error("Failed to load value for key: {} with ttl", key, e);
                throw e;
            }
        }
    }

    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        V value = get(key);
        if (value != null) {
            return value;
        }
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
                logger.error("Failed to load value for key: {} with fixed time", key, e);
                throw e;
            }
        }
    }

    // 新增：Hash 操作 - put to hash
    public void putToHash(String hashKey, String field, String value) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                commands.hset((K) wrapKey((K) hashKey), (K) (Object) field, (V) (Object) value);
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to put field '{}' to Redis hash '{}'", field, hashKey, e);
        }
    }

    // 新增：Hash 操作 - get from hash
    public String getFromHash(String hashKey, String field) {
        long startTime = System.nanoTime();
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                V value = commands.hget((K) wrapKey((K) hashKey), (K) (Object) field);
                stats.recordHit();
                return value != null ? (String) value : null;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            stats.recordMiss();
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.warn("Failed to get field '{}' from Redis hash '{}'", field, hashKey, e);
            return null;
        }
    }

    // 新增：Hash 操作 - get all from hash
    public Map<String, String> getAllFromHash(String hashKey) {
        try {
            StatefulRedisConnection<K, V> connection = getConnection();
            try {
                RedisCommands<K, V> commands = connection.sync();
                Map<K, V> result = commands.hgetall((K) wrapKey((K) hashKey));
                Map<String, String> stringResult = new HashMap<>();
                for (Map.Entry<K, V> entry : result.entrySet()) {
                    stringResult.put((String) entry.getKey(), (String) entry.getValue());
                }
                return stringResult;
            } finally {
                returnConnection(connection);
            }
        } catch (Exception e) {
            logger.error("Failed to get all fields from Redis hash '{}'", hashKey, e);
            return new HashMap<>();
        }
    }

    public void refresh() {
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

    @Override
    public void cleanUp() {
        logger.debug("Redis cache cleanup triggered");
    }

    private K wrapKey(K key) {
        if (key instanceof String) {
            return (K) (prefix + key);
        }
        return key;
    }

    private K unwrapKey(K key) {
        if (key instanceof String && prefix != null && !prefix.isEmpty()) {
            String keyStr = (String) key;
            if (keyStr.startsWith(prefix)) {
                return (K) keyStr.substring(prefix.length());
            }
        }
        return key;
    }

    public RedisMode getMode() {
        return connectionManager != null ? connectionManager.getMode() : null;
    }

    private String serialize(V value) {
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize value: {}", value, e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private StatefulRedisConnection<K, V> getConnection() throws Exception {
        return connectionManager.getConnectionPool().borrowObject();
    }

    private void returnConnection(StatefulRedisConnection<K, V> connection) {
        try {
            if (connection != null) {
                @SuppressWarnings("unchecked")
                GenericObjectPool<StatefulRedisConnection<K, V>> pool =
                        (GenericObjectPool<StatefulRedisConnection<K, V>>) connectionManager.getConnectionPool();
                pool.returnObject(connection);
            }
        } catch (Exception e) {
            logger.warn("Error returning connection to pool", e);
        }
    }
}