package org.im.cache.impl.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.stats.CacheStats;
import org.im.util.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis缓存实现（基于Lettuce的代理层）
 * <p>
 * 作为Lettuce客户端的代理层，提供统一的缓存接口
 * 具有高可用、高性能、线程安全等特性
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

    private final Object client; // RedisClient or RedisClusterClient
    private final CacheConfig config;
    private final CacheStats stats = new CacheStats();
    private final String prefix;
    private final boolean isCluster;

    @SuppressWarnings("unchecked")
    public RedisCache(CacheConfig config) {
        this.config = config;
        this.prefix = config.getName() != null ? config.getName() : "";
        String redisUri = config.getRedisUri();
        if (redisUri == null || redisUri.isEmpty()) {
            redisUri = "redis://localhost:6379";
        }

        if (redisUri.contains(",")) {
            // 集群模式
            this.isCluster = true;
            String[] uris = redisUri.split(",");
            List<RedisURI> redisURIs = new ArrayList<>();
            for (String uri : uris) {
                redisURIs.add(RedisURI.create(uri.trim()));
            }

            this.client = RedisClusterClient.create(redisURIs);
        } else {
            // 单机模式
            this.isCluster = false;
            RedisURI uri = RedisURI.create(redisUri);
            this.client = RedisClient.create(uri);
        }

        logger.info("RedisCache initialized with config: {} and mode: {}", config, isCluster ? "CLUSTER" : "STANDALONE");
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        if (key == null) {
            return null;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();

            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                V value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));

                if (value != null) {
                    stats.recordHit();
                } else {
                    stats.recordMiss();
                }

                stats.recordLoadSuccess(System.nanoTime() - startTime);
                return value;
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                V value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));

                if (value != null) {
                    stats.recordHit();
                } else {
                    stats.recordMiss();
                }

                stats.recordLoadSuccess(System.nanoTime() - startTime);
                return value;
            }
        } catch (Exception e) {
            stats.recordMiss();
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.warn("Failed to get value from Redis for key: {}", key, e);
            return null;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(K key, V value) {
        if (key == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            String serializedValue = serialize(value);

            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            }
            stats.recordPut();
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to put value to Redis for key: {}", key, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(K key, V value, long ttl, TimeUnit unit) {
        if (key == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            String serializedValue = serialize(value);
            long ttlSeconds = unit.toSeconds(ttl);

            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            }
            stats.recordPut();
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to put value to Redis for key: {} with ttl", key, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        if (key == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            String serializedValue = serialize(value);
            long ttlSeconds = (expireTimestamp - System.currentTimeMillis()) / 1000;

            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, (V) serializedValue);
                } else {
                    commands.set(wrapKey(key), (V) serializedValue);
                }
            }
            stats.recordPut();
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to put value to Redis for key: {} with fixed time", key, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> getAll(Set<? extends K> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            List<K> wrappedKeys = new ArrayList<>();
            for (K key : keys) {
                if (key != null) {
                    wrappedKeys.add(wrapKey(key));
                }
            }

            if (wrappedKeys.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<K, V> result = new HashMap<>();
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                List<KeyValue<K, V>> values = commands.mget(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
                Iterator<? extends K> keyIterator = keys.iterator();

                int hitCount = 0;
                int missCount = 0;

                for (KeyValue<K, V> keyValue : values) {
                    K originalKey = keyIterator.next();
                    if (keyValue.hasValue()) {
                        result.put(originalKey, keyValue.getValue());
                        hitCount++;
                    } else {
                        missCount++;
                    }
                }

                // Record stats
                for (int i = 0; i < hitCount; i++) {
                    stats.recordHit();
                }
                for (int i = 0; i < missCount; i++) {
                    stats.recordMiss();
                }
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                List<KeyValue<K, V>> values = commands.mget(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
                Iterator<? extends K> keyIterator = keys.iterator();

                int hitCount = 0;
                int missCount = 0;

                for (KeyValue<K, V> keyValue : values) {
                    K originalKey = keyIterator.next();
                    if (keyValue.hasValue()) {
                        result.put(originalKey, keyValue.getValue());
                        hitCount++;
                    } else {
                        missCount++;
                    }
                }

                // Record stats
                for (int i = 0; i < hitCount; i++) {
                    stats.recordHit();
                }
                for (int i = 0; i < missCount; i++) {
                    stats.recordMiss();
                }
            }

            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            // Record all as misses
            for (int i = 0; i < keys.size(); i++) {
                stats.recordMiss();
            }
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to get all values from Redis for keys: {}", keys, e);
            return new HashMap<>();
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Map<K, V> serializedMap = new HashMap<>();
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                if (key != null) {
                    String serializedValue = serialize(value);
                    serializedMap.put(wrapKey(key), (V) serializedValue);
                }
            }

            if (!serializedMap.isEmpty()) {
                if (isCluster) {
                    StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                    RedisClusterCommands<K, V> commands = clusterConnection.sync();
                    commands.mset(serializedMap);
                } else {
                    StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                    RedisCommands<K, V> commands = standaloneConnection.sync();
                    commands.mset(serializedMap);
                }

                // Record stats
                for (int i = 0; i < serializedMap.size(); i++) {
                    stats.recordPut();
                }
            }

            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to put all values to Redis for map: {}", map, e);
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(K key) {
        if (key == null) {
            return false;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Long result;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                result = commands.del(wrapKey(key));
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                result = commands.del(wrapKey(key));
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return result != null && result > 0;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to remove value from Redis for key: {}", key, e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int removeAll(Set<? extends K> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            List<K> wrappedKeys = new ArrayList<>();
            for (K key : keys) {
                if (key != null) {
                    wrappedKeys.add(wrapKey(key));
                }
            }

            if (wrappedKeys.isEmpty()) {
                return 0;
            }

            Long result;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                result = commands.del(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                result = commands.del(wrappedKeys.toArray((K[]) new Object[wrappedKeys.size()]));
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to remove all values from Redis for keys: {}", keys, e);
            return 0;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Long result;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                result = commands.exists(wrapKey(key));
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                result = commands.exists(wrapKey(key));
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return result != null && result > 0;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to check key existence in Redis for key: {}", key, e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public long size() {
        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            long size;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                size = commands.dbsize();
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                size = commands.dbsize();
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return size;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to get Redis database size", e);
            return -1;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                commands.flushdb();
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                commands.flushdb();
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to clear Redis database", e);
        } finally {
            closeConnection(connection);
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
    @SuppressWarnings("unchecked")
    public V getOrLoad(K key, Function<K, V> loader) {
        if (key == null || loader == null) {
            return null;
        }

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
    @SuppressWarnings("unchecked")
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        if (key == null || loader == null) {
            return null;
        }

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
    @SuppressWarnings("unchecked")
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        if (key == null || loader == null) {
            return null;
        }

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
    @SuppressWarnings("unchecked")
    public void putToHash(String hashKey, String field, String value) {
        if (hashKey == null || field == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                commands.hset((K) wrapKey((K) hashKey), (K) (Object) field, (V) (Object) value);
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                commands.hset((K) wrapKey((K) hashKey), (K) (Object) field, (V) (Object) value);
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to put field '{}' to Redis hash '{}'", field, hashKey, e);
        } finally {
            closeConnection(connection);
        }
    }

    // 新增：Hash 操作 - get from hash
    @SuppressWarnings("unchecked")
    public String getFromHash(String hashKey, String field) {
        if (hashKey == null || field == null) {
            return null;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            V value;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                value = commands.hget((K) wrapKey((K) hashKey), (K) (Object) field);
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                value = commands.hget((K) wrapKey((K) hashKey), (K) (Object) field);
            }

            if (value != null) {
                stats.recordHit();
            } else {
                stats.recordMiss();
            }

            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return value != null ? (String) value : null;
        } catch (Exception e) {
            stats.recordMiss();
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.warn("Failed to get field '{}' from Redis hash '{}'", field, hashKey, e);
            return null;
        } finally {
            closeConnection(connection);
        }
    }

    // 新增：Hash 操作 - get all from hash
    @SuppressWarnings("unchecked")
    public Map<String, String> getAllFromHash(String hashKey) {
        if (hashKey == null) {
            return Collections.emptyMap();
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Map<K, V> result;
            if (isCluster) {
                StatefulRedisClusterConnection<K, V> clusterConnection = (StatefulRedisClusterConnection<K, V>) connection;
                RedisClusterCommands<K, V> commands = clusterConnection.sync();
                result = commands.hgetall((K) wrapKey((K) hashKey));
            } else {
                StatefulRedisConnection<K, V> standaloneConnection = (StatefulRedisConnection<K, V>) connection;
                RedisCommands<K, V> commands = standaloneConnection.sync();
                result = commands.hgetall((K) wrapKey((K) hashKey));
            }
            Map<String, String> stringResult = new HashMap<>();
            for (Map.Entry<K, V> entry : result.entrySet()) {
                stringResult.put((String) entry.getKey(), (String) entry.getValue());
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return stringResult;
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to get all fields from Redis hash '{}'", hashKey, e);
            return new HashMap<>();
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public void refresh() {
        logger.debug("Redis cache does not support refresh operation");
    }

    @Override
    public void close() {
        try {
            if (client instanceof RedisClient) {
                ((RedisClient) client).shutdown();
            } else if (client instanceof RedisClusterClient) {
                ((RedisClusterClient) client).shutdown();
            }
        } catch (Exception e) {
            logger.warn("Error closing Redis client", e);
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

        // For non-string keys, serialize them to JSON and add prefix
        try {
            String keyStr = objectMapper.writeValueAsString(key);
            return (K) (prefix + keyStr);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize key: {}", key, e);
            return key;
        }
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
    private Object createConnection() {
        if (isCluster) {
            RedisClusterClient clusterClient = (RedisClusterClient) client;
            return clusterClient.connect((RedisCodec<K, V>) StringCodec.UTF8);
        } else {
            RedisClient redisClient = (RedisClient) client;
            return redisClient.connect((RedisCodec<K, V>) StringCodec.UTF8);
        }
    }

    private void closeConnection(Object connection) {
        if (connection != null) {
            try {
                if (connection instanceof StatefulRedisConnection) {
                    ((StatefulRedisConnection<?, ?>) connection).close();
                } else if (connection instanceof StatefulRedisClusterConnection) {
                    ((StatefulRedisClusterConnection<?, ?>) connection).close();
                }
            } catch (Exception e) {
                logger.warn("Error closing connection", e);
            }
        }
    }
}
