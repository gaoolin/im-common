package com.qtech.im.cache.impl.redis.connection;

import com.qtech.im.cache.impl.redis.RedisConnectionManager;
import com.qtech.im.cache.impl.redis.RedisMode;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */
public class StandaloneConnectionManager<K, V> implements RedisConnectionManager<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(StandaloneConnectionManager.class);

    private final RedisClient redisClient;
    private final GenericObjectPool<StatefulRedisConnection<K, V>> connectionPool;

    public StandaloneConnectionManager(String redisUri, GenericObjectPoolConfig<StatefulRedisConnection<K, V>> poolConfig) {
        RedisURI uri = RedisURI.create(redisUri);
        this.redisClient = RedisClient.create(uri);

        this.connectionPool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect((RedisCodec<K, V>) StringCodec.UTF8), poolConfig);

        logger.info("Standalone Redis connection manager initialized with URI: {}", redisUri);
    }

    @Override
    public GenericObjectPool<? extends StatefulRedisConnection<K, V>> getConnectionPool() {
        return connectionPool;
    }

    @Override
    public void close() {
        try {
            if (connectionPool != null) {
                connectionPool.close();
            }
        } catch (Exception e) {
            logger.warn("Error closing connection pool", e);
        }

        try {
            if (redisClient != null) {
                redisClient.shutdown();
            }
        } catch (Exception e) {
            logger.warn("Error shutting down Redis client", e);
        }
    }

    @Override
    public RedisMode getMode() {
        return RedisMode.STANDALONE;
    }
}
