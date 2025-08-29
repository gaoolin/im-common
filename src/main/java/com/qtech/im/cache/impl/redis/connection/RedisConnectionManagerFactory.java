package com.qtech.im.cache.impl.redis.connection;

import com.qtech.im.cache.impl.redis.RedisConnectionManager;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */
public class RedisConnectionManagerFactory {

    public static <K, V> RedisConnectionManager<K, V> createConnectionManager(
            String redisUri,
            GenericObjectPoolConfig<?> poolConfig) {

        // 根据URI判断部署模式
        if (isClusterMode(redisUri)) {
            return new ClusterConnectionManager<>(redisUri, (GenericObjectPoolConfig) poolConfig);
        } else {
            return new StandaloneConnectionManager<>(redisUri, (GenericObjectPoolConfig) poolConfig);
        }
    }

    private static boolean isClusterMode(String redisUri) {
        // 如果包含多个地址，认为是集群模式
        return redisUri != null && redisUri.contains(",");
    }
}