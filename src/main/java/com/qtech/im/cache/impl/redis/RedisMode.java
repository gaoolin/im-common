package com.qtech.im.cache.impl.redis;

/**
 * Redis部署模式枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/28
 */


/**

 */
public enum RedisMode {
    STANDALONE,    // 单节点模式
    SENTINEL,      // 哨兵模式
    CLUSTER        // 集群模式
}