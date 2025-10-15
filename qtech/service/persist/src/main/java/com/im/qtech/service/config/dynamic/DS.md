#### Spring Boot 3 + Java 21 + MyBatis-Plus 下的完整 动态数据源 + Mapper 自动扫描 + 分库切换 可用方案。方案特点：

* 支持多数据源

* 注解方式切换（类/方法）

* Mapper 自动注入，兼容 MyBatis-Plus

* 支持事务管理

#### 使用方式示例

```java
@Service
@DS(DSName.SECOND) // 类级别切换数据源
public class MsgService {

    @Autowired
    private WbOlpRawDataMapper mapper;

    @DS(DSName.THIRD) // 方法级别切换数据源
    public List<WbOlpRawData> queryThirdDb() {
        return mapper.selectList(null);
    }

    public List<WbOlpRawData> querySecondDb() {
        return mapper.selectList(null);
    }
}
```

#### 特点总结

* 命名简洁优雅：DSName, @DS, DSContextHolder, DSRoutingAspect, RoutingDataSource, DSConfig

* 线程安全：自动清理上下文，避免线程池交叉污染

* 注解切换数据源：类级/方法级灵活使用

* 支持 MyBatis-Plus 自动扫描 Mapper，SQLSessionTemplate + Mapper 完全兼容

* 统一事务管理：动态数据源事务管理一致

* 可扩展：新增数据源只需枚举 + Bean 配置即可

graph TD
A[Spring Boot Application] --> B[DSConfig（动态数据源配置）]
B --> B1[Primary RoutingDataSource Bean]
B --> B2[SqlSessionFactory Bean]
B --> B3[SqlSessionTemplate Bean]
B --> B4[TransactionManager Bean]
B --> B5[DataSource Beans: ds1, ds2, ds3]

    A --> C[Mapper Scan]
    C --> D[Mapper Interfaces (MyBatis-Plus BaseMapper)]
    D -->|依赖| B2[SqlSessionFactory Bean]

    A --> E[Service Layer]
    E -->|调用 Mapper| D
    E -->|@Transactional| B4[TransactionManager Bean]

    F[DataSourceSwitch Annotation] --> G[DataSourceAspect AOP]
    G -->|切换线程上下文| B1[RoutingDataSource Bean]

    style A fill:#f9f,stroke:#333,stroke-width:2px
    style B fill:#bbf,stroke:#333,stroke-width:2px
    style C fill:#bfb,stroke:#333,stroke-width:2px
    style D fill:#ffb,stroke:#333,stroke-width:2px
    style E fill:#fbf,stroke:#333,stroke-width:2px
    style F fill:#fbb,stroke:#333,stroke-width:2px
    style G fill:#bff,stroke:#333,stroke-width:2px

图解说明

DSConfig（动态数据源配置）

核心类，提供：

多个具体数据源 Bean（ds1, ds2, ds3）

RoutingDataSource（动态路由数据源）

SqlSessionFactory / SqlSessionTemplate

TransactionManager

Mapper Scan + Mapper Interfaces

@MapperScan 自动扫描 Mapper

Mapper 依赖 SqlSessionFactory，使用动态数据源

Service 层

调用 Mapper

使用 @Transactional 注解，事务管理由动态数据源处理

动态切换数据源

@DataSourceSwitch 注解 + DataSourceAspect AOP

切换当前线程的 RoutingDataSource