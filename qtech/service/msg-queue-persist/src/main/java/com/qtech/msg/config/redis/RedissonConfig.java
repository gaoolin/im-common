package com.qtech.msg.config.redis;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/28 17:27:20
 * desc   :
 */

/*
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .setScanInterval(2000) // 集群状态扫描间隔
                .addNodeAddress(
                        "redis://10.170.6.24:6379",
                        "redis://10.170.6.25:6379",
                        "redis://10.170.6.26:6379",
                        "redis://10.170.6.141:6379",
                        "redis://10.170.6.142:6379",
                        "redis://10.170.1.68:6379"
                )
                .setPassword("im@2024")
                .setReadMode(ReadMode.SLAVE)   // 读从节点
                .setSubscriptionMode(SubscriptionMode.SLAVE)
                .setMasterConnectionPoolSize(20)
                .setSlaveConnectionPoolSize(20);
        return Redisson.create(config);
    }
}
*/
