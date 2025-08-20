# im-common 使用手册（更新版）

## 1. 简介

`im-common` 是一个轻量级的通用工具模块，提供常用工具类和配置，支持多 Java 版本（Java 8 和 Java 17），无 Spring 依赖。

## 2. 项目信息

- **groupId**: `com.qtech`
- **artifactId**: `im-common`
- **version**: `1.0.0`
- **packaging**: [jar](file://E:\dossier\others\im-common\target\im-common-1.0.0.jar)

## 3. 添加依赖

### Maven 项目

在 [pom.xml](file://E:\dossier\others\im-common\pom.xml) 中添加以下依赖：

```xml
<dependency>
    <groupId>com.qtech</groupId>
    <artifactId>im-common</artifactId>
    <version>1.0.0</version>
</dependency>
```


### Gradle 项目

在 `build.gradle` 中添加：

```gradle
dependencies {
    implementation 'com.qtech:im-common:1.0.0'
}
```


## 4. 构建与安装

### 本地构建

```bash
# 在 im-common 项目根目录执行
mvn clean install
```


### 发布到私有仓库

```bash
# 配置 settings.xml 后发布
mvn deploy

# 构建并发布到阿里仓库
mvn clean install org.apache.maven.plugins:maven-deploy-plugin:2.8:deploy -DskipTests
```


## 5. 核心功能

### 5.1 ObjectMapper 工具类

使用 `JsonMapperProvider` 进行 JSON 序列化和反序列化：

```java
import com.qtech.im.util.JsonMapperProvider;

// 获取共享实例
ObjectMapper mapper = JsonMapperProvider.getSharedInstance();

// JSON 转 Java 对象
String json = "{\"name\":\"张三\",\"age\":25}";
MyObject obj = mapper.readValue(json, MyObject.class);

// Java 对象转 JSON
MyObject obj = new MyObject("李四", 30);
String json = mapper.writeValueAsString(obj);
```


### 5.2 创建自定义配置的 ObjectMapper

```java
// 创建具有自定义配置的 ObjectMapper 实例
ObjectMapper customMapper = JsonMapperProvider.createCustomizedInstance(m -> {
    m.configure(SerializationFeature.INDENT_OUTPUT, true);
});

// 创建新的具有自定义配置的 ObjectMapper 实例
ObjectMapper customNewMapper = JsonMapperProvider.createCustomizedNewInstance(m -> {
    m.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
});
```


### 5.3 常用工具类

#### 常量类
```java
import java.com.qtech.constant.CommonConstants;
```


#### 异常处理
```java
import java.com.qtech.exception.BusinessException;
```


#### 工具类
```java
import java.com.qtech.util.StringUtils;
import java.com.qtech.util.DateUtils;
```


## 6. 多版本支持

### Java 8 版本
- 激活 profile: `im-jdk-8`
- 代码位于 `src/main/java/java8/` 目录

### Java 17 版本
- 激活 profile: `im-jdk-17`
- 代码位于 `src/main/java/java17/` 目录

## 7. 配置说明

### Jackson 配置
- 日期格式：`yyyy-MM-dd HH:mm:ss`
- 时区：`Asia/Shanghai`
- 自动包含非空字段
- 支持 Java 8 时间 API

### 日志配置
- 使用 SLF4J API
- 可配合具体日志实现（如 Logback、Log4j）

## 8. 使用示例

### 示例 1：JSON 处理

```java
public class JsonExample {
    public static void main(String[] args) throws Exception {
        // 创建对象
        User user = new User("张三", 25);
        
        // 转换为 JSON
        ObjectMapper mapper = JsonMapperProvider.getSharedInstance();
        String json = mapper.writeValueAsString(user);
        System.out.println(json); // {"name":"张三","age":25}
        
        // 从 JSON 反序列化
        User newUser = mapper.readValue(json, User.class);
        System.out.println(newUser.getName()); // 张三
    }
}
```


### 示例 2：字符串处理

```java
public class StringUtilsExample {
    public static void main(String[] args) {
        // 判断是否为空
        boolean isEmpty = StringUtils.isEmpty(null);
        boolean isBlank = StringUtils.isBlank("   ");
        
        // 字符串拼接
        String result = StringUtils.join(new String[]{"a", "b", "c"}, "-");
        System.out.println(result); // a-b-c
    }
}
```


## 9. 注意事项

1. **版本兼容性**：确保使用的 Java 版本与项目配置一致
2. **依赖管理**：注意 `provided` 依赖需要在运行环境提供
3. **日志实现**：SLF4J API 需要搭配具体的日志实现（如 Logback）
4. **配置文件**：资源文件位于 `src/main/resources` 目录

## 10. 联系方式

如有问题，请联系开发团队或查看项目文档。