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
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.im.cache.config.CacheConfig;
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
 */
public class RedisCache<K, V> extends AbstractCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private static final ObjectMapper objectMapper = createObjectMapper();

    private final Object client; // RedisClient or RedisClusterClient
    private final GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool; // 添加连接池

    private final GenericObjectPool<StatefulRedisClusterConnection<String, String>> clusterConnectionPool; // 集群连接池
    private final String cacheName;
    private final boolean isCluster;
    private final Class<V> valueType;

    /**
     * 构造函数
     *
     * @param config 缓存配置
     */
    @SuppressWarnings("unchecked")
    public RedisCache(CacheConfig config) {
        this(config, (Class<V>) Object.class);
    }

    /**
     * 构造函数
     *
     * @param config    缓存配置
     * @param valueType 值类型
     */
    @SuppressWarnings("unchecked")
    public RedisCache(CacheConfig config, Class<V> valueType) {
        super(config);
        validateConfig(config);

        this.valueType = valueType != null ? valueType : (Class<V>) Object.class;
        this.cacheName = config.getCacheName() != null ? config.getCacheName() : "";

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

            RedisClusterClient clusterClient = RedisClusterClient.create(redisURIs);
            this.client = clusterClient;

            // 创建集群连接池
            GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>> poolConfig =
                    createPoolConfig(config);
            this.clusterConnectionPool = ConnectionPoolSupport.createGenericObjectPool(
                    clusterClient::connect, poolConfig);
            this.connectionPool = null;
        } else {
            // 单机模式
            this.isCluster = false;
            RedisURI uri = RedisURI.create(redisUri);
            RedisClient redisClient = RedisClient.create(uri);
            this.client = redisClient;

            // 创建单机连接池
            GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig =
                    createPoolConfig(config);
            this.connectionPool = ConnectionPoolSupport.createGenericObjectPool(
                    () -> redisClient.connect(StringCodec.UTF8), poolConfig);
            this.clusterConnectionPool = null;
        }

        logger.info("RedisCache initialized with config: {} and mode: {}", config, isCluster ? "CLUSTER" : "STANDALONE");
    }

    /**
     * 创建自定义的ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper createObjectMapper() {
        return JsonMapperProvider.createCustomizedInstance(mapper -> {
            mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 使用标准ISO格式
            DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            javaTimeModule.addDeserializer(LocalDateTime.class,
                    new LocalDateTimeDeserializer(isoFormatter));
            javaTimeModule.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(isoFormatter));

            mapper.registerModule(javaTimeModule);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        });
    }

    // 添加连接池配置创建方法
    private <T> GenericObjectPoolConfig<T> createPoolConfig(CacheConfig config) {
        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.getMaxConnections() > 0 ? config.getMaxConnections() : 20);
        poolConfig.setMaxIdle(config.getMaxIdleConnections() > 0 ? config.getMaxIdleConnections() : 10);
        poolConfig.setMinIdle(config.getMinIdleConnections() >= 0 ? config.getMinIdleConnections() : 2);
        poolConfig.setMaxWaitMillis(config.getConnectionTimeout() > 0 ? config.getConnectionTimeout() : 5000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
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

        if (config.getCacheName() == null || config.getCacheName().isEmpty()) {
            throw new IllegalArgumentException("Cache prefix cannot be null or empty");
        }

        if (config.getRedisUri() == null || config.getRedisUri().isEmpty()) {
            throw new IllegalArgumentException("Redis URI cannot be null or empty");
        }
    }

    /**
     * 获取数据
     *
     * @param key 键
     * @return 值
     */
    @Override
    @SuppressWarnings("unchecked")
    protected V do_get(K key) {
        if (key == null) {
            return null;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();

            String value;
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                value = commands.get(wrapKey(key));
                logger.debug("RedisCache get value: {} for key: {}", value, wrapKey(key));
            }

            if (value != null) {
                stats.recordHit();
                // 反序列化为对象
                return deserialize(value);
            } else {
                stats.recordMiss();
                return null;
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

    /**
     * 存储数据
     *
     * @param key   键
     * @param value 值
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void do_put(K key, V value) {
        if (key == null) {
            return;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            String serializedValue = serialize(value);

            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), (int) (config.getExpireAfterWrite() / 1000), serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                if (config.getExpireAfterWrite() > 0) {
                    commands.setex(wrapKey(key), (int) (config.getExpireAfterWrite() / 1000), serializedValue);
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

    /**
     * 删除数据
     *
     * @param key 键
     * @return 是否删除成功
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean do_remove(K key) {
        if (key == null) {
            return false;
        }

        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            Long result;
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                result = commands.del(wrapKey(key));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
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

    /**
     * 批量获取数据
     *
     * @param keys 键集合
     * @return 键值对映射
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Map<K, V> do_getAll(Set<? extends K> keys) {
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
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
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
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
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

    /**
     * 批量存储数据
     *
     * @param map 键值对映射
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void do_putAll(Map<? extends K, ? extends V> map) {
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
                    StatefulRedisClusterConnection<String, String> clusterConnection =
                            (StatefulRedisClusterConnection<String, String>) connection;
                    RedisClusterCommands<String, String> commands = clusterConnection.sync();
                    commands.mset(serializedMap);
                } else {
                    StatefulRedisConnection<String, String> standaloneConnection =
                            (StatefulRedisConnection<String, String>) connection;
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

    /**
     * 批量删除数据
     *
     * @param keys 键集合
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void do_removeAll(Set<? extends K> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
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
                return;
            }

            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                commands.del(wrappedKeys.toArray(new String[wrappedKeys.size()]));
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                commands.del(wrappedKeys.toArray(new String[wrappedKeys.size()]));
            }
            stats.recordLoadSuccess(System.nanoTime() - startTime);
        } catch (Exception e) {
            stats.recordLoadException(System.nanoTime() - startTime);
            logger.error("Failed to remove all values from Redis for keys: {}", keys, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 存储数据并指定过期时间
     *
     * @param key   键
     * @param value 值
     * @param ttl   过期时间
     * @param unit  时间单位
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void do_putWithTtl(K key, V value, long ttl, TimeUnit unit) {
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
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), (int) ttlSeconds, serializedValue);
                } else {
                    commands.set(wrapKey(key), serializedValue);
                }
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
                RedisCommands<String, String> commands = standaloneConnection.sync();
                if (ttlSeconds > 0) {
                    commands.setex(wrapKey(key), (int) ttlSeconds, serializedValue);
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

    /**
     * 设置缓存值并指定绝对过期时间戳
     *
     * @param key             缓存键
     * @param value           缓存值
     * @param expireTimestamp 绝对过期时间戳（毫秒）
     */
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

    /**
     * 判断缓存项是否存在
     *
     * @param key 缓存键
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean containsKey(K key) {
        return false;
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    @Override
    @SuppressWarnings("unchecked")
    public long size() {
        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            long size;
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                size = commands.dbsize();
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
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

    /**
     * 清空缓存
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        long startTime = System.nanoTime();
        Object connection = null;
        try {
            connection = createConnection();
            if (isCluster) {
                StatefulRedisClusterConnection<String, String> clusterConnection =
                        (StatefulRedisClusterConnection<String, String>) connection;
                RedisClusterCommands<String, String> commands = clusterConnection.sync();
                commands.flushdb();
            } else {
                StatefulRedisConnection<String, String> standaloneConnection =
                        (StatefulRedisConnection<String, String>) connection;
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

    /**
     * 获取缓存配置
     *
     * @return 缓存配置
     */
    @Override
    public CacheConfig getConfig() {
        return config;
    }

    /**
     * 获取或加载缓存值（带自动加载机制和绝对过期时间）
     *
     * @param key    缓存键
     * @param loader 加载函数
     * @return 缓存值
     */
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

    /**
     * 关闭缓存
     */
    @Override
    public void close() {
        try {
            // 先关闭连接池
            if (connectionPool != null) {
                connectionPool.close();
            }
            if (clusterConnectionPool != null) {
                clusterConnectionPool.close();
            }

            // 再关闭客户端
            if (client instanceof RedisClient) {
                ((RedisClient) client).shutdown();
            } else if (client instanceof RedisClusterClient) {
                ((RedisClusterClient) client).shutdown();
            }
        } catch (Exception e) {
            logger.warn("Error closing Redis client or connection pools", e);
        }
    }

    /**
     * 手动触发缓存清理
     */
    @Override
    public void cleanUp() {

    }

    // ==================== 辅助方法 ====================

    /**
     * 包装键名
     *
     * @param key 键
     * @return 包装后的键名
     */
    private String wrapKey(K key) {
        if (key instanceof String) {
            return cacheName + key;
        }

        // For non-string keys, serialize them to JSON and add prefix
        try {
            String keyStr = objectMapper.writeValueAsString(key);
            return cacheName + keyStr;
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize key: {}", key, e);
            return cacheName + key.toString();
        }
    }

    /**
     * 序列化值
     *
     * @param value 值
     * @return 序列化后的字符串
     */
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

    /**
     * 反序列化值
     *
     * @param value 序列化的字符串
     * @return 反序列化后的值
     */
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

    /**
     * 创建连接
     *
     * @return 连接对象
     */
    @SuppressWarnings("unchecked")
    private Object createConnection() {
        try {
            if (isCluster) {
                return clusterConnectionPool.borrowObject();
            } else {
                return connectionPool.borrowObject();
            }
        } catch (Exception e) {
            logger.error("Failed to borrow connection from pool", e);
            throw new RuntimeException("Failed to get connection from pool", e);
        }
    }

    /**
     * 关闭连接
     *
     * @param connection 连接对象
     */
    private void closeConnection(Object connection) {
        if (connection != null) {
            try {
                if (connection instanceof StatefulRedisConnection) {
                    connectionPool.returnObject((StatefulRedisConnection<String, String>) connection);
                } else if (connection instanceof StatefulRedisClusterConnection) {
                    clusterConnectionPool.returnObject((StatefulRedisClusterConnection<String, String>) connection);
                }
            } catch (Exception e) {
                logger.warn("Error returning connection to pool", e);
                // 如果归还失败，尝试关闭连接
                try {
                    if (connection instanceof StatefulRedisConnection) {
                        ((StatefulRedisConnection<?, ?>) connection).close();
                    } else if (connection instanceof StatefulRedisClusterConnection) {
                        ((StatefulRedisClusterConnection<?, ?>) connection).close();
                    }
                } catch (Exception closeException) {
                    logger.warn("Error closing connection", closeException);
                }
            }
        }
    }
}
