# IM Framework - 半导体智能制造通用框架

## 项目概述

IM Framework 是一个专为半导体智能制造行业设计的通用技术框架，专注于大数据建模、设备数据采集、存储、处理和应用。该框架提供了一套完整的解决方案，帮助企业在半导体制造过程中实现数据驱动的智能化生产和管理。

## 核心功能模块

### 1. 半导体行业专用工具类库

提供针对半导体制造场景的专业工具组件：

- **BatchProcessor**: 高效的批量数据处理工具，支持分批处理和并行处理大规模数据
- **DataQualityChecker**: 数据质量检查工具，确保制造数据的完整性、一致性和准确性
- **WaferMapParser**: 专业的Wafer Map解析工具，用于处理晶圆图数据
- **ProductionContext**: 生产上下文信息管理，包含公司、厂区、设备等完整的生产环境信息

### 2. ORM持久层框架

轻量级ORM框架，支持多数据源和高性能连接池：

- 多数据源管理支持
- 基于HikariCP的高性能连接池
- 简洁的实体映射和CRUD操作
- 事务管理支持

### 3. 上下文信息管理

标准化的半导体制造环境信息接口：

- **DeviceInfo**: 设备信息接口，管理设备标识、类型、状态等信息
- **LocationInfo**: 位置信息接口，描述公司、厂区、车间等地理位置信息
- 统一的数据验证和转换方法

### 4. 线程池管理

智能线程池框架，专为IoT和智能制造场景设计：

- 多优先级任务支持（重要、普通、低优先级）
- 虚拟线程兼容（支持Java 21）
- 智能拒绝策略
- 运行状态监控

### 5. ETL数据处理

完整的数据抽取、转换和加载解决方案：

- 多样化的ETL模块支持
- 设备上下文数据处理
- BOM数据处理
- 设备状态流处理

## 项目结构

```
im-framework/
├── common/              # 通用工具模块
├── config/              # 配置管理模块
├── core/                # 核心基础模块
├── orm/                 # 对象关系映射框架
├── cache/               # 缓存管理模块
├── integration/         # 集成适配模块
├── semiconductor/       # 半导体行业专用模块
├── qtech/               # 丘钛微定制模块
├── etl/                 # 数据抽取转换加载模块
└── storage-service/     # 存储服务模块
```

## 半导体行业应用场景

### 1. 设备数据采集与质量管控

```java
// 数据采集与质量检查示例
public void collectAndProcessEquipmentData(String equipmentId){
        // 1. 采集设备数据
        List<EquipmentData> rawData=collectRawData(equipmentId);

        // 2. 检查数据质量
        DataQualityChecker.CompletenessResult qualityResult=
        DataQualityChecker.checkCompleteness(rawData,
        data->data!=null&&data.isValid(),0.95);

        // 3. 批量处理数据
        List<BatchProcessor.BatchResult<EquipmentData>>batchResults=
        BatchProcessor.processInBatches(rawData,1000,this::processBatch);
        }
```

### 2. Wafer测试数据分析

```java
// Wafer数据分析示例
public void analyzeWaferTestResults(String waferMapFilePath){
        // 1. 解析Wafer Map
        WaferMapParser.WaferMap waferMap=WaferMapParser.parseWaferMap(
        waferMapFilePath,WaferMapParser.WaferMapFormat.AUTO_DETECT);

        // 2. 生成分析报告
        WaferMapParser.WaferMapReport report=WaferMapParser.generateReport(waferMap);

        // 3. 质量检查与警报
        if(report.getYield()< 0.8){
        triggerQualityAlert(waferMap,report);
        }
        }
```

### 3. 生产上下文管理

```java
// 生产环境上下文示例
ProductionContext context=new ProductionContext();
        context.setCompany("ABC Semiconductor");
        context.setSite("Suzhou Factory");
        context.setEqpType("Etching Machine");
        context.setProduct("Sensor Chip");
        context.setLot("LOT20250101001");
```

## 最佳实践

### 性能优化建议

1. **批量处理**: 使用[BatchProcessor](file://E:\dossier\others\im-framework\qtech\service\msg-queue-persist\src\main\java\com\qtech\im\common\batch\BatchProcessor.java#L31-L199)
   处理大量数据以提高性能
2. **并行计算**: 合理使用并行处理提升计算密集型任务性能
3. **内存管理**: 注意大文件和大数据集的内存使用情况
4. **缓存机制**: 适当使用缓存减少重复计算

### 错误处理策略

1. **异常分类**:
   使用[SemiconductorException](file://E:\dossier\others\im-framework\qtech\service\msg-queue-persist\src\main\java\com\qtech\im\exception\SemiconductorException.java#L21-L173)
   进行精确异常处理
2. **恢复机制**: 实现故障恢复和重试逻辑
3. **日志记录**: 详细记录操作日志便于问题排查
4. **监控告警**: 建立完善的监控和告警机制

## 技术特点

- **轻量级设计**: 无Spring依赖，支持多Java版本
- **模块化架构**: 高内聚低耦合，组件可独立使用
- **行业专业化**: 针对半导体制造场景深度优化
- **高可靠性**: 完善的错误处理和数据验证机制
- **易于扩展**: 清晰的接口设计，便于功能扩展

## 适用场景

- 半导体制造企业数据平台建设
- 智能工厂设备数据采集系统
- 生产过程质量监控与分析
- 制造执行系统(MES)开发
- 设备维护与预测性分析系统

## 开始使用

请参考各模块下的详细文档和使用示例，了解具体功能的使用方法和最佳实践。