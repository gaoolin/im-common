package com.qtech.im.cache.impl.redis;


import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/28
 */

/**
 * Redis连接管理器接口
 * 支持不同Redis部署模式的统一连接管理
 */
public interface RedisConnectionManager<K, V> {

    /**
     * 获取连接池
     */
    GenericObjectPool<StatefulRedisConnection<K, V>> getConnectionPool();

    /**
     * 关闭连接管理器
     */
    void close();

    /**
     * 获取连接模式
     */
    RedisMode getMode();
}