# Protobuf 配置与代码生成操作文档

## 1. 准备工作

### 1.1 确保目录结构存在

```bash
# 创建 proto 文件目录
mkdir -p svc/main/proto
```

### 1.2 添加 Maven 插件依赖

在项目 `pom.xml` 中添加 Protobuf 插件：

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.github.os72</groupId>
            <artifactId>protoc-jar-maven-plugin</artifactId>
            <version>3.11.4</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <protocVersion>3.11.4</protocVersion>
                        <includeStdTypes>true</includeStdTypes>
                        <inputDirectories>
                            <include>src/main/proto</include>
                        </inputDirectories>
                        <outputTargets>
                            <outputTarget>
                                <type>java</type>
                                <outputDirectory>src/main/java</outputDirectory>
                            </outputTarget>
                        </outputTargets>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 2. 创建 Protobuf 定义文件

### 2.1 创建 [.proto](file://E:\dossier\others\im-framework\qtech\inspection\eq-aa-lst-chk\src\main\proto\eq_lst.proto) 文件

在 `src/main/proto/` 目录下创建 [eq_lst.proto](file://E:\dossier\others\im-framework\qtech\inspection\eq-aa-lst-chk\src\main\proto\eq_lst.proto) 文件：

```protobuf
syntax = "proto3";

option java_package = "com.im.aa.inspection.proto";
option java_outer_classname = "EqLstProto";

message EqLstPOJO {
  string module = 1;
  string aa1 = 2;
  string aa2 = 3;
  string aa3 = 4;
  string backToPosition = 5;
  string blemish = 6;
  // 添加其他字段...
}
```

## 3. 生成 Java 代码

### 3.1 清理并生成代码

```bash
# 清理之前的构建并重新生成
mvn clean generate-sources
```

### 3.2 验证生成结果

生成的 Java 文件应该位于：

```
src/main/java/com/im/aa/inspection/proto/EqLstProto.java
```

## 4. 更新序列化相关类

### 4.1 更新 [EqLstProtoConverter](file://E:\dossier\others\im-framework\qtech\inspection\eq-aa-lst-chk\src\main\java\com\im\aa\inspection\serializer\EqLstProtoConverter.java#L8-L37) 类

确保导入正确的包：

```java
import com.im.aa.inspection.proto.EqLstProto;
```

### 4.2 更新 [EqLstProtobufMapper](file://E:\dossier\others\im-framework\qtech\inspection\eq-aa-lst-chk\src\main\java\com\im\aa\inspection\serializer\EqLstProtobufMapper.java#L10-L29) 类

确保导入正确的包：

```java
import com.im.aa.inspection.proto.EqLstProto;
```

## 5. 常见问题排查

### 5.1 目录不存在

如果提示目录不存在，手动创建：

```bash
mkdir -p svc/main/proto
```

### 5.2 文件未更新

如果插件跳过代码生成，执行清理命令：

```bash
mvn clean generate-sources
```

### 5.3 符号无法解析

检查以下几点：

1. 确保 [.proto](file://E:\dossier\others\im-framework\qtech\inspection\eq-aa-lst-chk\src\main\proto\eq_lst.proto) 文件正确创建
2. 确保 Maven 插件配置正确
3. 执行 `mvn clean compile` 重新编译项目

## 6. 验证操作结果

### 6.1 检查生成的类

确认 `EqLstProto` 类已生成并可被正常引用

### 6.2 测试序列化功能

```java
// 测试序列化和反序列化功能
EqLstPOJO obj=new EqLstPOJO();
        byte[]data=EqLstProtobufMapper.serialize(obj);
        EqLstPOJO deserialized=EqLstProtobufMapper.deserialize(data);
```

完成以上步骤后，Protobuf 配置和代码生成应该能正常工作。