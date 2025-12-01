package com.qtech.msg.config.redis;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/24 11:24:46
 * desc   :  Redisson + BloomFilter 配置类（支持 Spring Boot 配置绑定）
 */

/*
@Slf4j
@Configuration
public class RedissonBloomFilterConfig {

    private static final String BLOOM_FILTER_NAME = "im:olp:dedup:filter";
    private static final long EXPECTED_INSERTIONS = 10_000_000L; // 预计插入数
    private static final double FALSE_PROBABILITY = 0.01; // 误判率
    // 布隆过滤器的过期时间（单位：秒）
    private static final long BLOOM_FILTER_EXPIRY_TIME = 1800L; // 1小时过期
    @Value("${spring.redis.password}")
    private String redisPassword;
    @Value("${spring.redis.cluster.max-redirects:3}")
    private int maxRedirects;
    @Value("${spring.redis.timeout:5000}")
    private int timeout;

    @Autowired
    private RedisClusterConfig redisClusterConfig;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config
                .setCodec(new StringCodec()) // 使用 StringCodec 避免乱码
                .useClusterServers()
                .addNodeAddress(redisClusterConfig.getNodes().stream()
                        .map(node -> node.startsWith("redis://") ? node : "redis://" + node)
                        .toArray(String[]::new))
                .setPassword(redisPassword)
                .setTimeout(timeout)
                .setScanInterval(2000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setConnectTimeout(5000);

        return Redisson.create(config);
    }

    @Bean
    public RBloomFilter<String> bloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
        bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
        log.info(">>>>> 布隆过滤器 [{}] 初始化完成，容量={}，误判率={}", BLOOM_FILTER_NAME, EXPECTED_INSERTIONS, FALSE_PROBABILITY);

        // 设置布隆过滤器过期时间
        redissonClient.getBucket(BLOOM_FILTER_NAME).expire(BLOOM_FILTER_EXPIRY_TIME, TimeUnit.SECONDS);
        log.info(">>>>> 布隆过滤器 [{}] 设置了过期时间：{} 秒", BLOOM_FILTER_NAME, BLOOM_FILTER_EXPIRY_TIME);

        return bloomFilter;
    }
}
*/
