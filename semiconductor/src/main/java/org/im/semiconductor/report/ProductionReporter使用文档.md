# ProductionReporter 使用说明文档

## 1. 概述

[ProductionReporter](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\semiconductor\report\production\ProductionReporter.java#L10-L22)
是一个专业的生产报表生成工具类，专门用于半导体封测行业的各种生产报表生成、验证和分发。该工具解决了报表格式不统一、数据准确性差、生成效率低下和分发管理困难等问题。

## 2. 核心功能

### 2.1 标准化报表生成

- 支持多种报表类型（日报、周报、月报等）
- 多种输出格式（PDF、Excel、CSV等）
- 可配置的报表模板和参数

### 2.2 实时报表推送

- 多种分发方式（邮件、FTP、HTTP）
- 支持批量分发
- 分发状态跟踪

### 2.3 报表数据验证

- 数据完整性检查
- 格式规范验证
- 业务逻辑验证

### 2.4 报表历史查询

- 灵活的查询条件
- 分页和排序支持
- 报表统计分析

## 3. 支持的报表类型和格式

### 3.1 报表类型

```java
public enum ReportType {
    DAILY_PRODUCTION("Daily Production Report"),      // 日报
    WEEKLY_SUMMARY("Weekly Production Summary"),      // 周报
    MONTHLY_ANALYSIS("Monthly Production Analysis"),  // 月报
    QUALITY_REPORT("Quality Control Report"),         // 质量报告
    EQUIPMENT_UTILIZATION("Equipment Utilization Report"), // 设备利用率报告
    YIELD_ANALYSIS("Yield Analysis Report"),          // 良率分析报告
    CUSTOM_REPORT("Custom Report")                    // 自定义报告
}
```

### 3.2 报表格式

```java
public enum ReportFormat {
    PDF("pdf", "Portable Document Format"),
    EXCEL("xlsx", "Microsoft Excel"),
    CSV("csv", "Comma-Separated Values"),
    HTML("html", "HyperText Markup Language"),
    JSON("json", "JavaScript Object Notation"),
    XML("xml", "eXtensible Markup Language");
}
```

## 4. 基本使用方法

### 4.1 标准化报表生成

```java
// 创建报表配置
ProductionReporter.ReportConfig config = new ProductionReporter.ReportConfig()
    .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
    .setReportFormat(ProductionReporter.ReportFormat.PDF)
    .setReportTitle("2025-08-21 生产日报")
    .setReportDirectory("./reports")
    .addRecipient("manager@company.com")
    .setAutoDistribute(true)
    .setParameter("shift", "Day Shift")
    .setParameter("line", "Line A");

// 生成报表
ProductionReporter.GenerationResult result = ProductionReporter.generateStandardReport(config);

if (result.isSuccess()) {
    ProductionReporter.ProductionReport report = result.getReport();
    System.out.println("报表生成成功: " + report.getReportId());
    System.out.println("文件路径: " + report.getFilePath());
    System.out.println("文件大小: " + report.getFileSize() + " 字节");
} else {
    System.err.println("报表生成失败: " + result.getMessage());
    if (result.getException() != null) {
        result.getException().printStackTrace();
    }
}
```

### 4.2 实时报表推送

```java
// 获取已生成的报表
ProductionReporter.ProductionReport report = getGeneratedReport(); // 假设已有报表

// 创建分发配置
ProductionReporter.DistributionConfig distConfig = new ProductionReporter.DistributionConfig()
    .setEnableEmail(true)
    .addEmailRecipient("supervisor@company.com")
    .addEmailRecipient("quality@company.com")
    .setEmailSetting("subject", "生产报表 - " + new Date())
    .setEmailSetting("smtpServer", "smtp.company.com");

// 推送报表
List<ProductionReporter.DistributionResult> results = 
    ProductionReporter.distributeReport(report, distConfig);

for (ProductionReporter.DistributionResult result : results) {
    if (result.isSuccess()) {
        System.out.println("报表推送成功到: " + result.getRecipient());
    } else {
        System.err.println("报表推送失败到 " + result.getRecipient() + ": " + result.getMessage());
    }
}
```

### 4.3 报表数据验证

```java
// 获取报表进行验证
ProductionReporter.ProductionReport report = getReportToValidate();

// 验证报表数据
ProductionReporter.ValidationResult validationResult = 
    ProductionReporter.validateReportData(report);

if (validationResult.isValid()) {
    System.out.println("报表验证通过: " + validationResult.getMessage());
} else {
    System.err.println("报表验证失败: " + validationResult.getMessage());
    System.err.println("错误详情:");
    for (String error : validationResult.getErrors()) {
        System.err.println("  - " + error);
    }
}
```

### 4.4 报表历史查询

```java
// 创建查询条件
ProductionReporter.ReportQuery query = new ProductionReporter.ReportQuery()
    .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
    .setStartDate(LocalDateTime.of(2025, 8, 1, 0, 0))
    .setEndDate(LocalDateTime.of(2025, 8, 31, 23, 59))
    .setKeyword("Line A")
    .setPage(1)
    .setPageSize(20)
    .setSortBy("createTime")
    .setAscending(false);

// 查询历史报表
List<ProductionReporter.ProductionReport> reports = 
    ProductionReporter.queryHistoricalReports(query);

System.out.println("查询到 " + reports.size() + " 份报表:");
for (ProductionReporter.ProductionReport report : reports) {
    System.out.println("  - " + report.getReportTitle() + 
                      " (" + report.getCreateTime() + ")");
}
```

## 5. 高级使用示例

### 5.1 异步报表生成

```java
public class AsyncReportGenerator {
    public void generateReportAsync() {
        ProductionReporter.ReportConfig config = new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.MONTHLY_ANALYSIS)
            .setReportFormat(ProductionReporter.ReportFormat.EXCEL)
            .setReportTitle("2025年8月生产月报");
        
        // 异步生成报表
        ProductionReporter.generateReportAsync(config, new ProductionReporter.ReportGenerationCallback() {
            @Override
            public void onComplete(ProductionReporter.GenerationResult result) {
                if (result.isSuccess()) {
                    System.out.println("异步报表生成成功: " + result.getReport().getReportId());
                    // 进行后续处理，如分发报表
                    distributeGeneratedReport(result.getReport());
                } else {
                    System.err.println("异步报表生成失败: " + result.getMessage());
                }
            }
        });
        
        System.out.println("报表生成请求已提交，将在后台处理...");
    }
    
    private void distributeGeneratedReport(ProductionReporter.ProductionReport report) {
        // 分发生成的报表
        ProductionReporter.DistributionConfig distConfig = new ProductionReporter.DistributionConfig()
            .setEnableEmail(true)
            .addEmailRecipient("management@company.com");
        
        ProductionReporter.distributeReport(report, distConfig);
    }
}
```

### 5.2 批量报表生成

```java
public class BatchReportGenerator {
    public void generateBatchReports() {
        List<ProductionReporter.ReportConfig> configs = new ArrayList<>();
        
        // 添加日报配置
        configs.add(new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
            .setReportFormat(ProductionReporter.ReportFormat.PDF)
            .setReportTitle("2025-08-21 生产日报 - A线"));
        
        configs.add(new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
            .setReportFormat(ProductionReporter.ReportFormat.PDF)
            .setReportTitle("2025-08-21 生产日报 - B线"));
        
        configs.add(new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.QUALITY_REPORT)
            .setReportFormat(ProductionReporter.ReportFormat.EXCEL)
            .setReportTitle("2025-08-21 质量报告"));
        
        // 批量生成报表
        List<ProductionReporter.GenerationResult> results = 
            ProductionReporter.generateBatchReports(configs);
        
        System.out.println("批量报表生成完成:");
        int successCount = 0;
        for (int i = 0; i < results.size(); i++) {
            ProductionReporter.GenerationResult result = results.get(i);
            if (result.isSuccess()) {
                successCount++;
                System.out.println("  ✓ " + result.getReport().getReportTitle());
            } else {
                System.out.println("  ✗ 配置 " + (i+1) + ": " + result.getMessage());
            }
        }
        System.out.println("成功: " + successCount + "/" + results.size());
    }
}
```

### 5.3 报表统计分析

```java
public class ReportAnalyzer {
    public void analyzeReportStatistics() {
        // 获取报表统计信息
        ProductionReporter.ReportStatistics statistics = ProductionReporter.getReportStatistics();
        
        System.out.println("=== 报表统计分析 ===");
        System.out.println("总报表数: " + statistics.getTotalReports());
        System.out.println("已完成报表: " + statistics.getCompletedReports());
        System.out.println("失败报表: " + statistics.getFailedReports());
        System.out.println("总生成次数: " + statistics.getTotalGenerated());
        
        System.out.println("\n按类型分布:");
        statistics.getTypeDistribution().forEach((type, count) -> {
            System.out.println("  " + type.getDescription() + ": " + count);
        });
        
        System.out.println("\n按格式分布:");
        statistics.getFormatDistribution().forEach((format, count) -> {
            System.out.println("  " + format.getDescription() + ": " + count);
        });
    }
}
```

## 6. 配置参数详解

### 6.1 ReportConfig 配置项

```java
ProductionReporter.ReportConfig config = new ProductionReporter.ReportConfig()
    .setReportType(ReportType.DAILY_PRODUCTION)        // 报表类型
    .setReportFormat(ReportFormat.PDF)                 // 报表格式
    .setReportDate(LocalDateTime.now())                // 报表日期
    .setReportTitle("自定义标题")                        // 报表标题
    .setReportDirectory("./reports")                   // 报表存储目录
    .setParameter("customParam", "value")              // 自定义参数
    .addRecipient("user@company.com")                  // 接收者
    .setAutoDistribute(true)                           // 自动分发
    .setEnableValidation(true)                         // 启用验证
    .setMaxRetryAttempts(3)                            // 最大重试次数
    .setRetryDelay(5000)                               // 重试延迟(毫秒)
    .setTemplatePath("/templates/daily.ftl")           // 模板路径
    .setTimezone("Asia/Shanghai");                     // 时区
```

### 6.2 ReportQuery 查询条件

```java
ProductionReporter.ReportQuery query = new ProductionReporter.ReportQuery()
    .setReportType(ReportType.DAILY_PRODUCTION)        // 报表类型过滤
    .setStartDate(LocalDateTime.of(2025, 8, 1, 0, 0))  // 开始时间
    .setEndDate(LocalDateTime.of(2025, 8, 31, 23, 59)) // 结束时间
    .setKeyword("关键字搜索")                            // 关键字搜索
    .setPage(1)                                        // 页码
    .setPageSize(50)                                   // 每页大小
    .setSortBy("createTime")                           // 排序字段
    .setAscending(false);                              // 是否升序
```

### 6.3 DistributionConfig 分发配置

```java
ProductionReporter.DistributionConfig distConfig = new ProductionReporter.DistributionConfig()
    .setEnableEmail(true)                              // 启用邮件分发
    .addEmailRecipient("user1@company.com")            // 邮件接收者
    .addEmailRecipient("user2@company.com")
    .setEnableFtp(false)                               // 启用FTP分发
    .addFtpServer("ftp://server.company.com/reports")  // FTP服务器
    .setEnableHttp(true)                               // 启用HTTP分发
    .setHttpEndpoint("https://api.company.com/reports") // HTTP端点
    .setEmailSetting("smtpServer", "smtp.company.com") // 邮件设置
    .setEmailSetting("smtpPort", "587")
    .setFtpSetting("username", "ftpuser")              // FTP设置
    .setFtpSetting("password", "ftppass")
    .setHttpSetting("authToken", "bearer-token");      // HTTP设置
```

## 7. 错误处理

### 7.1 生成结果处理

```java
public class ReportErrorHandler {
    public void handleGenerationResult(ProductionReporter.GenerationResult result) {
        if (result.isSuccess()) {
            System.out.println("报表生成成功");
            ProductionReporter.ProductionReport report = result.getReport();
            // 处理成功结果
            processSuccessfulReport(report);
        } else {
            System.err.println("报表生成失败: " + result.getMessage());
            
            // 根据错误类型进行不同处理
            if (result.getException() != null) {
                if (result.getException() instanceof FileNotFoundException) {
                    System.err.println("模板文件未找到");
                } else if (result.getException() instanceof SecurityException) {
                    System.err.println("权限不足");
                } else {
                    System.err.println("未知错误: " + result.getException().getMessage());
                }
            }
            
            // 记录错误日志
            logError(result);
        }
    }
    
    private void processSuccessfulReport(ProductionReporter.ProductionReport report) {
        // 处理成功生成的报表
        System.out.println("报表ID: " + report.getReportId());
        System.out.println("生成时间: " + report.getGenerateTime());
        System.out.println("文件路径: " + report.getFilePath());
    }
    
    private void logError(ProductionReporter.GenerationResult result) {
        // 记录详细的错误信息
        System.err.println("错误时间: " + new Date(result.getGenerationTime()));
        if (result.getException() != null) {
            result.getException().printStackTrace();
        }
    }
}
```

## 8. 最佳实践

### 8.1 报表生成最佳实践

```java
public class ReportGenerationBestPractice {
    public void generateProductionReport() {
        try {
            // 1. 验证输入参数
            validateInputParameters();
            
            // 2. 创建详细的报表配置
            ProductionReporter.ReportConfig config = createDetailedConfig();
            
            // 3. 生成报表
            ProductionReporter.GenerationResult result = 
                ProductionReporter.generateStandardReport(config);
            
            // 4. 处理结果
            if (result.isSuccess()) {
                handleSuccessfulGeneration(result);
            } else {
                handleFailedGeneration(result);
            }
            
        } catch (Exception e) {
            System.err.println("报表生成过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private ProductionReporter.ReportConfig createDetailedConfig() {
        return new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
            .setReportFormat(ProductionReporter.ReportFormat.PDF)
            .setReportTitle(generateReportTitle())
            .setReportDirectory(getReportDirectory())
            .setMaxRetryAttempts(3)
            .setRetryDelay(5000)
            .setEnableValidation(true)
            .setParameter("generatedBy", "System")
            .setParameter("generatedTime", LocalDateTime.now())
            .addRecipient("production.manager@company.com")
            .addRecipient("quality.manager@company.com")
            .setAutoDistribute(true);
    }
    
    private String generateReportTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return "生产日报 - " + sdf.format(new Date());
    }
    
    private String getReportDirectory() {
        // 根据环境确定报表存储目录
        String env = System.getProperty("env", "dev");
        switch (env) {
            case "prod":
                return "/opt/reports/production";
            case "testing":
                return "/opt/reports/testing";
            default:
                return "./reports";
        }
    }
    
    private void handleSuccessfulGeneration(ProductionReporter.GenerationResult result) {
        ProductionReporter.ProductionReport report = result.getReport();
        System.out.println("报表生成成功:");
        System.out.println("  ID: " + report.getReportId());
        System.out.println("  标题: " + report.getReportTitle());
        System.out.println("  路径: " + report.getFilePath());
        System.out.println("  大小: " + report.getFileSize() + " 字节");
        
        // 记录成功日志
        logSuccess(report);
    }
    
    private void handleFailedGeneration(ProductionReporter.GenerationResult result) {
        System.err.println("报表生成失败:");
        System.err.println("  错误信息: " + result.getMessage());
        if (result.getException() != null) {
            System.err.println("  异常详情: " + result.getException().getMessage());
        }
        
        // 记录错误日志
        logFailure(result);
    }
    
    private void logSuccess(ProductionReporter.ProductionReport report) {
        // 实现日志记录逻辑
        System.out.println("[" + new Date() + "] 报表生成成功: " + report.getReportId());
    }
    
    private void logFailure(ProductionReporter.GenerationResult result) {
        // 实现错误日志记录逻辑
        System.err.println("[" + new Date() + "] 报表生成失败: " + result.getMessage());
    }
    
    private void validateInputParameters() {
        // 实现输入参数验证逻辑
        System.out.println("验证输入参数...");
    }
}
```

### 8.2 报表安全和权限控制

```java
public class SecureReportManager {
    public void generateReportWithSecurity(String userId, String sessionId) {
        // 1. 验证用户权限
        if (!hasReportGenerationPermission(userId, sessionId)) {
            System.err.println("用户没有生成报表的权限");
            return;
        }
        
        // 2. 创建安全的报表配置
        ProductionReporter.ReportConfig config = createSecureConfig(userId);
        
        // 3. 生成报表
        ProductionReporter.GenerationResult result = 
            ProductionReporter.generateStandardReport(config);
        
        // 4. 记录操作日志
        logReportGeneration(userId, result);
    }
    
    private boolean hasReportGenerationPermission(String userId, String sessionId) {
        // 集成访问控制管理器进行权限检查
        // 这里简化实现
        return userId != null && !userId.isEmpty();
    }
    
    private ProductionReporter.ReportConfig createSecureConfig(String userId) {
        return new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
            .setReportFormat(ProductionReporter.ReportFormat.PDF)
            .setReportTitle("安全报表 - " + new Date())
            .setParameter("generatedBy", userId)
            .setParameter("securityLevel", "CONFIDENTIAL");
    }
    
    private void logReportGeneration(String userId, ProductionReporter.GenerationResult result) {
        // 记录安全操作日志
        System.out.println("用户 " + userId + " 生成报表操作已记录");
        if (result.isSuccess()) {
            System.out.println("  报表ID: " + result.getReport().getReportId());
        } else {
            System.out.println("  失败原因: " + result.getMessage());
        }
    }
}
```

## 9. 系统集成

### 9.1 与调度系统的集成

```java
// 定时任务配置示例
@Component
public class ScheduledReportGenerator {
    
    // 每天早上8点生成日报
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyReport() {
        ProductionReporter.ReportConfig config = new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.DAILY_PRODUCTION)
            .setReportFormat(ProductionReporter.ReportFormat.PDF)
            .setReportDate(LocalDateTime.now().minusDays(1)) // 昨天的数据
            .setAutoDistribute(true)
            .addRecipient("production.manager@company.com")
            .addRecipient("plant.manager@company.com");
        
        ProductionReporter.GenerationResult result = 
            ProductionReporter.generateStandardReport(config);
        
        if (result.isSuccess()) {
            System.out.println("日报自动生成成功");
        } else {
            System.err.println("日报自动生成失败: " + result.getMessage());
        }
    }
    
    // 每周一早上9点生成周报
    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyReport() {
        ProductionReporter.ReportConfig config = new ProductionReporter.ReportConfig()
            .setReportType(ProductionReporter.ReportType.WEEKLY_SUMMARY)
            .setReportFormat(ProductionReporter.ReportFormat.EXCEL)
            .setAutoDistribute(true)
            .addRecipient("management@company.com");
        
        ProductionReporter.GenerationResult result = 
            ProductionReporter.generateStandardReport(config);
        
        // 处理结果...
    }
}
```

### 9.2 与Web服务的集成

```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@RequestBody ReportGenerationRequest request) {
        try {
            ProductionReporter.ReportConfig config = convertToReportConfig(request);
            ProductionReporter.GenerationResult result = 
                ProductionReporter.generateStandardReport(config);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(new ReportGenerationResponse(
                    result.getReport().getReportId(),
                    result.getReport().getFilePath(),
                    "报表生成成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(new ReportGenerationResponse(
                    null, null, "报表生成失败: " + result.getMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ReportGenerationResponse(
                null, null, "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<ProductionReporter.ProductionReport>> queryReports(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        ProductionReporter.ReportQuery query = new ProductionReporter.ReportQuery()
            .setReportType(type != null ? ReportType.valueOf(type) : null)
            .setStartDate(startDate)
            .setEndDate(endDate)
            .setPage(page)
            .setPageSize(pageSize);
        
        List<ProductionReporter.ProductionReport> reports = 
            ProductionReporter.queryHistoricalReports(query);
        
        return ResponseEntity.ok(reports);
    }
    
    private ProductionReporter.ReportConfig convertToReportConfig(ReportGenerationRequest request) {
        return new ProductionReporter.ReportConfig()
            .setReportType(ReportType.valueOf(request.getReportType()))
            .setReportFormat(ReportFormat.valueOf(request.getReportFormat()))
            .setReportTitle(request.getReportTitle())
            .setAutoDistribute(request.isAutoDistribute())
            .setRecipients(request.getRecipients());
    }
}
```

这个使用说明文档涵盖了 [ProductionReporter](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\semiconductor\report\production\ProductionReporter.java#L10-L22)
的主要功能和使用方法，可以帮助开发者快速上手并正确使用该报表生成工具类。