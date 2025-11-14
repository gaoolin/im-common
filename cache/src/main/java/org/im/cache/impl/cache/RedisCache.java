package org.im.cache.impl.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.StringCodec;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.stats.CacheStats;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * @date 2025/08/28
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.createCustomizedInstance(m -> {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置 LocalDateTime 反序列化器，支持空格分隔格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(formatter));
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(formatter));
        m.registerModule(javaTimeModule);
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    });

    private final Object client; // RedisClient or RedisClusterClient
    private final CacheConfig config;
    private final CacheStats stats = new CacheStats();
    private final String prefix;
    private final boolean isCluster;
    private final Class<V> valueType;

    @SuppressWarnings("unchecked")
    public RedisCache(CacheConfig config) {
        this(config, (Class<V>) Object.class);
    }

    @SuppressWarnings("unchecked")
    public RedisCache(CacheConfig config, Class<V> valueType) {
        validateConfig(config);
        this.config = config;
        this.valueType = valueType != null ? valueType : (Class<V>) Object.class;
        this.prefix = config.getPrefix() != null ? config.getPrefix() : "";
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

    /**
     * 验证配置参数
     *
     * @param config 缓存配置
     */
    private void validateConfig(CacheConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("CacheConfig cannot be null");
        }

        if (config.getPrefix() == null || config.getPrefix().isEmpty()) {
            throw new IllegalArgumentException("Cache prefix cannot be null or empty");
        }

        if (config.getRedisUri() == null || config.getRedisUri().isEmpty()) {
            throw new IllegalArgumentException("Redis URI cannot be null or empty");
        }
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                String value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));

                if (value != null) {
                    stats.recordHit();
                    // 反序列化为对象
                    return deserialize(value);
                } else {
                    stats.recordMiss();
                    return null;
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                String value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));

                if (value != null) {
                    stats.recordHit();
                    // 反序列化为对象
                    return deserialize(value);
                } else {
                    stats.recordMiss();
                    return null;
                }
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), config.getExpireAfterWrite() / 1000, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), ttlSeconds, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
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
            List<String> wrappedKeys = new ArrayList<>();
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                List<KeyValue<String, String>> values = commands.mget(wrappedKeys.toArray(new String[0]));
                Iterator<? extends K> keyIterator = keys.iterator();

                int hitCount = 0;
                int missCount = 0;

                for (KeyValue<String, String> keyValue : values) {
                    K originalKey = keyIterator.next();
                    if (keyValue.hasValue()) {
                        V deserializedValue = deserialize(keyValue.getValue());
                        result.put(originalKey, deserializedValue);
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
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                List<KeyValue<String, String>> values = commands.mget(wrappedKeys.toArray(new String[0]));
                Iterator<? extends K> keyIterator = keys.iterator();

                int hitCount = 0;
                int missCount = 0;

                for (KeyValue<String, String> keyValue : values) {
                    K originalKey = keyIterator.next();
                    if (keyValue.hasValue()) {
                        V deserializedValue = deserialize(keyValue.getValue());
                        result.put(originalKey, deserializedValue);
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
            Map<String, String> serializedMap = new HashMap<>();
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                if (key != null) {
                    String serializedValue = serialize(value);
                    serializedMap.put(wrapKey(key), serializedValue);
                }
            }

            if (!serializedMap.isEmpty()) {
                if (isCluster) {
                    StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                    RedisClusterCommands<String, String> commands = clusterConnection.sync();
                    commands.mset(serializedMap);
                } else {
                    StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                    RedisCommands<String, String> commands = standaloneConnection.sync();
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                result = commands.del(wrapKey(key));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
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
            List<String> wrappedKeys = new ArrayList<>();
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                result = commands.del(wrappedKeys.toArray(new String[wrappedKeys.size()]));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                result = commands.del(wrappedKeys.toArray(new String[wrappedKeys.size()]));
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                result = commands.exists(wrapKey(key));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                size = commands.dbsize();
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
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
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                commands.flushdb();
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
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
    // 修改方法签名，使用泛型类型 K
    @SuppressWarnings("unchecked")
    public void putToHash(K hashKey, String field, String value) {
        if (hashKey == null || field == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                commands.hset(wrapKey(hashKey), field, value);
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                commands.hset(wrapKey(hashKey), field, value);
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
    public String getFromHash(K hashKey, String field) {
        if (hashKey == null || field == null) {
            return null;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            String value;
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                value = commands.hget(wrapKey(hashKey), field);
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                value = commands.hget(wrapKey(hashKey), field);
            }

            if (value != null) {
                stats.recordHit();
            } else {
                stats.recordMiss();
            }

            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return value;
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
    public Map<String, String> getAllFromHash(K hashKey) {
        if (hashKey == null) {
            return Collections.emptyMap();
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Map<String, String> result;
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection = (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                result = commands.hgetall(wrapKey(hashKey));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection = (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                result = commands.hgetall(wrapKey(hashKey));
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
            return result;
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

    private String wrapKey(K key) {
        if (key instanceof String) {
            return prefix + key;
        }

        // For non-string keys, serialize them to JSON and add prefix
        try {
            String keyStr = objectMapper.writeValueAsString(key);
            return prefix + keyStr;
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize key: {}", key, e);
            return prefix + key.toString();
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
    private V deserialize(String value) {
        if (value == null) {
            return null;
        }

        // 如果值类型是String，直接返回
        if (valueType == String.class || valueType == Object.class) {
            return (V) value;
        }

        try {
            // 如果是泛型类型，使用TypeReference处理
            if (valueType == Map.class) {
                return (V) objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
                });
            } else if (valueType == List.class) {
                return (V) objectMapper.readValue(value, new TypeReference<List<Object>>() {
                });
            }

            return objectMapper.readValue(value, valueType);
        } catch (Exception e) {
            logger.error("Failed to deserialize value: {} to type: {}", value, valueType, e);
            // 如果反序列化失败，返回原始字符串值
            return (V) value;
        }
    }

    @SuppressWarnings("unchecked")
    private Object createConnection() {
        if (isCluster) {
            RedisClusterClient clusterClient = (RedisClusterClient) client;
            return clusterClient.connect(StringCodec.UTF8);
        } else {
            RedisClient redisClient = (RedisClient) client;
            return redisClient.connect(StringCodec.UTF8);
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
