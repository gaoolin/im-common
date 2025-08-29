package com.qtech.im.cache.impl.redis.connection;

import com.qtech.im.cache.impl.redis.RedisConnectionManager;
import com.qtech.im.cache.impl.redis.RedisMode;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */

public class ClusterConnectionManager<K, V> implements RedisConnectionManager<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionManager.class);

    private final RedisClusterClient clusterClient;
    private final GenericObjectPool<StatefulRedisClusterConnection<K, V>> connectionPool;

    public ClusterConnectionManager(String redisUris, GenericObjectPoolConfig<StatefulRedisClusterConnection<K, V>> poolConfig) {
        // 解析多个URI
        List<String> uriList = Arrays.asList(redisUris.split(","));

        // 创建RedisURI列表
        List<io.lettuce.core.RedisURI> redisURIs = uriList.stream()
                .map(RedisURI::create)
                .collect(Collectors.toList());

        this.clusterClient = RedisClusterClient.create(redisURIs);

        this.connectionPool = ConnectionPoolSupport.createGenericObjectPool(
                () -> clusterClient.connect((RedisCodec<K, V>) StringCodec.UTF8),
                poolConfig
        );

        logger.info("Redis Cluster connection manager initialized with URIs: {}", redisUris);
    }

    @Override
    public GenericObjectPool<StatefulRedisConnection<K, V>> getConnectionPool() {
        // 类型转换，实际使用时需要处理
        return (GenericObjectPool<StatefulRedisConnection<K, V>>) (Object) connectionPool;
    }

    @Override
    public void close() {
        try {
            if (connectionPool != null) {
                connectionPool.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing cluster connection pool", e);
        }

        try {
            if (clusterClient != null) {
                clusterClient.shutdown();
            }
        } catch (Exception e) {
            logger.warn("Error shutting down Redis cluster client", e);
        }
    }

    @Override
    public RedisMode getMode() {
        return RedisMode.CLUSTER;
    }
}