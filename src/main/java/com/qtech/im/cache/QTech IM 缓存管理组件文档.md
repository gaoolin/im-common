# QTech IM 缓存管理组件文档

## 一、概述

QTech IM 缓存管理组件是一个高性能、易用的本地缓存解决方案，基于 Caffeine 缓存库构建。该组件提供了完整的缓存管理功能，包括缓存创建、配置、访问、统计和监控，同时内置了缓存穿透、击穿和雪崩保护机制。

## 二、核心功能

### 1. 缓存类型支持

- 本地缓存（基于 Caffeine）
- 分布式缓存（预留接口，需自定义实现）
- 混合缓存（预留接口，需自定义实现）

### 2. 缓存保护机制

- 缓存穿透保护：防止查询不存在的数据导致数据库压力
- 缓存击穿保护：防止热点数据失效瞬间大量请求打到数据库
- 缓存雪崩保护：防止大量缓存同时失效导致系统崩溃

### 3. 缓存统计和监控

- 请求统计（命中、未命中）
- 加载统计（成功、失败、时间）
- 驱逐统计
- 命中率计算

### 4. 过期策略

- 写入后过期（expireAfterWrite）
- 访问后过期（expireAfterAccess）
- 自定义过期时间

### 5. 注解支持

- [@Cacheable](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\cache\annotation\Cacheable.java#L25-L79)：缓存方法结果
- [@CachePut](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\cache\annotation\CachePut.java#L15-L62)：更新缓存
- [@CacheEvict](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\cache\annotation\CacheEvict.java#L14-L54)：清除缓存

## 三、架构设计

```
缓存管理模块
├── 核心接口层
│   ├── Cache 接口
│   ├── CacheManager 接口
│   └── CacheKeyGenerator 接口
├── 配置管理层
│   ├── CacheConfig 配置类
│   ├── CacheStats 统计类
│   └── CacheManagerStats 管理器统计类
├── 注解驱动层
│   ├── @Cacheable 注解
│   ├── @CachePut 注解
│   ├── @CacheEvict 注解
│   └── SimpleKeyGenerator 实现
└── 实现层
    ├── CaffeineCache 实现
    ├── ProtectedCache 保护包装器
    ├── LocalCacheManager 本地管理器
    └── SimpleCacheManager 简单管理器
```

## 四、使用指南

### 1. 基本使用

#### 创建缓存实例

```java
// 使用构建器模式创建缓存
Cache<String, Object> cache=CacheBuilder.newBuilder()
        .name("myCache")
        .maximumSize(1000)
        .expireAfterWrite(30,TimeUnit.MINUTES)
        .recordStats(true)
        .build();

// 或使用配置方式创建
        CacheConfig config=new CacheConfig();
        config.setName("myCache");
        config.setMaximumSize(1000);
        config.setExpireAfterWrite(TimeUnit.MINUTES.toMillis(30));
        Cache<String, Object> cache=new CaffeineCache<>(config);
```

#### 基本操作

```java
// 存储数据
cache.put("key1","value1");

// 获取数据
        String value=cache.get("key1");

// 删除数据
        cache.remove("key1");

// 批量操作
        Map<String, String> data=new HashMap<>();
        data.put("key1","value1");
        data.put("key2","value2");
        cache.putAll(data);
```

### 2. 高级功能

#### 自动加载

```java
// 获取或加载数据
User user=cache.getOrLoad("userId",key->loadUserFromDatabase(key));

// 获取或加载数据并设置过期时间
        User user=cache.getOrLoad("userId",key->loadUserFromDatabase(key),10,TimeUnit.MINUTES);
```

#### 缓存统计

```java
CacheStats stats=cache.getStats();
        System.out.println("命中率: "+stats.getHitRate());
        System.out.println("平均加载时间: "+stats.getAverageLoadTime()+"ms");
```

#### 缓存管理

```java
CacheManager manager=new LocalCacheManager();
        Cache<String, Object> cache=manager.createCache("myCache",config);
        Cache<String, Object> retrievedCache=manager.getCache("myCache");
        manager.removeCache("myCache");
```

### 3. 注解使用

```java
@Service
public class UserService {
    
    @Cacheable(cacheName = "userCache", key = "#userId")
    public User getUserById(String userId) {
        return loadUserFromDatabase(userId);
    }
    
    @CachePut(cacheName = "userCache", key = "#user.id")
    public User updateUser(User user) {
        saveUserToDatabase(user);
        return user;
    }
    
    @CacheEvict(cacheName = "userCache", key = "#userId")
    public void deleteUser(String userId) {
        deleteUserFromDatabase(userId);
    }
}
```

## 五、配置说明

### CacheConfig 主要配置项

| 配置项                       | 类型      | 默认值  | 说明               |
|---------------------------|---------|------|------------------|
| maximumSize               | int     | 1000 | 最大缓存大小           |
| expireAfterWrite          | long    | 30分钟 | 写入后过期时间（毫秒）      |
| expireAfterAccess         | long    | 0    | 访问后过期时间（毫秒）      |
| recordStats               | boolean | true | 是否记录统计信息         |
| enableNullValueProtection | boolean | true | 是否启用空值保护         |
| nullValueExpireTime       | long    | 1分钟  | 空值过期时间（毫秒）       |
| enableBreakdownProtection | boolean | true | 是否启用击穿保护         |
| breakdownLockTimeout      | long    | 10秒  | 击穿保护锁超时时间（毫秒）    |
| enableAvalancheProtection | boolean | true | 是否启用雪崩保护         |
| avalancheProtectionRange  | long    | 5分钟  | 雪崩保护随机过期时间范围（毫秒） |

## 六、注意事项

### 1. 内存管理

- 合理设置缓存大小，避免内存溢出
- 根据应用内存情况调整 maximumSize 参数
- 监控缓存统计信息，及时发现内存问题

### 2. 键的设计

- 确保缓存键的唯一性和一致性
- 避免使用过大的对象作为键
- 使用自定义键生成器处理复杂场景

### 3. 异常处理

- 缓存操作应有完善的异常处理机制
- 在缓存失效时降级到数据源访问
- 记录缓存操作异常日志

### 4. 资源释放

- 应用关闭时及时释放缓存资源
- 调用 cache.close() 方法清理资源
- 在 Spring 环境中使用 @PreDestroy 注解

## 七、性能优化建议

### 1. 缓存大小设置

根据应用内存和数据访问模式合理设置缓存大小，避免过大导致内存压力或过小导致频繁驱逐。

### 2. 过期策略选择

- 对于频繁更新的数据，使用 expireAfterWrite
- 对于访问模式可预测的数据，使用 expireAfterAccess
- 对于热点数据，考虑使用较长的过期时间

### 3. 统计监控

定期监控缓存统计信息，包括命中率、加载时间等指标，及时发现性能问题。

## 八、扩展性

### 1. 自定义缓存实现

可以通过实现 Cache 接口创建自定义缓存实现，如 Redis 缓存。

### 2. 自定义键生成器

实现 CacheKeyGenerator 接口可以创建自定义键生成逻辑。

### 3. 分布式缓存支持

虽然当前主要支持本地缓存，但架构设计支持扩展分布式缓存实现。

## 九、与 Redis 的对比

| 特性   | QTech IM 缓存组件 | Redis         |
|------|---------------|---------------|
| 部署方式 | 嵌入式           | 独立服务          |
| 性能   | 微秒级访问延迟       | 毫秒级访问延迟（网络开销） |
| 数据存储 | JVM内存         | 独立内存空间        |
| 扩展性  | 单机限制          | 集群支持          |
| 数据结构 | 基本KV          | 多种数据结构        |
| 持久化  | 不支持           | 支持RDB和AOF     |
| 依赖性  | 零依赖           | 需要Redis服务器    |

## 十、适用场景

### 推荐使用 QTech IM 缓存组件的场景：

1. 单体应用性能优化
2. 微服务本地缓存
3. 对延迟敏感的应用
4. 不希望引入外部依赖的项目
5. 开发测试环境

### 更适合使用 Redis 的场景：

1. 分布式系统数据共享
2. 大数据量缓存需求
3. 需要数据持久化
4. 复杂数据结构需求
5. 有专业运维团队支持

这套缓存组件为 Java 应用提供了高性能、易用的本地缓存解决方案，特别适用于单体应用和微服务的本地缓存需求。通过合理的配置和使用，可以显著提升应用性能。

基于对整个缓存系统的分析，我现在为您生成一个更完整和详细的架构图：

```mermaid
graph TD
    A[缓存管理模块] --> B[核心接口层]
    A --> C[配置管理层]
    A --> D[注解驱动层]
    A --> E[构建与工厂层]
    A --> F[实现层]
    A --> G[管理器层]
    A --> H[支持组件层]
    
    B --> B1[Cache接口]
    B --> B2[CacheManager接口]
    B --> B3[CacheKeyGenerator接口]
    
    C --> C1[CacheConfig配置类]
    C --> C2[CacheStats统计类]
    C --> C3[CacheManagerStats统计类]
    
    D --> D1[@Cacheable注解]
    D --> D2[@CachePut注解]
    D --> D3[@CacheEvict注解]
    D --> D4[CacheKeyGenerator接口]
    D --> D5[SimpleKeyGenerator实现]
    
    E --> E1[CacheBuilder构建器]
    E --> E2[CacheConfigBuilder配置构建器]
    E --> E3[CacheFactory工厂类]
    
    F --> F1[CaffeineCache实现]
    F --> F2[ProtectedCache包装器]
    
    G --> G1[LocalCacheManager实现]
    G --> G2[SimpleCacheManager实现]
    
    H --> H1[ExpiringValue过期值包装器]
    H --> H2[NullValueMarker空值标记]
    
    F1 --> B1
    F1 --> C1
    F1 --> C2
    
    F2 --> B1
    F2 --> C1
    F2 --> H1
    F2 --> H2
    
    E3 --> F1
    E3 --> F2
    E3 --> G2
    
    D1 -.-> D4
    D2 -.-> D4
    D3 -.-> D4
    D4 --> D5
    
    style A fill:#e1f5fe
    style B fill:#e8f5e8
    style C fill:#f3e5f5
    style D fill:#fff3e0
    style E fill:#e0f2f1
    style F fill:#fce4ec
    style G fill:#f1f8e9
    style H fill:#fff8e1
```

这个架构图展示了QTech IM缓存管理组件的完整架构，包括以下主要层次：

1. **核心接口层** - 定义了缓存系统的基本接口
2. **配置管理层** - 处理缓存的配置和统计信息
3. **注解驱动层** - 提供声明式缓存支持
4. **构建与工厂层** - 提供创建缓存实例的工具
5. **实现层** - 缓存的具体实现
6. **管理器层** - 缓存实例的管理
7. **支持组件层** - 提供辅助功能的组件

各个组件之间的关系清晰，体现了良好的分层设计和解耦思想，便于系统的维护和扩展。