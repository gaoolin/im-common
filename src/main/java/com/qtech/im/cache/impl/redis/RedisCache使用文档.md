# RedisCache 使用文档

## 1. 概述

RedisCache 是 QTech IM 缓存管理组件中的分布式缓存实现，基于高性能的 Lettuce Redis 客户端构建。它提供了与本地缓存一致的接口，同时具备分布式缓存的能力，支持多种 Redis
部署模式，包括单节点、主从、哨兵和集群模式。

## 2. 特性

- **多模式支持**：支持单节点、集群等多种 Redis 部署模式
- **高性能**：使用 Lettuce 客户端，支持异步和连接池
- **线程安全**：通过连接池管理 Redis 连接，确保多线程环境下的安全性
- **丰富的缓存操作**：支持单个和批量的缓存操作
- **过期策略**：支持设置缓存项的过期时间
- **统计监控**：提供缓存访问统计信息
- **资源管理**：支持优雅关闭和资源释放
- **配置灵活**：支持多种连接参数和连接池配置

## 3. 依赖

使用 RedisCache 需要添加以下依赖：

```xml

<dependencies>
    <!-- Lettuce Redis 客户端 -->
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
        <version>6.2.4.RELEASE</version>
    </dependency>

    <!-- Apache Commons Pool2 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
        <version>2.11.1</version>
    </dependency>
</dependencies>
```

## 4. 配置

### 4.1 基本配置

RedisCache 通过 CacheConfig 进行配置：

```java
CacheConfig config=new CacheConfig();
        config.setName("userCache");                           // 缓存名称
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED); // 设置为分布式缓存类型
        config.setRedisUri("redis://localhost:6379");          // Redis 连接地址
        config.setMaximumSize(1000);                           // 连接池最大连接数
        config.setExpireAfterWrite(TimeUnit.MINUTES.toMillis(30)); // 写入后30分钟过期
```

### 4.2 高级配置

```java
// 设置连接超时和Socket超时
config.setRedisConnectionTimeout(2000);
        config.setRedisSoTimeout(2000);

// 连接池配置
        GenericObjectPoolConfig poolConfig=new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(50);
        poolConfig.setMinIdle(10);
```

## 5. 支持的Redis部署模式

### 5.1 单节点模式

适用于简单的 Redis 部署环境：

```java
// 单节点Redis配置
CacheConfig config=new CacheConfig();
        config.setName("standaloneCache");
        config.setRedisUri("redis://localhost:6379");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);

        Cache<String, User> cache=new RedisCache<>(config);
```

### 5.2 Redis集群模式

适用于需要高可用和水平扩展的场景：

```java
// Redis集群配置
CacheConfig config=new CacheConfig();
        config.setName("clusterCache");
        config.setRedisUri("redis://192.168.1.10:6379,192.168.1.11:6379,192.168.1.12:6379");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);

        Cache<String, User> cache=new RedisCache<>(config);

// Redis 6.0+ 支持 ACL 用户名
// 格式: redis://username:password@host:port
        config.setRedisUri("redis://default:mypassword@192.168.1.10:6379,192.168.1.11:6379,192.168.1.12:6379");

// 基本格式
        redis://:password@host:port

// 示例：集群中所有节点使用相同密码
        CacheConfig config=new CacheConfig();
        config.setName("clusterAuthCache");
        config.setRedisUri("redis://:mypassword@192.168.1.10:6379,192.168.1.11:6379,192.168.1.12:6379");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);

        Cache<String, User> cache=new RedisCache<>(config);

```

### 5.3 带认证的连接

```java
// 带密码的Redis连接
CacheConfig config=new CacheConfig();
        config.setName("authCache");
        config.setRedisUri("redis://:password@localhost:6379"); // 格式: redis://:password@host:port

        Cache<String, User> cache=new RedisCache<>(config);
```

### 5.4 SSL/TLS连接

```java
// SSL/TLS加密连接
CacheConfig config=new CacheConfig();
        config.setName("sslCache");
        config.setRedisUri("rediss://localhost:6380"); // 使用rediss协议

        Cache<String, User> cache=new RedisCache<>(config);
```

## 6. 使用方法

### 6.1 创建 RedisCache 实例

```java
// 方式1：直接创建
CacheConfig config=new CacheConfig();
        config.setName("myRedisCache");
        config.setRedisUri("redis://192.168.1.100:6379");
        RedisCache<String, User> cache=new RedisCache<>(config);

// 方式2：通过 CacheFactory 创建
        CacheConfig config=new CacheConfig();
        config.setName("myRedisCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setRedisUri("redis://192.168.1.100:6379");
        Cache<String, User> cache=CacheFactory.createCache(config);

// 方式3：通过 CacheBuilder 创建
        Cache<String, User> cache=CacheBuilder.newBuilder()
        .name("myRedisCache")
        .cacheType(CacheConfig.CacheType.DISTRIBUTED)
        .build();
```

### 6.2 基本操作

```java
// 存储缓存项
cache.put("userId1",user1);

// 获取缓存项
        User user=cache.get("userId1");

// 删除缓存项
        boolean removed=cache.remove("userId1");

// 检查缓存项是否存在
        boolean exists=cache.containsKey("userId1");

// 清空所有缓存项
        cache.clear();

// 获取缓存大小
        long size=cache.size();
```

### 6.3 批量操作

```java
// 批量存储
Map<String, User> userMap=new HashMap<>();
        userMap.put("userId1",user1);
        userMap.put("userId2",user2);
        cache.putAll(userMap);

// 批量获取
        Set<String> keys=new HashSet<>();
        keys.add("userId1");
        keys.add("userId2");
        Map<String, User> users=cache.getAll(keys);

// 批量删除
        Set<String> deleteKeys=new HashSet<>();
        deleteKeys.add("userId1");
        deleteKeys.add("userId2");
        int deletedCount=cache.removeAll(deleteKeys);
```

### 6.4 过期时间设置

```java
// 设置固定过期时间
cache.put("userId1",user1,30,TimeUnit.MINUTES);

// 设置绝对过期时间
        long expireTime=System.currentTimeMillis()+TimeUnit.HOURS.toMillis(1);
        cache.putAtFixedTime("userId1",user1,expireTime);
```

### 6.5 自动加载机制

```java
// 获取或加载（缓存未命中时自动加载）
User user=cache.getOrLoad("userId1",key->{
        // 从数据库或其他数据源加载用户
        return loadUserFromDatabase(key);
        });

// 获取或加载（带过期时间）
        User user=cache.getOrLoad("userId1",
        key->loadUserFromDatabase(key),
        30,TimeUnit.MINUTES);

// 获取或加载（带绝对过期时间）
        long expireTime=System.currentTimeMillis()+TimeUnit.HOURS.toMillis(1);
        User user=cache.getOrLoadAtFixedTime("userId1",
        key->loadUserFromDatabase(key),
        expireTime);
```

### 6.6 统计信息

```java
// 获取统计信息
CacheStats stats=cache.getStats();
        System.out.println("请求总数: "+stats.getRequestCount());
        System.out.println("命中次数: "+stats.getHitCount());
        System.out.println("未命中次数: "+stats.getMissCount());
        System.out.println("命中率: "+String.format("%.2f%%",stats.getHitRate()*100));
        System.out.println("平均加载时间: "+String.format("%.2fms",stats.getAverageLoadTime()));
```

### 6.7 资源管理

```java
// 在应用关闭时释放资源
try{
        cache.close();
        }catch(Exception e){
        logger.error("Error closing cache",e);
        }
```

## 7. 最佳实践

### 7.1 连接池配置

```java
CacheConfig config=new CacheConfig();
        config.setMaximumSize(100);  // 连接池最大连接数
        config.setRedisUri("redis://localhost:6379");

// 建议根据应用的并发量调整连接池大小
// 通常设置为应用最大并发线程数的1.5倍左右
```

### 7.2 键设计

```java
// 使用有意义的键前缀，避免键冲突
// RedisCache 会自动为每个缓存实例添加名称前缀
config.setName("userCache");  // 生成的键类似: userCache:userId1

// 避免使用过长或过复杂的键

// 在集群模式下，使用hash tags确保相关数据在同一个节点上
// {user123}:profile 和 {user123}:settings 会分配到同一个节点
        cache.put("{user123}:profile",userProfile);
        cache.put("{user123}:settings",userSettings);
```

### 7.3 异常处理

```java
try{
        User user=cache.get("userId1");
        if(user==null){
        // 缓存未命中，降级到数据源
        user=loadUserFromDatabase("userId1");
        }
        return user;
        }catch(Exception e){
        logger.error("Cache operation failed",e);
        // 缓存操作失败，降级到数据源
        return loadUserFromDatabase("userId1");
        }
```

### 7.4 过期策略

```java
// 根据数据特性选择合适的过期策略
config.setExpireAfterWrite(TimeUnit.MINUTES.toMillis(30));  // 写入后30分钟过期
// 或者
        config.setExpireAfterAccess(TimeUnit.MINUTES.toMillis(10)); // 访问后10分钟过期
```

### 7.5 批量操作注意事项

```java
// 在Cluster模式下，批量操作的键应该在同一个节点上
// 使用hash tags确保路由到同一节点
Set<String> keys=new HashSet<>();
        keys.add("{user123}:profile");
        keys.add("{user123}:settings");
// 这些键会路由到同一个节点，可以正常执行
        Map<String, Object> result=cache.getAll(keys);
```

## 8. 监控和故障排除

### 8.1 监控建议

```java
// 定期输出缓存统计信息
ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedDelay(()->{
        try{
        CacheStats stats=cache.getStats();
        logger.info("RedisCache Stats - HitRate: {}%, Size: {}",
        String.format("%.2f",stats.getHitRate()*100),
        cache.size());
        }catch(Exception e){
        logger.error("Error getting cache stats",e);
        }
        },0,5,TimeUnit.MINUTES);
```

### 8.2 常见问题

1. **连接超时**：检查 Redis 服务器地址和端口是否正确，网络是否通畅
2. **连接池耗尽**：适当增加连接池大小或检查是否有连接泄漏
3. **序列化问题**：确保缓存的键值类型可以正确序列化和反序列化
4. **内存不足**：合理设置过期策略和最大内存限制
5. **集群重定向**：在集群模式下，确保键的分布合理，避免频繁的MOVED/ASK重定向

## 9. 性能优化建议

### 9.1 连接池优化

```java
// 根据应用负载调整连接池参数
GenericObjectPoolConfig<StatefulRedisConnection<String, String>>poolConfig=
        new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(50);           // 最大连接数
        poolConfig.setMaxIdle(20);            // 最大空闲连接数
        poolConfig.setMinIdle(5);             // 最小空闲连接数
        poolConfig.setTestOnBorrow(true);     // 借用时测试连接
        poolConfig.setTestOnReturn(true);     // 归还时测试连接
        poolConfig.setTestWhileIdle(true);    // 空闲时测试连接
```

### 9.2 批量操作优化

```java
// 尽量使用批量操作减少网络往返
// 一次性获取多个键的值
List<String> keys=Arrays.asList("key1","key2","key3");
        List<String> values=cache.getAll(new HashSet<>(keys)).values().stream()
        .map(Object::toString)
        .collect(Collectors.toList());
```

### 9.3 键空间优化

```java
// 合理设计键的命名空间，避免键名冲突
// 使用前缀区分不同业务模块
config.setName("user-service");  // 生成键: user-service:userId1
```

## 10. 适用场景

### 10.1 推荐使用场景

1. **分布式系统**：多个应用实例需要共享缓存数据
2. **大数据量缓存**：需要缓存大量数据，超过单机内存限制
3. **高可用要求**：对缓存数据的可靠性和持久性有要求
4. **集群部署**：应用部署在多个节点上，需要统一的缓存层
5. **需要持久化**：希望缓存数据在重启后能够恢复

### 10.2 不适用场景

1. **单机应用**：只有一个应用实例，对延迟要求极高的场景
2. **小数据量**：缓存数据量较小，本地缓存即可满足需求
3. **无网络环境**：无法连接到 Redis 服务器的环境
4. **严格一致性要求**：对数据一致性要求极高的场景（Redis是最终一致性）

## 11. 与本地缓存的对比

| 特性     | RedisCache       | 本地缓存(CaffeineCache) |
|--------|------------------|---------------------|
| 部署方式   | 需要 Redis 服务器     | 嵌入式，零依赖             |
| 性能     | 毫秒级访问延迟(网络开销)    | 微秒级访问延迟             |
| 数据存储   | 独立内存空间           | JVM 堆内存             |
| 扩展性    | 支持集群扩展           | 受限于单机 JVM           |
| 数据共享   | 多实例共享            | 仅当前实例访问             |
| 持久化    | 支持 RDB 和 AOF     | 不支持                 |
| 复杂数据结构 | 支持 Lists, Sets 等 | 仅基本 KV              |
| 高可用    | 支持主从、集群          | 无内置高可用              |

## 12. 总结

RedisCache 为 QTech IM 缓存管理组件提供了强大的分布式缓存能力，支持多种 Redis 部署模式。通过合理的配置和使用，可以显著提升分布式应用的性能和可扩展性。在使用时，需要根据具体场景选择合适的配置参数，并注意资源管理和异常处理。

### 12.1 选择建议

- **单机应用或对延迟要求极高**：使用 CaffeineCache 本地缓存
- **分布式应用且需要共享缓存数据**：使用 RedisCache
- **大数据量或需要持久化**：使用 RedisCache
- **高可用要求**：使用 RedisCache 集群模式

### 12.2 注意事项

1. 合理配置连接池参数，避免资源浪费或不足
2. 设计合理的键命名策略，避免键冲突
3. 在集群模式下注意键的分布，避免频繁重定向
4. 定期监控缓存性能和资源使用情况
5. 正确处理异常情况，确保系统稳定性

通过以上文档，您可以全面了解 RedisCache 的功能和使用方法，根据实际需求选择合适的配置和使用方式。

# 在Spring Boot中优雅使用im-common缓存管理组件

## 1. 配置类

### 1.1 RedisCache配置类

```java
package com.yourcompany.yourproject.config;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.impl.cache.RedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Configuration
public class ImCommonCacheConfig {

    @Value("${redis.cluster.nodes:}")
    private String redisClusterNodes;

    @Value("${redis.standalone.host:localhost}")
    private String redisHost;

    @Value("${redis.standalone.port:6379}")
    private int redisPort;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${cache.default.expire:1800000}")
    private long defaultExpire;

    @Value("${cache.default.max-size:1000}")
    private int maxSize;

    @Bean
    @Primary
    public Cache<String, Object> defaultCache() {
        CacheConfig config = new CacheConfig();
        config.setName("defaultCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setMaximumSize(maxSize);
        config.setExpireAfterWrite(defaultExpire);
        config.setRecordStats(true);

        // 构建 Redis URI
        String redisUri = buildRedisUri();
        config.setRedisUri(redisUri);

        return new RedisCache<>(config);
    }

    @Bean
    public Cache<String, String> stringCache() {
        CacheConfig config = new CacheConfig();
        config.setName("stringCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setMaximumSize(500);
        config.setExpireAfterWrite(TimeUnit.MINUTES.toMillis(10));
        config.setRecordStats(true);

        config.setRedisUri(buildRedisUri());
        return new RedisCache<>(config);
    }

    @Bean
    public Cache<Long, Object> longKeyCache() {
        CacheConfig config = new CacheConfig();
        config.setName("longKeyCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setMaximumSize(200);
        config.setExpireAfterWrite(TimeUnit.HOURS.toMillis(1));
        config.setRecordStats(true);

        config.setRedisUri(buildRedisUri());
        return new RedisCache<>(config);
    }

    private String buildRedisUri() {
        StringBuilder uriBuilder = new StringBuilder();

        if (StringUtils.hasText(redisPassword)) {
            uriBuilder.append("redis://:").append(redisPassword).append("@");
        } else {
            uriBuilder.append("redis://");
        }

        if (StringUtils.hasText(redisClusterNodes)) {
            // 集群模式
            uriBuilder.append(redisClusterNodes);
        } else {
            // 单节点模式
            uriBuilder.append(redisHost).append(":").append(redisPort);
        }

        return uriBuilder.toString();
    }
}
```

### 1.2 属性配置文件

在 `application.yml` 中添加配置：

```yaml
# Redis配置
redis:
  cluster:
    nodes: 192.168.1.10:6379,192.168.1.11:6379,192.168.1.12:6379
  standalone:
    host: localhost
    port: 6379
  password: ${REDIS_PASSWORD:}

# 缓存配置
cache:
  default:
    expire: 1800000  # 30分钟
    max-size: 1000
```

## 2. 缓存管理服务

```java
package com.yourcompany.yourproject.service;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class ImCommonCacheService {

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired
    private Cache<String, String> stringCache;

    @Autowired
    private Cache<Long, Object> longKeyCache;

    // 基本操作
    public <T> T get(String key, Class<T> type) {
        Object value = defaultCache.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void put(String key, Object value) {
        defaultCache.put(key, value);
    }

    public void put(String key, Object value, long ttl, TimeUnit unit) {
        defaultCache.put(key, value, ttl, unit);
    }

    public <T> T getOrLoad(String key, Function<String, T> loader, Class<T> type) {
        Object value = defaultCache.getOrLoad(key, (Function) loader);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public boolean remove(String key) {
        return defaultCache.remove(key);
    }

    public Map<String, Object> getAll(Set<String> keys) {
        return defaultCache.getAll(keys);
    }

    public void putAll(Map<String, Object> map) {
        defaultCache.putAll(map);
    }

    public boolean containsKey(String key) {
        return defaultCache.containsKey(key);
    }

    public long size() {
        return defaultCache.size();
    }

    public void clear() {
        defaultCache.clear();
    }

    public CacheStats getStats() {
        return defaultCache.getStats();
    }

    // 针对特定类型的缓存操作
    public String getString(String key) {
        return stringCache.get(key);
    }

    public void putString(String key, String value) {
        stringCache.put(key, value);
    }

    public Object getByLongKey(Long key) {
        return longKeyCache.get(key);
    }

    public void putByLongKey(Long key, Object value) {
        longKeyCache.put(key, value);
    }
}
```

## 3. 使用注解实现声明式缓存（使用实际存在的im-common注解）

### 3.1 缓存切面（使用实际的im-common注解）

```java
package com.yourcompany.yourproject.aspect;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ImCommonCacheAspect {

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired(required = false)
    private Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        String key = generateKey(cacheable.key(), joinPoint);
        String cacheName = cacheable.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 从缓存获取
        Object result = cache.get(key);
        if (result != null) {
            return result;
        }

        // 执行方法
        result = joinPoint.proceed();

        // 放入缓存
        if (cacheable.ttl() > 0) {
            cache.put(key, result, cacheable.ttl(), cacheable.ttlUnit());
        } else {
            cache.put(key, result);
        }

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CachePut cachePut = method.getAnnotation(CachePut.class);

        String key = generateKey(cachePut.key(), joinPoint);
        String cacheName = cachePut.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 执行方法
        Object result = joinPoint.proceed();

        // 放入缓存
        cache.put(key, result);

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CacheEvict)")
    public Object cacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        String key = generateKey(cacheEvict.key(), joinPoint);
        String cacheName = cacheEvict.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 清除缓存
        if (cacheEvict.allEntries()) {
            cache.clear();
        } else {
            cache.remove(key);
        }

        // 执行方法
        return joinPoint.proceed();
    }

    private String generateKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            // 默认使用方法名+参数作为键
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(joinPoint.getSignature().getName());
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                keyBuilder.append(":").append(arg);
            }
            return keyBuilder.toString();
        }

        // 使用SpEL表达式生成键
        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();

        // 设置参数变量
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return expression.getValue(context, String.class);
    }

    @SuppressWarnings("unchecked")
    private Cache<String, Object> getCacheByName(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            return defaultCache;
        }

        // 从缓存映射中获取
        Cache<?, ?> cache = cacheMap.get(cacheName);
        if (cache != null) {
            return (Cache<String, Object>) cache;
        }

        // 默认返回默认缓存
        return defaultCache;
    }

    // 注册缓存实例的方法
    public void registerCache(String name, Cache<?, ?> cache) {
        cacheMap.put(name, cache);
    }
}
```

### 3.2 启用AspectJ代理

```java
package com.yourcompany.yourproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {
}
```

### 3.3 在业务服务中使用im-common注解

```java
package com.yourcompany.yourproject.service;

import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    @Cacheable(cacheName = "defaultCache", key = "'product:' + #productId")
    public Product getProductById(String productId) {
        // 模拟数据库查询
        System.out.println("Loading product from database: " + productId);
        return new Product(productId, "Product " + productId);
    }

    @Cacheable(cacheName = "defaultCache", key = "'product:' + #productId", ttl = 30, timeUnit = TimeUnit.MINUTES)
    public Product getProductWithTtl(String productId) {
        // 模拟数据库查询
        System.out.println("Loading product from database with TTL: " + productId);
        return new Product(productId, "Product " + productId);
    }

    @CachePut(cacheName = "defaultCache", key = "'product:' + #product.id")
    public Product saveProduct(Product product) {
        // 模拟数据库保存
        System.out.println("Saving product to database: " + product);
        return product;
    }

    @CacheEvict(cacheName = "defaultCache", key = "'product:' + #productId")
    public void deleteProduct(String productId) {
        // 模拟数据库删除
        System.out.println("Deleting product from database: " + productId);
    }

    @CacheEvict(cacheName = "defaultCache", allEntries = true)
    public void clearAllProducts() {
        // 清空所有产品缓存
        System.out.println("Clearing all product cache");
    }
}
```

### 3.4 多缓存实例支持

如果需要支持多个缓存实例，可以扩展切面：

```java
package com.yourcompany.yourproject.aspect;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class ImCommonMultiCacheAspect {

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired(required = false)
    private Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        String key = generateKey(cacheable.key(), joinPoint);
        String cacheName = cacheable.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 从缓存获取
        Object result = cache.get(key);
        if (result != null) {
            return result;
        }

        // 执行方法
        result = joinPoint.proceed();

        // 放入缓存
        if (cacheable.ttl() > 0 && cacheable.timeUnit() != null) {
            cache.put(key, result, cacheable.ttl(), cacheable.timeUnit());
        } else {
            cache.put(key, result);
        }

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CachePut cachePut = method.getAnnotation(CachePut.class);

        String key = generateKey(cachePut.key(), joinPoint);
        String cacheName = cachePut.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 执行方法
        Object result = joinPoint.proceed();

        // 放入缓存
        cache.put(key, result);

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CacheEvict)")
    public Object cacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        String key = generateKey(cacheEvict.key(), joinPoint);
        String cacheName = cacheEvict.cacheName();

        // 获取对应的缓存实例
        Cache<String, Object> cache = getCacheByName(cacheName);

        // 清除缓存
        if (cacheEvict.allEntries()) {
            cache.clear();
        } else {
            cache.remove(key);
        }

        // 执行方法
        return joinPoint.proceed();
    }

    private String generateKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            // 默认使用方法名+参数作为键
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(joinPoint.getSignature().getName());
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                keyBuilder.append(":").append(arg);
            }
            return keyBuilder.toString();
        }

        // 使用SpEL表达式生成键
        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();

        // 设置参数变量
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return expression.getValue(context, String.class);
    }

    @SuppressWarnings("unchecked")
    private Cache<String, Object> getCacheByName(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            return defaultCache;
        }

        // 从缓存映射中获取
        Cache<?, ?> cache = cacheMap.get(cacheName);
        if (cache != null) {
            return (Cache<String, Object>) cache;
        }

        // 默认返回默认缓存
        return defaultCache;
    }

    // 注册缓存实例的方法
    public void registerCache(String name, Cache<?, ?> cache) {
        cacheMap.put(name, cache);
    }
}
```

## 4. 业务服务中使用

### 4.1 使用注入的缓存服务

```java
package com.yourcompany.yourproject.service;

import com.qtech.im.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private ImCommonCacheService cacheService;

    public User getUserById(String userId) {
        String cacheKey = "user:" + userId;

        // 使用缓存服务获取
        User user = cacheService.get(cacheKey, User.class);
        if (user != null) {
            return user;
        }

        // 缓存未命中，从数据库加载
        user = loadUserFromDatabase(userId);
        if (user != null) {
            cacheService.put(cacheKey, user);
        }

        return user;
    }

    public User getUserWithTtl(String userId) {
        String cacheKey = "user_ttl:" + userId;

        return cacheService.getOrLoad(cacheKey, id -> loadUserFromDatabase(userId), User.class);
    }

    public User saveUser(User user) {
        // 保存到数据库
        saveUserToDatabase(user);

        // 更新缓存
        String cacheKey = "user:" + user.getId();
        cacheService.put(cacheKey, user);

        return user;
    }

    public void deleteUser(String userId) {
        // 从数据库删除
        deleteUserFromDatabase(userId);

        // 从缓存删除
        String cacheKey = "user:" + userId;
        cacheService.remove(cacheKey);
    }

    private User loadUserFromDatabase(String userId) {
        // 模拟数据库查询
        return new User(userId, "User " + userId);
    }

    private void saveUserToDatabase(User user) {
        // 模拟数据库保存
        System.out.println("Saving user to database: " + user);
    }

    private void deleteUserFromDatabase(String userId) {
        // 模拟数据库删除
        System.out.println("Deleting user from database: " + userId);
    }
}
```

### 4.2 使用自定义注解

```java
package com.yourcompany.yourproject.service;

import com.yourcompany.yourproject.annotation.ImCacheEvict;
import com.yourcompany.yourproject.annotation.ImCachePut;
import com.yourcompany.yourproject.annotation.ImCacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    @ImCacheable(value = "defaultCache", key = "'product:' + #productId")
    public Product getProductById(String productId) {
        // 模拟数据库查询
        return new Product(productId, "Product " + productId);
    }

    @ImCacheable(value = "defaultCache", key = "'product:' + #productId", ttl = 30, timeUnit = TimeUnit.MINUTES)
    public Product getProductWithTtl(String productId) {
        // 模拟数据库查询
        return new Product(productId, "Product " + productId);
    }

    @ImCachePut(value = "defaultCache", key = "'product:' + #product.id")
    public Product saveProduct(Product product) {
        // 模拟数据库保存
        System.out.println("Saving product to database: " + product);
        return product;
    }

    @ImCacheEvict(value = "defaultCache", key = "'product:' + #productId")
    public void deleteProduct(String productId) {
        // 模拟数据库删除
        System.out.println("Deleting product from database: " + productId);
    }

    @ImCacheEvict(value = "defaultCache", allEntries = true)
    public void clearAllProducts() {
        // 清空所有产品缓存
        System.out.println("Clearing all product cache");
    }
}
```

## 5. 控制器中使用

```java
package com.yourcompany.yourproject.controller;

import com.qtech.im.cache.support.CacheStats;
import com.yourcompany.yourproject.service.ImCommonCacheService;
import com.yourcompany.yourproject.service.ProductService;
import com.yourcompany.yourproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CacheController {

    @Autowired
    private ImCommonCacheService cacheService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping("/user/{userId}")
    public User getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/product/{productId}")
    public Product getProduct(@PathVariable String productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/cache/stats")
    public CacheStats getCacheStats() {
        return cacheService.getStats();
    }

    @DeleteMapping("/cache/clear")
    public String clearCache() {
        cacheService.clear();
        return "Cache cleared";
    }

    @GetMapping("/cache/size")
    public long getCacheSize() {
        return cacheService.size();
    }
}
```

## 6. 应用启动和关闭处理

```java
package com.yourcompany.yourproject.config;

import com.qtech.im.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class CacheLifecycleHandler implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 应用启动时的初始化逻辑
        System.out.println("Cache system initialized");
    }

    // 处理应用关闭事件
    @Component
    public static class CacheShutdownHandler implements ApplicationListener<ContextClosedEvent> {

        @Autowired
        @Qualifier("defaultCache")
        private Cache<String, Object> defaultCache;

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            // 应用关闭时清理资源
            try {
                if (defaultCache != null) {
                    defaultCache.close();
                    System.out.println("Cache resources cleaned up");
                }
            } catch (Exception e) {
                System.err.println("Error closing cache: " + e.getMessage());
            }
        }
    }
}
```

## 7. 配置文件示例

### 7.1 application.yml

```yaml
# Redis配置
redis:
  cluster:
    nodes: 192.168.1.10:6379,192.168.1.11:6379,192.168.1.12:6379
  standalone:
    host: localhost
    port: 6379
  password: ${REDIS_PASSWORD:}

# 缓存配置
cache:
  default:
    expire: 1800000  # 30分钟
    max-size: 1000

# 启用AspectJ代理
spring:
  aop:
    proxy-target-class: true
```

## 8. 最佳实践总结

### 8.1 配置最佳实践

1. **外部化配置**：将缓存配置放在 `application.yml` 中
2. **环境区分**：使用不同的配置文件区分开发、测试、生产环境
3. **敏感信息保护**：使用环境变量存储密码等敏感信息

### 8.2 使用最佳实践

1. **类型安全**：使用泛型和类型检查确保类型安全
2. **键命名规范**：使用统一的键命名规范，避免冲突
3. **异常处理**：妥善处理缓存异常，避免影响主流程
4. **资源管理**：在应用关闭时正确释放缓存资源

### 8.3 监控最佳实践

1. **统计监控**：定期输出缓存统计信息
2. **性能监控**：监控缓存命中率和响应时间
3. **健康检查**：实现缓存健康检查端点

通过以上配置和实践，您可以在Spring Boot项目中优雅地使用im-common缓存管理组件，充分发挥其功能特性，同时保持与Spring生态的良好集成。

## 当使用第三方包中的注解时，特别是在Spring AOP中使用`@Around("@annotation(...))`时，可能需要额外的配置来确保Spring能正确识别和处理这些注解。

以下是需要考虑的几个方面和解决方案：

## 1. 确保注解可被扫描到

### 1.1 在主应用类或配置类上添加组件扫描

```java
package com.yourcompany.yourproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.yourcompany.yourproject",     // 你的项目包
        "com.qtech.im.cache.annotation"    // im-common注解包
})
public class YourSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourSpringBootApplication.class, args);
    }
}
```

## 2. 确保Aspect能处理第三方注解

### 2.1 显式启用AspectJ处理第三方注解

```java
package com.yourcompany.yourproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfig {
    // 启用CGLIB代理，确保能处理各种情况
}
```

## 3. 在切面类中显式引用注解

### 3.1 修改切面类以确保注解被正确加载

```java
package com.yourcompany.yourproject.aspect;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ImCommonCacheAspect {

    // 显式引用注解类，确保被类加载器加载
    static {
        try {
            Class.forName("com.qtech.im.cache.annotation.Cacheable");
            Class.forName("com.qtech.im.cache.annotation.CachePut");
            Class.forName("com.qtech.im.cache.annotation.CacheEvict");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load cache annotations", e);
        }
    }

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired(required = false)
    private Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        // ... 其余实现保持不变
        String key = generateKey(cacheable.key(), joinPoint);
        String cacheName = cacheable.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        Object result = cache.get(key);
        if (result != null) {
            return result;
        }

        result = joinPoint.proceed();

        if (cacheable.ttl() > 0) {
            cache.put(key, result, cacheable.ttl(), cacheable.ttlUnit());
        } else {
            cache.put(key, result);
        }

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CachePut cachePut = method.getAnnotation(CachePut.class);

        String key = generateKey(cachePut.key(), joinPoint);
        String cacheName = cachePut.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        Object result = joinPoint.proceed();
        cache.put(key, result);

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CacheEvict)")
    public Object cacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        String key = generateKey(cacheEvict.key(), joinPoint);
        String cacheName = cacheEvict.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        if (cacheEvict.allEntries()) {
            cache.clear();
        } else {
            cache.remove(key);
        }

        return joinPoint.proceed();
    }

    // 其余方法保持不变...
    private String generateKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(joinPoint.getSignature().getName());
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                keyBuilder.append(":").append(arg);
            }
            return keyBuilder.toString();
        }

        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return expression.getValue(context, String.class);
    }

    @SuppressWarnings("unchecked")
    private Cache<String, Object> getCacheByName(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            return defaultCache;
        }

        Cache<?, ?> cache = cacheMap.get(cacheName);
        if (cache != null) {
            return (Cache<String, Object>) cache;
        }

        return defaultCache;
    }

    public void registerCache(String name, Cache<?, ?> cache) {
        cacheMap.put(name, cache);
    }
}
```

## 4. Maven/Gradle依赖配置

### 4.1 确保im-common依赖正确引入

```xml
<!-- pom.xml -->
<dependencies>
    <!-- 其他依赖 -->

    <dependency>
        <groupId>com.qtech</groupId>
        <artifactId>im-common</artifactId>
        <version>${im-common.version}</version>
    </dependency>
</dependencies>
```

## 5. 可能需要的额外配置

### 5.1 如果遇到类加载问题，可以添加以下配置

```java
package com.yourcompany.yourproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "com.yourcompany.yourproject",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ,
                pattern = "com.qtech.im.cache.annotation.*"
        )
)
public class AnnotationConfig {
    // 确保注解能被正确扫描
}
```

## 6. 测试配置是否正确

### 6.1 创建一个简单的测试服务

```java
package com.yourcompany.yourproject.service;

import com.qtech.im.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TestCacheService {

    @Cacheable(cacheName = "defaultCache", key = "'test:' + #id")
    public String getCachedValue(String id) {
        System.out.println("Loading value for id: " + id);
        return "Value for " + id;
    }
}
```

### 6.2 创建测试控制器

```java
package com.yourcompany.yourproject.controller;

import com.yourcompany.yourproject.service.TestCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestCacheService testCacheService;

    @GetMapping("/cache/{id}")
    public String testCache(@PathVariable String id) {
        // 第一次调用应该打印"Loading value"
        String result1 = testCacheService.getCachedValue(id);

        // 第二次调用应该直接从缓存获取，不打印"Loading value"
        String result2 = testCacheService.getCachedValue(id);

        return "First: " + result1 + ", Second: " + result2;
    }
}
```

## 7. 调试和故障排除

### 7.1 如果切面不生效，可以添加调试日志

```java

@Aspect
@Component
public class ImCommonCacheAspect {
    private static final Logger logger = LoggerFactory.getLogger(ImCommonCacheAspect.class);

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.debug("Processing @Cacheable annotation for method: {}",
                joinPoint.getSignature().getName());
        // ... 其余实现
    }
}
```

通过以上配置，应该能够确保Spring Boot项目正确识别和处理im-common项目中的第三方缓存注解。关键点是确保注解类能被类加载器正确加载，并且Spring AOP能够正确拦截这些注解。

是的，你说得非常对。当使用第三方包中的注解时，特别是在Spring AOP中使用`@Around("@annotation(...))`时，可能需要额外的配置来确保Spring能正确识别和处理这些注解。

以下是需要考虑的几个方面和解决方案：

## 1. 确保注解可被扫描到

### 1.1 在主应用类或配置类上添加组件扫描

```java
package com.yourcompany.yourproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.yourcompany.yourproject",     // 你的项目包
        "com.qtech.im.cache.annotation"    // im-common注解包
})
public class YourSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourSpringBootApplication.class, args);
    }
}
```

## 2. 确保Aspect能处理第三方注解

### 2.1 显式启用AspectJ处理第三方注解

```java
package com.yourcompany.yourproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfig {
    // 启用CGLIB代理，确保能处理各种情况
}
```

## 3. 在切面类中显式引用注解

### 3.1 修改切面类以确保注解被正确加载

```java
package com.yourcompany.yourproject.aspect;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ImCommonCacheAspect {

    // 显式引用注解类，确保被类加载器加载
    static {
        try {
            Class.forName("com.qtech.im.cache.annotation.Cacheable");
            Class.forName("com.qtech.im.cache.annotation.CachePut");
            Class.forName("com.qtech.im.cache.annotation.CacheEvict");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load cache annotations", e);
        }
    }

    @Autowired
    @Qualifier("defaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired(required = false)
    private Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        // ... 其余实现保持不变
        String key = generateKey(cacheable.key(), joinPoint);
        String cacheName = cacheable.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        Object result = cache.get(key);
        if (result != null) {
            return result;
        }

        result = joinPoint.proceed();

        if (cacheable.ttl() > 0) {
            cache.put(key, result, cacheable.ttl(), cacheable.ttlUnit());
        } else {
            cache.put(key, result);
        }

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CachePut cachePut = method.getAnnotation(CachePut.class);

        String key = generateKey(cachePut.key(), joinPoint);
        String cacheName = cachePut.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        Object result = joinPoint.proceed();
        cache.put(key, result);

        return result;
    }

    @Around("@annotation(com.qtech.im.cache.annotation.CacheEvict)")
    public Object cacheEvict(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现保持不变
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        String key = generateKey(cacheEvict.key(), joinPoint);
        String cacheName = cacheEvict.cacheName();

        Cache<String, Object> cache = getCacheByName(cacheName);

        if (cacheEvict.allEntries()) {
            cache.clear();
        } else {
            cache.remove(key);
        }

        return joinPoint.proceed();
    }

    // 其余方法保持不变...
    private String generateKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(joinPoint.getSignature().getName());
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                keyBuilder.append(":").append(arg);
            }
            return keyBuilder.toString();
        }

        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return expression.getValue(context, String.class);
    }

    @SuppressWarnings("unchecked")
    private Cache<String, Object> getCacheByName(String cacheName) {
        if (cacheName == null || cacheName.isEmpty()) {
            return defaultCache;
        }

        Cache<?, ?> cache = cacheMap.get(cacheName);
        if (cache != null) {
            return (Cache<String, Object>) cache;
        }

        return defaultCache;
    }

    public void registerCache(String name, Cache<?, ?> cache) {
        cacheMap.put(name, cache);
    }
}
```

## 4. Maven/Gradle依赖配置

### 4.1 确保im-common依赖正确引入

```xml
<!-- pom.xml -->
<dependencies>
    <!-- 其他依赖 -->

    <dependency>
        <groupId>com.qtech</groupId>
        <artifactId>im-common</artifactId>
        <version>${im-common.version}</version>
    </dependency>
</dependencies>
```

## 5. 可能需要的额外配置

### 5.1 如果遇到类加载问题，可以添加以下配置

```java
package com.yourcompany.yourproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "com.yourcompany.yourproject",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ,
                pattern = "com.qtech.im.cache.annotation.*"
        )
)
public class AnnotationConfig {
    // 确保注解能被正确扫描
}
```

## 6. 测试配置是否正确

### 6.1 创建一个简单的测试服务

```java
package com.yourcompany.yourproject.service;

import com.qtech.im.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TestCacheService {

    @Cacheable(cacheName = "defaultCache", key = "'test:' + #id")
    public String getCachedValue(String id) {
        System.out.println("Loading value for id: " + id);
        return "Value for " + id;
    }
}
```

### 6.2 创建测试控制器

```java
package com.yourcompany.yourproject.controller;

import com.yourcompany.yourproject.service.TestCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestCacheService testCacheService;

    @GetMapping("/cache/{id}")
    public String testCache(@PathVariable String id) {
        // 第一次调用应该打印"Loading value"
        String result1 = testCacheService.getCachedValue(id);

        // 第二次调用应该直接从缓存获取，不打印"Loading value"
        String result2 = testCacheService.getCachedValue(id);

        return "First: " + result1 + ", Second: " + result2;
    }
}
```

## 7. 调试和故障排除

### 7.1 如果切面不生效，可以添加调试日志

```java

@Aspect
@Component
public class ImCommonCacheAspect {
    private static final Logger logger = LoggerFactory.getLogger(ImCommonCacheAspect.class);

    @Around("@annotation(com.qtech.im.cache.annotation.Cacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.debug("Processing @Cacheable annotation for method: {}",
                joinPoint.getSignature().getName());
        // ... 其余实现
    }
}
```

通过以上配置，应该能够确保Spring Boot项目正确识别和处理im-common项目中的第三方缓存注解。关键点是确保注解类能被类加载器正确加载，并且Spring AOP能够正确拦截这些注解。

# 在其他Spring Boot项目中使用im-common缓存模块的测试用例

## 1. 测试配置

### 1.1 测试配置类

```java
package com.yourcompany.yourproject.test.config;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.impl.cache.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestCacheConfiguration {

    @Bean
    @Primary
    public Cache<String, Object> testDefaultCache() {
        CacheConfig config = new CacheConfig();
        config.setName("testDefaultCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setMaximumSize(100);
        config.setExpireAfterWrite(60000); // 1分钟
        config.setRecordStats(true);
        config.setRedisUri("redis://localhost:6379");

        return new RedisCache<>(config);
    }

    @Bean
    public Cache<String, String> testStringCache() {
        CacheConfig config = new CacheConfig();
        config.setName("testStringCache");
        config.setCacheType(CacheConfig.CacheType.DISTRIBUTED);
        config.setMaximumSize(50);
        config.setExpireAfterWrite(30000); // 30秒
        config.setRecordStats(true);
        config.setRedisUri("redis://localhost:6379");

        return new RedisCache<>(config);
    }
}
```

### 1.2 测试实体类

```java
package com.yourcompany.yourproject.test.model;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private String id;
    private String name;
    private Double price;

    public Product() {
    }

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
        this.price = Math.random() * 100;
    }

    public Product(String id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                Objects.equals(name, product.name) &&
                Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
```

## 2. 服务层测试

### 2.1 使用编程方式的缓存服务测试

```java
package com.yourcompany.yourproject.test.service;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheStats;
import com.yourcompany.yourproject.test.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(com.yourcompany.yourproject.test.config.TestCacheConfiguration.class)
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CacheServiceTest {

    @Autowired
    @Qualifier("testDefaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired
    private Cache<String, String> stringCache;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        defaultCache.clear();
        stringCache.clear();
        executorService = Executors.newFixedThreadPool(5);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        defaultCache.clear();
        stringCache.clear();
    }

    @Test
    @Order(1)
    @DisplayName("测试基本缓存操作")
    void testBasicCacheOperations() {
        // Given
        String key = "basicTestKey";
        String value = "basicTestValue";

        // When & Then
        // 测试put和get
        defaultCache.put(key, value);
        String result = (String) defaultCache.get(key);
        assertEquals(value, result);

        // 测试containsKey
        assertTrue(defaultCache.containsKey(key));

        // 测试remove
        boolean removed = defaultCache.remove(key);
        assertTrue(removed);
        assertFalse(defaultCache.containsKey(key));
        assertNull(defaultCache.get(key));
    }

    @Test
    @Order(2)
    @DisplayName("测试对象缓存")
    void testObjectCache() {
        // Given
        String key = "product1";
        Product product = new Product("1", "Test Product", 99.99);

        // When
        defaultCache.put(key, product);
        Product result = (Product) defaultCache.get(key);

        // Then
        assertNotNull(result);
        assertEquals(product, result);
    }

    @Test
    @Order(3)
    @DisplayName("测试带TTL的缓存操作")
    void testCacheWithTTL() throws InterruptedException {
        // Given
        String key = "ttlTestKey";
        String value = "ttlTestValue";

        // When
        defaultCache.put(key, value, 1, java.util.concurrent.TimeUnit.SECONDS);
        String result1 = (String) defaultCache.get(key);

        // 等待过期
        Thread.sleep(1500);

        String result2 = (String) defaultCache.get(key);

        // Then
        assertEquals(value, result1);
        assertNull(result2);
    }

    @Test
    @Order(4)
    @DisplayName("测试批量操作")
    void testBatchOperations() {
        // Given
        Map<String, String> testData = new HashMap<>();
        testData.put("batch1", "value1");
        testData.put("batch2", "value2");
        testData.put("batch3", "value3");

        Set<String> keys = testData.keySet();

        // When
        defaultCache.putAll(testData);
        Map<String, String> results = (Map<String, String>) (Map<?, ?>) defaultCache.getAll(keys);

        // Then
        assertEquals(testData.size(), results.size());
        assertEquals(testData, results);
    }

    @Test
    @Order(5)
    @DisplayName("测试getOrLoad方法")
    void testGetOrLoad() {
        // Given
        String key = "loadTestKey";

        // When
        String result = (String) defaultCache.getOrLoad(key, k -> {
            // 模拟从数据库加载
            return "Loaded value for " + k;
        });

        String result2 = (String) defaultCache.get(key); // 第二次应该从缓存获取

        // Then
        assertEquals("Loaded value for loadTestKey", result);
        assertEquals("Loaded value for loadTestKey", result2);
    }

    @Test
    @Order(6)
    @DisplayName("测试缓存统计信息")
    void testCacheStats() {
        // Given
        CacheStats initialStats = defaultCache.getStats();

        // When
        defaultCache.get("nonExistentKey"); // 未命中
        defaultCache.put("statKey", "statValue");
        defaultCache.get("statKey"); // 命中

        CacheStats finalStats = defaultCache.getStats();

        // Then
        assertEquals(2, finalStats.getRequestCount() - initialStats.getRequestCount());
        assertEquals(1, finalStats.getHitCount() - initialStats.getHitCount());
        assertEquals(1, finalStats.getMissCount() - initialStats.getMissCount());
        assertTrue(finalStats.getHitRate() >= 0);
    }

    @Test
    @Order(7)
    @DisplayName("测试并发访问")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 50;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "concurrent:" + threadId + ":" + j;
                    String value = "value:" + threadId + ":" + j;

                    // 随机执行读写操作
                    if (j % 3 == 0) {
                        defaultCache.put(key, value);
                    } else if (j % 3 == 1) {
                        defaultCache.get(key);
                    } else {
                        defaultCache.getOrLoad(key, k -> "loaded:" + k);
                    }
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Then
        long cacheSize = defaultCache.size();
        assertTrue(cacheSize > 0, "缓存中应该有数据");
    }

    @Test
    @Order(8)
    @DisplayName("测试缓存大小和清理")
    void testCacheSizeAndClear() {
        // Given
        long initialSize = defaultCache.size();

        // When
        for (int i = 0; i < 20; i++) {
            defaultCache.put("sizeTest" + i, "value" + i);
        }

        long sizeAfterPut = defaultCache.size();
        defaultCache.clear();
        long sizeAfterClear = defaultCache.size();

        // Then
        assertEquals(0, initialSize);
        assertTrue(sizeAfterPut > 0);
        assertEquals(0, sizeAfterClear);
    }
}
```

### 2.2 使用注解方式的缓存服务测试

```java
package com.yourcompany.yourproject.test.service;

import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import com.qtech.im.cache.Cache;
import com.yourcompany.yourproject.test.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        com.yourcompany.yourproject.test.config.TestCacheConfiguration.class,
        CacheAnnotationServiceTest.TestCacheService.class
})
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CacheAnnotationServiceTest {

    @Service
    public static class TestCacheService {

        @Cacheable(cacheName = "testDefaultCache", key = "'product:' + #productId")
        public Product getProductById(String productId) {
            // 模拟从数据库加载
            return new Product(productId, "Product " + productId);
        }

        @Cacheable(cacheName = "testDefaultCache", key = "'product_ttl:' + #productId", ttl = 2, ttlUnit = java.util.concurrent.TimeUnit.SECONDS)
        public Product getProductWithTtl(String productId) {
            // 模拟从数据库加载
            return new Product(productId, "TTL Product " + productId);
        }

        @CachePut(cacheName = "testDefaultCache", key = "'product:' + #product.id")
        public Product saveProduct(Product product) {
            // 模拟保存到数据库
            return product;
        }

        @CacheEvict(cacheName = "testDefaultCache", key = "'product:' + #productId")
        public void deleteProduct(String productId) {
            // 模拟从数据库删除
        }

        @CacheEvict(cacheName = "testDefaultCache", allEntries = true)
        public void clearAllProducts() {
            // 模拟清空所有产品
        }
    }

    @Autowired
    private TestCacheService testCacheService;

    @Autowired
    private Cache<String, Object> testDefaultCache;

    @BeforeEach
    void setUp() {
        testDefaultCache.clear();
    }

    @AfterEach
    void tearDown() {
        testDefaultCache.clear();
    }

    @Test
    @Order(1)
    @DisplayName("测试@Cacheable注解")
    void testCacheableAnnotation() {
        // When
        Product product1 = testCacheService.getProductById("123");
        Product product2 = testCacheService.getProductById("123"); // 应该从缓存获取

        // Then
        assertNotNull(product1);
        assertNotNull(product2);
        assertEquals(product1, product2);
        assertEquals("Product 123", product1.getName());
    }

    @Test
    @Order(2)
    @DisplayName("测试@Cacheable注解带TTL")
    void testCacheableWithTtl() throws InterruptedException {
        // When
        Product product1 = testCacheService.getProductWithTtl("456");
        Product product2 = testCacheService.getProductWithTtl("456"); // 应该从缓存获取

        // 等待过期
        Thread.sleep(2500);

        Product product3 = testCacheService.getProductWithTtl("456"); // 应该重新加载

        // Then
        assertNotNull(product1);
        assertNotNull(product2);
        assertNotNull(product3);
        assertEquals(product1, product2);
        assertNotEquals(product1, product3); // 应该是重新加载的对象
    }

    @Test
    @Order(3)
    @DisplayName("测试@CachePut注解")
    void testCachePutAnnotation() {
        // Given
        Product product = new Product("789", "Cached Product", 199.99);

        // When
        Product result = testCacheService.saveProduct(product);
        Product cachedProduct = (Product) testDefaultCache.get("product:789");

        // Then
        assertEquals(product, result);
        assertEquals(product, cachedProduct);
    }

    @Test
    @Order(4)
    @DisplayName("测试@CacheEvict注解")
    void testCacheEvictAnnotation() {
        // Given
        testCacheService.getProductById("999"); // 先放入缓存
        Object cachedProduct1 = testDefaultCache.get("product:999");

        // When
        testCacheService.deleteProduct("999"); // 删除缓存
        Object cachedProduct2 = testDefaultCache.get("product:999");

        // Then
        assertNotNull(cachedProduct1);
        assertNull(cachedProduct2);
    }

    @Test
    @Order(5)
    @DisplayName("测试@CacheEvict注解清空所有")
    void testCacheEvictAllAnnotation() {
        // Given
        testCacheService.getProductById("111");
        testCacheService.getProductById("222");
        testCacheService.getProductById("333");

        long sizeBefore = testDefaultCache.size();

        // When
        testCacheService.clearAllProducts();
        long sizeAfter = testDefaultCache.size();

        // Then
        assertTrue(sizeBefore > 0);
        assertEquals(0, sizeAfter);
    }
}
```

## 3. 控制器层测试

```java
package com.yourcompany.yourproject.test.controller;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheStats;
import com.yourcompany.yourproject.test.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Import({
        com.yourcompany.yourproject.test.config.TestCacheConfiguration.class,
        CacheControllerTest.TestCacheController.class
})
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CacheControllerTest {

    @RestController
    @RequestMapping("/api/test/cache")
    public static class TestCacheController {

        @Autowired
        @Qualifier("testDefaultCache")
        private Cache<String, Object> defaultCache;

        @GetMapping("/product/{id}")
        public Product getProduct(@PathVariable String id) {
            return (Product) defaultCache.getOrLoad("product:" + id,
                    key -> new Product(id, "Product " + id));
        }

        @PostMapping("/product")
        public Product saveProduct(@RequestBody Product product) {
            defaultCache.put("product:" + product.getId(), product);
            return product;
        }

        @DeleteMapping("/product/{id}")
        public void deleteProduct(@PathVariable String id) {
            defaultCache.remove("product:" + id);
        }

        @GetMapping("/stats")
        public CacheStats getStats() {
            return defaultCache.getStats();
        }

        @DeleteMapping("/clear")
        public void clearCache() {
            defaultCache.clear();
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("testDefaultCache")
    private Cache<String, Object> defaultCache;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        defaultCache.clear();
    }

    @AfterEach
    void tearDown() {
        defaultCache.clear();
    }

    @Test
    @Order(1)
    @DisplayName("测试获取产品接口")
    void testGetProduct() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/test/cache/product/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Product 123"))
                .andDo(print());
    }

    @Test
    @Order(2)
    @DisplayName("测试保存产品接口")
    void testSaveProduct() throws Exception {
        // Given
        String productJson = "{\"id\":\"456\",\"name\":\"Test Product\",\"price\":99.99}";

        // When & Then
        mockMvc.perform(post("/api/test/cache/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("456"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andDo(print());

        // 验证缓存中已存在该产品
        Object cachedProduct = defaultCache.get("product:456");
        assertNotNull(cachedProduct);
        assertTrue(cachedProduct instanceof Product);
    }

    @Test
    @Order(3)
    @DisplayName("测试删除产品接口")
    void testDeleteProduct() throws Exception {
        // Given
        String productId = "789";
        defaultCache.put("product:" + productId, new Product(productId, "To Be Deleted"));

        // When & Then
        mockMvc.perform(delete("/api/test/cache/product/" + productId))
                .andExpect(status().isOk())
                .andDo(print());

        // 验证缓存中已删除该产品
        Object cachedProduct = defaultCache.get("product:" + productId);
        assertNull(cachedProduct);
    }

    @Test
    @Order(4)
    @DisplayName("测试获取缓存统计信息接口")
    void testGetStats() throws Exception {
        // Given
        defaultCache.get("testStatKey"); // 产生一次未命中

        // When & Then
        mockMvc.perform(get("/api/test/cache/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.missCount").value(greaterThanOrEqualTo(1)))
                .andDo(print());
    }

    @Test
    @Order(5)
    @DisplayName("测试清空缓存接口")
    void testClearCache() throws Exception {
        // Given
        defaultCache.put("toBeCleared", "someValue");
        long sizeBefore = defaultCache.size();

        // When & Then
        mockMvc.perform(delete("/api/test/cache/clear"))
                .andExpect(status().isOk())
                .andDo(print());

        long sizeAfter = defaultCache.size();
        assertTrue(sizeBefore > 0);
        assertEquals(0, sizeAfter);
    }
}
```

## 4. 集成测试

```java
package com.yourcompany.yourproject.test.integration;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.support.CacheStats;
import com.yourcompany.yourproject.test.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(com.yourcompany.yourproject.test.config.TestCacheConfiguration.class)
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CacheIntegrationTest {

    @Autowired
    @Qualifier("testDefaultCache")
    private Cache<String, Object> defaultCache;

    @Autowired
    private Cache<String, String> stringCache;

    @BeforeEach
    void setUp() {
        defaultCache.clear();
        stringCache.clear();
    }

    @AfterEach
    void tearDown() {
        defaultCache.clear();
        stringCache.clear();
    }

    @Test
    @Order(1)
    @DisplayName("测试大数据量缓存性能")
    void testLargeDataPerformance() {
        // Given
        int dataSize = 5000;
        List<String> keys = new ArrayList<>();

        // When - 批量放入
        long startTime = System.nanoTime();
        for (int i = 0; i < dataSize; i++) {
            String key = "perf:" + i;
            keys.add(key);
            defaultCache.put(key, new Product(String.valueOf(i), "Performance Test Product " + i));
        }
        long putTime = System.nanoTime() - startTime;

        // When - 批量获取
        startTime = System.nanoTime();
        int hitCount = 0;
        for (String key : keys) {
            Object value = defaultCache.get(key);
            if (value != null) {
                hitCount++;
            }
        }
        long getTime = System.nanoTime() - startTime;

        // Then
        assertEquals(dataSize, hitCount);
        assertTrue(putTime < TimeUnit.SECONDS.toNanos(10), "批量放入应该在10秒内完成");
        assertTrue(getTime < TimeUnit.SECONDS.toNanos(5), "批量获取应该在5秒内完成");
    }

    @Test
    @Order(2)
    @DisplayName("测试高并发场景")
    void testHighConcurrencyScenario() throws InterruptedException {
        // Given
        int threadCount = 20;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "concurrent:" + threadId + ":" + j;
                    Product product = new Product(key, "Concurrent Product " + j);

                    // 随机执行操作
                    int operation = j % 4;
                    switch (operation) {
                        case 0:
                            defaultCache.put(key, product);
                            break;
                        case 1:
                            defaultCache.get(key);
                            break;
                        case 2:
                            defaultCache.getOrLoad(key, k -> product);
                            break;
                        case 3:
                            defaultCache.remove(key);
                            break;
                    }
                }
            }, executor);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long totalTime = System.currentTimeMillis() - startTime;

        executor.shutdown();

        // Then
        assertTrue(totalTime < 30000, "高并发操作应该在30秒内完成");
        assertTrue(defaultCache.size() >= 0);
    }

    @Test
    @Order(3)
    @DisplayName("测试缓存统计准确性")
    void testCacheStatisticsAccuracy() {
        // Given
        int testIterations = 1000;
        CacheStats initialStats = defaultCache.getStats();

        // When
        for (int i = 0; i < testIterations; i++) {
            String key = "stats:" + i;
            Product product = new Product(String.valueOf(i), "Stats Test Product " + i);

            if (i % 2 == 0) {
                // 命中测试
                defaultCache.put(key, product);
                defaultCache.get(key);
            } else {
                // 未命中测试
                defaultCache.get(key);
            }
        }

        CacheStats finalStats = defaultCache.getStats();

        // Then
        assertEquals(testIterations, finalStats.getRequestCount() - initialStats.getRequestCount());
        assertTrue(finalStats.getHitCount() > 0);
        assertTrue(finalStats.getMissCount() > 0);
        assertTrue(finalStats.getHitRate() >= 0 && finalStats.getHitRate() <= 1);
    }

    @Test
    @Order(4)
    @DisplayName("测试多缓存实例隔离性")
    void testMultiCacheIsolation() {
        // Given
        String key = "isolationTest";
        String defaultValue = "default value";
        String stringValue = "string value";

        // When
        defaultCache.put(key, defaultValue);
        stringCache.put(key, stringValue);

        Object defaultResult = defaultCache.get(key);
        String stringResult = stringCache.get(key);

        // Then
        assertNotNull(defaultResult);
        assertEquals(defaultValue, defaultResult);
        assertEquals(stringValue, stringResult);

        // 清理一个缓存不应该影响另一个
        defaultCache.clear();

        Object defaultResultAfterClear = defaultCache.get(key);
        String stringResultAfterClear = stringCache.get(key);

        assertNull(defaultResultAfterClear);
        assertEquals(stringValue, stringResultAfterClear);
    }

    @Test
    @Order(5)
    @DisplayName("测试资源管理和优雅关闭")
    void testResourceManagement() {
        // Given
        for (int i = 0; i < 100; i++) {
            defaultCache.put("resource:" + i, new Product(String.valueOf(i), "Resource Test " + i));
        }

        long sizeBeforeClose = defaultCache.size();

        // When
        assertDoesNotThrow(() -> {
            // 执行一些操作
            CacheStats stats = defaultCache.getStats();
            assertNotNull(stats);
        });

        // Then
        assertTrue(sizeBeforeClose > 0);
    }
}
```

这些测试用例涵盖了在Spring Boot项目中使用im-common缓存模块的各种场景，包括基本功能测试、注解功能测试、控制器层测试和集成测试。通过这些测试可以确保im-common缓存模块在实际项目中能够正常工作并满足性能要求。