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