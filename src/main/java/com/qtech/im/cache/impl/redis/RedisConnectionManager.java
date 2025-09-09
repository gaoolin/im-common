package com.qtech.im.cache.impl.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Redis连接管理器接口
 * 支持不同Redis部署模式的统一连接管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */

public interface RedisConnectionManager<K, V> {

    /**
     * 获取连接池
     * <p>
     * 根据不同的Redis部署模式，返回相应的连接池:
     * <ul>
     * <li>单机/哨兵模式: GenericObjectPool<StatefulRedisConnection<K, V>></li>
     * <li>集群模式: GenericObjectPool<StatefulRedisClusterConnection<K, V>></li>
     * </ul>
     * StatefulRedisClusterConnection是StatefulRedisConnection的子类型
     * </p>
     *
     * @return 连接池对象
     */
    GenericObjectPool<? extends StatefulRedisConnection<K, V>> getConnectionPool();

    /**
     * 关闭连接管理器
     *
     * @throws Exception 如果关闭过程中发生错误
     */
    void close() throws Exception;

    /**
     * 获取连接模式
     *
     * @return Redis部署模式
     */
    RedisMode getMode();
}