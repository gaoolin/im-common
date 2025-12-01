# HierarchicalCommandStructure 使用文档

## 1. 概述

[HierarchicalCommandStructure](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\semiconductor\equipment\qtech\HierarchicalCommandStructure.java#L34-L609)
是一个支持任意层级的命令结构类，专为设备参数管理和命令路由设计。它提供了完整的命令路径解析、层级管理、范围验证等功能。

### 1.1 主要特性

- **通用性**：支持任意层级的命令结构
- **规范性**：遵循命令结构标准
- **专业性**：提供专业的命令路由和解析能力
- **灵活性**：支持动态层级扩展
- **可靠性**：确保命令处理的稳定性
- **安全性**：防止命令注入攻击
- **复用性**：可被各种命令场景复用
- **容错性**：具备良好的错误处理能力

## 2. 核心组件

### 2.1 HierarchicalCommandStructure 主类

#### 2.1.1 构造方法

```java
// 默认构造函数
HierarchicalCommandStructure cmd1=new HierarchicalCommandStructure();

// 从命令路径构造
        HierarchicalCommandStructure cmd2=new HierarchicalCommandStructure("DEVICE.MOTION.MOVE");

// 从命令层级列表构造
        List<CommandLevel> levels=new ArrayList<>();
// ... 添加层级
        HierarchicalCommandStructure cmd3=new HierarchicalCommandStructure(levels);
```

#### 2.1.2 添加命令层级

```java
// 添加命令层级
cmd1.addLevel("DEVICE","设备命令");

// 添加带描述的命令层级
        cmd1.addLevel("MOTION","运动控制","控制设备运动的命令");

// 在指定位置插入命令层级
        cmd1.insertLevel(0,"SYSTEM","系统命令");

// 移除指定层级
        cmd1.removeLevel(1);
```

#### 2.1.3 查询命令层级

```java
// 获取指定层级
CommandLevel level=cmd1.getLevel(0);

// 获取根层级命令
        CommandLevel root=cmd1.getRootLevel();

// 获取叶层级命令
        CommandLevel leaf=cmd1.getLeafLevel();

// 获取命令层级数量
        int count=cmd1.getLevelCount();

// 获取所有命令层级
        List<CommandLevel> allLevels=cmd1.getCommandLevels();
```

#### 2.1.4 路径操作

```java
// 获取命令路径
String path=cmd1.getCommandPath(); // "DEVICE.MOTION.MOVE"

// 获取规范化路径
        String normalizedPath=cmd1.getNormalizedPath();

// 获取父路径
        String parentPath=cmd1.getParentPath();

// 获取子路径
        String subPath=cmd1.getSubPath(1); // 从第2个层级开始
```

#### 2.1.5 命令匹配

```java
List<HierarchicalCommandStructure> commandList=new ArrayList<>();
// ... 添加命令

// 精确匹配
        List<HierarchicalCommandStructure> exactMatches=cmd1.findMatchingCommands(
        commandList,
        HierarchicalCommandStructure.MatchStrategy.EXACT
        );

// 前缀匹配
        List<HierarchicalCommandStructure> prefixMatches=cmd1.findMatchingCommands(
        commandList,
        HierarchicalCommandStructure.MatchStrategy.PREFIX
        );

// 后缀匹配
        List<HierarchicalCommandStructure> suffixMatches=cmd1.findMatchingCommands(
        commandList,
        HierarchicalCommandStructure.MatchStrategy.SUFFIX
        );

// 包含匹配
        List<HierarchicalCommandStructure> containsMatches=cmd1.findMatchingCommands(
        commandList,
        HierarchicalCommandStructure.MatchStrategy.CONTAINS
        );
```

#### 2.1.6 验证功能

```java
// 验证命令结构
ValidationResult result=cmd1.validate();

        if(result.isValid()){
        System.out.println("命令结构有效");
        }else{
        System.out.println("验证错误: "+result.getErrors());
        }

        if(result.hasWarnings()){
        System.out.println("警告信息: "+result.getWarnings());
        }
```

### 2.2 CommandLevel 命令层级类

#### 2.2.1 属性说明

```java
public class CommandLevel {
    private String name;        // 层级名称
    private Object value;       // 层级值
    private String description; // 层级描述
    private int levelIndex;     // 层级索引
}
```

#### 2.2.2 使用示例

```java
// 创建命令层级
CommandLevel level1=new CommandLevel("DEVICE","设备命令",0);
        CommandLevel level2=new CommandLevel("MOTION","运动控制","控制设备运动",1);

// 获取属性
        String name=level1.getName();
        Object value=level1.getValue();
        String description=level1.getDescription();
        int index=level1.getLevelIndex();

// 设置属性
        level1.setName("NEW_DEVICE");
        level1.setValue("新设备命令");
        level1.setDescription("更新后的设备命令");
        level1.setLevelIndex(0);
```

### 2.3 CommandConfig 命令配置类

#### 2.3.1 配置选项

```java
public class CommandConfig {
    private String separator = ".";        // 分隔符
    private int maxCommandLength = 50;     // 最大命令长度
    private int maxPathLength = 500;       // 最大路径长度
    private boolean caseSensitive = false; // 是否区分大小写
    private List<String> reservedCommands; // 保留命令
}
```

#### 2.3.2 配置使用

```java
// 获取配置
CommandConfig config=cmd1.getConfig();

// 设置分隔符
        config.setSeparator("_");

// 设置最大命令长度
        config.setMaxCommandLength(100);

// 设置是否区分大小写
        config.setCaseSensitive(true);

// 添加保留命令
        config.addReservedCommand("SYSTEM");
        config.addReservedCommand("ADMIN");

// 检查是否为保留命令
        boolean isReserved=config.isReservedCommand("SYSTEM");
```

## 3. 实际应用示例

### 3.1 基本使用

```java
// 创建3层级命令结构
HierarchicalCommandStructure deviceCmd=new HierarchicalCommandStructure()
        .addLevel("DEVICE","设备命令")
        .addLevel("MOTION","运动控制")
        .addLevel("MOVE","移动");

        System.out.println("命令路径: "+deviceCmd.getCommandPath()); // DEVICE.MOTION.MOVE
        System.out.println("层级数量: "+deviceCmd.getLevelCount());   // 3

// 访问各个层级
        CommandLevel root=deviceCmd.getRootLevel();
        System.out.println("根层级: "+root.getName()); // DEVICE

        CommandLevel leaf=deviceCmd.getLeafLevel();
        System.out.println("叶层级: "+leaf.getName()); // MOVE
```

### 3.2 动态操作

```java
// 创建基础命令
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure()
        .addLevel("APP","应用程序")
        .addLevel("SERVICE","服务");

// 动态添加层级
        cmd.addLevel("CONTROLLER","控制器");

// 在指定位置插入
        cmd.insertLevel(2,"INTERCEPTOR","拦截器");

        System.out.println("当前路径: "+cmd.getCommandPath());
// APP.SERVICE.INTERCEPTOR.CONTROLLER

// 移除层级
        cmd.removeLevel(1);
        System.out.println("移除后路径: "+cmd.getCommandPath());
// APP.INTERCEPTOR.CONTROLLER
```

### 3.3 路径操作

```java
HierarchicalCommandStructure complexCmd=new HierarchicalCommandStructure()
        .addLevel("SYSTEM","系统")
        .addLevel("MODULE","模块")
        .addLevel("SUBMODULE","子模块")
        .addLevel("FUNCTION","功能")
        .addLevel("ACTION","动作");

// 获取不同部分的路径
        String fullPath=complexCmd.getCommandPath();
        System.out.println("完整路径: "+fullPath);
// SYSTEM.MODULE.SUBMODULE.FUNCTION.ACTION

        String parentPath=complexCmd.getParentPath();
        System.out.println("父路径: "+parentPath);
// SYSTEM.MODULE.SUBMODULE.FUNCTION

        String subPath=complexCmd.getSubPath(2);
        System.out.println("子路径(从索引2开始): "+subPath);
// SUBMODULE.FUNCTION.ACTION
```

### 3.4 命令匹配

```java
// 创建命令列表
List<HierarchicalCommandStructure> commands=Arrays.asList(
        new HierarchicalCommandStructure("APP.SERVICE.CONTROLLER.METHOD1"),
        new HierarchicalCommandStructure("APP.SERVICE.CONTROLLER.METHOD2"),
        new HierarchicalCommandStructure("APP.SERVICE.MODEL.DATA"),
        new HierarchicalCommandStructure("APP.CONFIG.SETTING.VALUE")
        );

// 创建搜索命令
        HierarchicalCommandStructure searchCmd=new HierarchicalCommandStructure("APP.SERVICE");

// 前缀匹配
        List<HierarchicalCommandStructure> matches=searchCmd.findMatchingCommands(
        commands,
        HierarchicalCommandStructure.MatchStrategy.PREFIX
        );

        System.out.println("匹配的命令数量: "+matches.size());
        for(HierarchicalCommandStructure match:matches){
        System.out.println("匹配命令: "+match.getCommandPath());
        }
```

### 3.5 验证和错误处理

```java
// 创建可能有问题的命令
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure()
        .addLevel("VERY_LONG_COMMAND_NAME_THAT_EXCEEDS_THE_MAXIMUM_LENGTH_LIMIT","超长命令")
        .addLevel("VALID","有效命令");

// 验证命令
        ValidationResult result=cmd.validate();

        if(!result.isValid()){
        System.out.println("验证失败:");
        for(String error:result.getErrors()){
        System.out.println("  错误: "+error);
        }
        }

        if(result.hasWarnings()){
        System.out.println("警告信息:");
        for(String warning:result.getWarnings()){
        System.out.println("  警告: "+warning);
        }
        }
```

### 3.6 配置自定义

```java
// 创建自定义配置的命令
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();
        CommandConfig config=cmd.getConfig();

// 自定义配置
        config.setSeparator("_");           // 使用下划线作为分隔符
        config.setMaxCommandLength(30);     // 设置最大命令长度
        config.setCaseSensitive(true);      // 区分大小写
        config.setMaxPathLength(200);       // 设置最大路径长度

// 添加保留命令
        config.addReservedCommand("SYSTEM");
        config.addReservedCommand("ADMIN");
        config.addReservedCommand("ROOT");

// 使用自定义配置创建命令
        cmd.addLevel("MySystem","我的系统")
        .addLevel("MyModule","我的模块")
        .addLevel("MyFunction","我的功能");

        System.out.println("自定义路径: "+cmd.getCommandPath());
// MySystem_MyModule_MyFunction (大写，因为设置了区分大小写但未启用)

        String normalized=cmd.getNormalizedPath();
        System.out.println("规范化路径: "+normalized);
```

## 4. 最佳实践

### 4.1 命令命名规范

```java
// 推荐的命名方式
HierarchicalCommandStructure goodCmd=new HierarchicalCommandStructure()
        .addLevel("DEVICE","设备相关命令")
        .addLevel("MOTION","运动控制")
        .addLevel("START","启动命令");

// 避免的命名方式
        HierarchicalCommandStructure badCmd=new HierarchicalCommandStructure()
        .addLevel("dev1","模糊的设备名")
        .addLevel("mot","不清晰的缩写")
        .addLevel("strt","难以理解的缩写");
```

### 4.2 性能优化

```java
// 批量操作时，避免频繁的路径重建
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();

// 好的做法：一次性添加所有层级
        cmd.addLevel("LEVEL1","第一层")
        .addLevel("LEVEL2","第二层")
        .addLevel("LEVEL3","第三层");

// 避免的做法：逐个添加（会触发多次路径重建）
// entity.addLevel("LEVEL1", "第一层");
// entity.addLevel("LEVEL2", "第二层");
// entity.addLevel("LEVEL3", "第三层");
```

### 4.3 错误处理

```java
try{
        // 可能抛出异常的操作
        HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();
        cmd.insertLevel(10,"NAME","VALUE"); // 索引越界
        }catch(IndexOutOfBoundsException e){
        System.err.println("索引越界: "+e.getMessage());
        }catch(IllegalArgumentException e){
        System.err.println("参数错误: "+e.getMessage());
        }

// 使用验证机制进行优雅的错误处理
        HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();
        ValidationResult result=cmd.validate();

        if(!result.isValid()){
        // 记录错误并采取相应措施
        logger.warn("命令验证失败: "+result.getErrors());
        // 可以选择修复错误或拒绝该命令
        }
```

### 4.4 线程安全

```java
// HierarchicalCommandStructure 是不可变的，线程安全
public class CommandService {
    private final HierarchicalCommandStructure baseCommand;

    public CommandService() {
        this.baseCommand = new HierarchicalCommandStructure()
                .addLevel("API", "API服务")
                .addLevel("V1", "版本1");
    }

    public HierarchicalCommandStructure createCommand(String action) {
        // 创建新的命令实例，保持原实例不变
        return new HierarchicalCommandStructure(baseCommand.getCommandLevels())
                .addLevel(action, "动作命令");
    }
}
```

## 5. 扩展和定制

### 5.1 继承扩展

```java
public class DeviceCommand extends HierarchicalCommandStructure {
    private String deviceId;
    private Date createTime;

    public DeviceCommand(String deviceId) {
        this.deviceId = deviceId;
        this.createTime = new Date();
    }

    public DeviceCommand addDeviceLevel(String name, String description) {
        return (DeviceCommand) this.addLevel(name, name, description);
    }

    // 自定义验证逻辑
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        if (deviceId == null || deviceId.isEmpty()) {
            result.addError("设备ID不能为空");
        }

        return result;
    }

    // Getter方法
    public String getDeviceId() {
        return deviceId;
    }

    public Date getCreateTime() {
        return createTime;
    }
}
```

### 5.2 自定义匹配策略

```java
public enum CustomMatchStrategy {
    WILDCARD,    // 通配符匹配
    REGEX,       // 正则表达式匹配
    SEMANTIC     // 语义匹配
}

public class AdvancedCommandMatcher {
    public static List<HierarchicalCommandStructure> match(
            HierarchicalCommandStructure pattern,
            List<HierarchicalCommandStructure> commands,
            CustomMatchStrategy strategy) {

        List<HierarchicalCommandStructure> result = new ArrayList<>();

        switch (strategy) {
            case WILDCARD:
                // 实现通配符匹配逻辑
                break;
            case REGEX:
                // 实现正则表达式匹配逻辑
                break;
            case SEMANTIC:
                // 实现语义匹配逻辑
                break;
        }

        return result;
    }
}
```

## 6. 常见问题解答

### 6.1 如何处理层级数量不确定的情况？

```java
// 可以动态添加任意数量的层级
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();

// 根据实际需要添加层级
        List<String> commandParts=Arrays.asList("A","B","C","D","E");
        for(int i=0;i<commandParts.size();i++){
        cmd.addLevel("LEVEL_"+i,commandParts.get(i));
        }

        System.out.println("层级数量: "+cmd.getLevelCount());
```

### 6.2 如何处理特殊字符？

```java
// 配置类会自动处理特殊字符
HierarchicalCommandStructure cmd=new HierarchicalCommandStructure();
        CommandConfig config=cmd.getConfig();

// 设置合适的分隔符避免冲突
        config.setSeparator("|"); // 使用管道符而不是点号

        cmd.addLevel("LEVEL.WITH.DOTS","包含点号的层级")
        .addLevel("LEVEL-WITH-DASHES","包含连字符的层级");

        System.out.println("安全的路径: "+cmd.getCommandPath());
```

### 6.3 如何进行性能监控？

```java
public class CommandPerformanceMonitor {
    private long creationCount = 0;
    private long validationCount = 0;
    private long matchCount = 0;

    public HierarchicalCommandStructure createCommand(String... levels) {
        creationCount++;
        HierarchicalCommandStructure cmd = new HierarchicalCommandStructure();
        for (String level : levels) {
            cmd.addLevel(level, level);
        }
        return cmd;
    }

    public ValidationResult validateCommand(HierarchicalCommandStructure cmd) {
        validationCount++;
        return cmd.validate();
    }

    public List<HierarchicalCommandStructure> matchCommands(
            HierarchicalCommandStructure pattern,
            List<HierarchicalCommandStructure> commands) {
        matchCount++;
        return pattern.findMatchingCommands(commands,
                HierarchicalCommandStructure.MatchStrategy.EXACT);
    }

    public void printStats() {
        System.out.println("命令创建次数: " + creationCount);
        System.out.println("验证次数: " + validationCount);
        System.out.println("匹配次数: " + matchCount);
    }
}
```

这份使用文档涵盖了 [HierarchicalCommandStructure](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\semiconductor\equipment\parameter\command\HierarchicalCommandStructure.java#L34-L609)
的所有主要功能和使用场景，可以帮助开发者快速上手并充分利用这个强大的命令结构管理工具。