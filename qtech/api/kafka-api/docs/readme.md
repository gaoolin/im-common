### 配置项说明

1. server 配置

```
port：设置应用运行的 HTTP 端口。

servlet.context-path：设置应用的访问路径，默认为 /，可以根据需要调整。
```

2. tomcat 配置：

```
uri-encoding：确保 URL 编码格式是 UTF-8，避免字符乱码问题。

accept-count：设置最大排队连接数，当线程池已满时，新的请求会进入排队队列。增加此值可以防止请求被拒绝，但需要谨慎使用，避免过多占用内存。

threads.max：Tomcat 最大线程数。可以根据机器资源调整，如果资源有限，减少此值有助于减轻负担。

threads.min-spare：设置初始空闲线程数，减小此值可以减少空闲线程占用的内存。
```

3. spring.kafka 配置

```
bootstrap-servers：Kafka 集群的地址，用于连接 Kafka 服务。

producer 配置：

client-id：指定 Kafka 生产者的客户端 ID，便于区分不同的生产者实例。

key-serializer 和 value-serializer：设置 Kafka 生产者的消息序列化方式，这里选择了字符串序列化。

enable-idempotence：启用幂等性，确保同一条消息不会被重复发送。

acks：设置消息确认机制为 all，确保所有副本都确认接收。

retries 和 retry-backoff-ms：设置重试次数和重试间隔。

batch-size 和 linger-ms：批量发送配置，控制消息的发送效率，减少请求频率。

compression-type：设置消息压缩格式，减少网络带宽的消耗。

buffer-memory 和 max-request-size：控制生产者的缓存大小和请求大小，避免内存过载。
```

4. springdoc 配置

```
api-docs：启用 OpenAPI 文档生成功能，用于生成 API 文档。

openapi：API 文档的元数据，如标题、版本、描述等。
```

5. logging 配置

```
level：设置不同组件的日志级别。

root：设置根日志级别为 info，记录一般信息。

org.springframework.web：设置 Spring Web 模块的日志级别为 debug，以便调试时获得更多的详细信息。

org.apache.kafka：设置 Kafka 的日志级别为 warn，避免日志过多。
```

6. api.keys 配置

```
admin 和 user：设置 API 密钥，建议使用 Kubernetes Secrets 或环境变量来保护这些密钥。
```

7. exempt 配置

```
paths：指定哪些路径可以被豁免，避免对某些 API 进行认证等处理。
```

8. management 健康检查配置

```
health：开启磁盘空间健康检查，确保服务器有足够的磁盘空间。

endpoints：公开 /actuator/health 和 /actuator/info 端点供 Kubernetes 进行健康检查。
```

9. Kubernetes 健康检查配置

```
livenessProbe：确保容器运行正常，能够响应请求。

readinessProbe：确保容器已就绪，可以开始接受流量。
```

### 总结

此配置文件为你的应用提供了以下优化：

* 适配资源限制（2 核 CPU、2GB 内存），调整了 Tomcat 和 Kafka 的配置。_

* 启用了健康检查和就绪探针，帮助 Kubernetes 管理容器的生命周期。

* 配置了安全的 API 密钥和日志级别，确保敏感数据的安全和日志可调试。