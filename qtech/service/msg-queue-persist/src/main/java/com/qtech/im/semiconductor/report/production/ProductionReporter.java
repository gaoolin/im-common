package com.qtech.im.semiconductor.report.production;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 生产报表生成工具类
 * <p>
 * 特性：
 * - 通用性：支持多种报表格式和数据源
 * - 规范性：统一的报表结构和生成流程
 * - 专业性：半导体生产报表专业模板和计算
 * - 灵活性：可配置的报表内容和分发方式
 * - 可靠性：完善的错误处理和数据验证机制
 * - 安全性：报表数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/21
 */
public class ProductionReporter {

    // 默认配置
    public static final String DEFAULT_REPORT_FORMAT = "PDF";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_REPORT_DIRECTORY = "./reports";
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 5000; // 5秒
    private static final Logger logger = LoggerFactory.getLogger(ProductionReporter.class);
    // 内部存储（实际应用中应使用数据库）
    private static final Map<String, ProductionReport> reportStorage = new ConcurrentHashMap<>();
    private static final AtomicLong reportCounter = new AtomicLong(0);
    // 线程池用于异步处理
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    // 报表生成器注册表
    private static final Map<ReportFormat, ReportGenerator> generatorRegistry = new ConcurrentHashMap<>();
    // 报表分发器注册表
    private static final Map<String, ReportDistributor> distributorRegistry = new ConcurrentHashMap<>();
    // 报表验证器注册表
    private static final Map<ReportType, ReportValidator> validatorRegistry = new ConcurrentHashMap<>();

    // 初始化默认组件
    static {
        // 注册默认生成器、分发器和验证器
        registerDefaultComponents();
        logger.info("ProductionReporter initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认生成器
        // registerGenerator(new PdfReportGenerator());
        // registerGenerator(new ExcelReportGenerator());
        // registerGenerator(new CsvReportGenerator());

        // 注册默认分发器
        // registerDistributor(new EmailReportDistributor());
        // registerDistributor(new FtpReportDistributor());
        // registerDistributor(new HttpReportDistributor());

        // 注册默认验证器
        // registerValidator(new ProductionReportValidator());
        // registerValidator(new QualityReportValidator());
    }

    /**
     * 注册报表生成器
     *
     * @param generator 报表生成器
     */
    public static void registerGenerator(ReportGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator cannot be null");
        }

        for (ReportFormat format : ReportFormat.values()) {
            if (generator.supportsFormat(format)) {
                generatorRegistry.put(format, generator);
                logger.debug("Registered generator {} for format {}", generator.getGeneratorName(), format);
            }
        }
    }

    /**
     * 注册报表分发器
     *
     * @param distributor 报表分发器
     */
    public static void registerDistributor(ReportDistributor distributor) {
        if (distributor == null) {
            throw new IllegalArgumentException("Distributor cannot be null");
        }

        // 注册支持的所有分发方法
        String[] methods = {"email", "ftp", "http"}; // 简化实现
        for (String method : methods) {
            if (distributor.supportsMethod(method)) {
                distributorRegistry.put(method, distributor);
                logger.debug("Registered distributor {} for method {}", distributor.getDistributorName(), method);
            }
        }
    }

    /**
     * 注册报表验证器
     *
     * @param validator 报表验证器
     */
    public static void registerValidator(ReportValidator validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }

        for (ReportType type : ReportType.values()) {
            if (validator.supportsType(type)) {
                validatorRegistry.put(type, validator);
                logger.debug("Registered validator {} for type {}", validator.getValidatorName(), type);
            }
        }
    }

    /**
     * 标准化报表生成
     *
     * @param config 报表配置
     * @return 生成结果
     */
    public static GenerationResult generateStandardReport(ReportConfig config) {
        if (config == null) {
            String message = "Invalid report configuration";
            logger.error(message);
            return new GenerationResult(false, null, message, null);
        }

        ProductionReport report = null;
        Exception lastException = null;

        // 重试机制
        for (int attempt = 0; attempt <= config.getMaxRetryAttempts(); attempt++) {
            try {
                logger.info("Generating report attempt {}/{}: {}",
                        attempt + 1, config.getMaxRetryAttempts() + 1,
                        config.getReportType().getDescription());

                // 1. 创建报表对象
                report = createReport(config);
                if (report == null) {
                    throw new RuntimeException("Failed to create report object");
                }

                // 2. 填充报表数据
                populateReportData(report, config);

                // 3. 生成报表文件
                byte[] reportData = generateReportData(report, config);
                if (reportData == null || reportData.length == 0) {
                    throw new RuntimeException("Failed to generate report data");
                }

                // 4. 保存报表文件
                String filePath = saveReportFile(report, reportData, config);
                if (filePath == null || filePath.isEmpty()) {
                    throw new RuntimeException("Failed to save report file");
                }

                // 5. 更新报表状态
                report.setFilePath(filePath);
                report.setFileSize(reportData.length);
                report.setGenerateTime(LocalDateTime.now());
                report.setStatus(ReportStatus.COMPLETED);

                // 6. 存储报表信息
                storeReport(report);

                // 7. 自动分发（如果配置）
                if (config.isAutoDistribute() && !config.getRecipients().isEmpty()) {
                    DistributionConfig distConfig = new DistributionConfig();
                    distConfig.setEmailRecipients(config.getRecipients());
                    distConfig.setEnableEmail(true);
                    distributeReport(report, distConfig);
                }

                String message = "Report generated successfully";
                logger.info("Report generated successfully: {}", report.getReportId());
                return new GenerationResult(true, report, message, null);

            } catch (Exception e) {
                lastException = e;
                String message = "Report generation failed (attempt " + (attempt + 1) + "): " + e.getMessage();
                logger.warn(message, e);

                if (report != null) {
                    report.incrementRetryCount();
                    report.setErrorMessage(message);
                    if (attempt < config.getMaxRetryAttempts()) {
                        report.setStatus(ReportStatus.PENDING);
                        try {
                            Thread.sleep(config.getRetryDelay());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        report.setStatus(ReportStatus.FAILED);
                    }
                }
            }
        }

        // 所有重试都失败
        String message = "Report generation failed after " + (config.getMaxRetryAttempts() + 1) + " attempts";
        logger.error(message, lastException);
        return new GenerationResult(false, report, message, lastException);
    }

    /**
     * 创建报表对象
     *
     * @param config 报表配置
     * @return 报表对象
     */
    private static ProductionReport createReport(ReportConfig config) {
        try {
            ProductionReport report = new ProductionReport(config.getReportType(), config.getReportFormat());

            // 设置报表标题
            String title = config.getReportTitle();
            if (title == null || title.isEmpty()) {
                title = generateDefaultTitle(config);
            }
            report.setReportTitle(title);

            // 设置创建者
            report.setCreatedBy("System");

            // 设置接收者
            report.setRecipients(config.getRecipients());

            // 设置元数据
            report.setMetadata("timezone", config.getTimezone());
            report.setMetadata("templatePath", config.getTemplatePath());

            logger.debug("Created report object: {}", report.getReportId());
            return report;
        } catch (Exception e) {
            logger.error("Failed to create report object", e);
            return null;
        }
    }

    /**
     * 生成默认报表标题
     *
     * @param config 报表配置
     * @return 默认标题
     */
    private static String generateDefaultTitle(ReportConfig config) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return config.getReportType().getDescription() + " - " + sdf.format(new Date());
    }

    /**
     * 填充报表数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateReportData(ProductionReport report, ReportConfig config) {
        try {
            // 根据报表类型填充数据
            switch (report.getReportType()) {
                case DAILY_PRODUCTION:
                    populateDailyProductionData(report, config);
                    break;
                case WEEKLY_SUMMARY:
                    populateWeeklySummaryData(report, config);
                    break;
                case MONTHLY_ANALYSIS:
                    populateMonthlyAnalysisData(report, config);
                    break;
                case QUALITY_REPORT:
                    populateQualityReportData(report, config);
                    break;
                case EQUIPMENT_UTILIZATION:
                    populateEquipmentUtilizationData(report, config);
                    break;
                case YIELD_ANALYSIS:
                    populateYieldAnalysisData(report, config);
                    break;
                default:
                    populateCustomReportData(report, config);
                    break;
            }

            logger.debug("Populated report data for report: {}", report.getReportId());
        } catch (Exception e) {
            logger.error("Failed to populate report data", e);
            throw new RuntimeException("Failed to populate report data", e);
        }
    }

    /**
     * 填充日报表数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateDailyProductionData(ProductionReport report, ReportConfig config) {
        // 添加生产概览章节
        ReportSection overviewSection = new ReportSection("production_overview", "生产概览");

        // 模拟数据
        overviewSection.addData(new ReportData("total_products", 10000));
        overviewSection.addData(new ReportData("good_products", 9850));
        overviewSection.addData(new ReportData("defect_products", 150));
        overviewSection.addData(new ReportData("yield_rate", "98.50%"));
        overviewSection.addData(new ReportData("production_date", config.getReportDate().toLocalDate().toString()));

        report.addSection(overviewSection);

        // 添加设备状态章节
        ReportSection equipmentSection = new ReportSection("equipment_status", "设备状态");
        equipmentSection.addData(new ReportData("running_equipment", 15));
        equipmentSection.addData(new ReportData("maintenance_equipment", 2));
        equipmentSection.addData(new ReportData("down_equipment", 1));

        report.addSection(equipmentSection);
    }

    /**
     * 填充周报数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateWeeklySummaryData(ProductionReport report, ReportConfig config) {
        ReportSection weeklySection = new ReportSection("weekly_summary", "周生产总结");
        weeklySection.addData(new ReportData("week_number", "W" + config.getReportDate().getDayOfYear() / 7));
        weeklySection.addData(new ReportData("total_production", 70000));
        weeklySection.addData(new ReportData("weekly_yield", "98.20%"));
        weeklySection.addData(new ReportData("trend", "↑ 0.3%"));

        report.addSection(weeklySection);
    }

    /**
     * 填充月报数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateMonthlyAnalysisData(ProductionReport report, ReportConfig config) {
        ReportSection monthlySection = new ReportSection("monthly_analysis", "月度分析");
        monthlySection.addData(new ReportData("month", config.getReportDate().getMonth().toString()));
        monthlySection.addData(new ReportData("monthly_production", 300000));
        monthlySection.addData(new ReportData("monthly_yield", "98.10%"));
        monthlySection.addData(new ReportData("target_achievement", "95.50%"));

        report.addSection(monthlySection);
    }

    /**
     * 填充质量报告数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateQualityReportData(ProductionReport report, ReportConfig config) {
        ReportSection qualitySection = new ReportSection("quality_metrics", "质量指标");
        qualitySection.addData(new ReportData("cpk_value", "1.33"));
        qualitySection.addData(new ReportData("defect_rate", "1.50%"));
        qualitySection.addData(new ReportData("sigma_level", "4.5"));
        qualitySection.addData(new ReportData("improvement_suggestion", "优化温度控制参数"));

        report.addSection(qualitySection);
    }

    /**
     * 填充设备利用率数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateEquipmentUtilizationData(ProductionReport report, ReportConfig config) {
        ReportSection utilizationSection = new ReportSection("equipment_utilization", "设备利用率");
        utilizationSection.addData(new ReportData("average_utilization", "85.20%"));
        utilizationSection.addData(new ReportData("peak_utilization", "92.50%"));
        utilizationSection.addData(new ReportData("idle_time", "14.80%"));
        utilizationSection.addData(new ReportData("maintenance_time", "8.30%"));

        report.addSection(utilizationSection);
    }

    /**
     * 填充良率分析数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateYieldAnalysisData(ProductionReport report, ReportConfig config) {
        ReportSection yieldSection = new ReportSection("yield_analysis", "良率分析");
        yieldSection.addData(new ReportData("current_yield", "98.50%"));
        yieldSection.addData(new ReportData("target_yield", "99.00%"));
        yieldSection.addData(new ReportData("gap", "0.50%"));
        yieldSection.addData(new ReportData("improvement_plan", "优化清洗工艺"));

        report.addSection(yieldSection);
    }

    /**
     * 填充自定义报表数据
     *
     * @param report 报表对象
     * @param config 报表配置
     */
    private static void populateCustomReportData(ProductionReport report, ReportConfig config) {
        ReportSection customSection = new ReportSection("custom_data", "自定义数据");
        customSection.addData(new ReportData("parameter_count", config.getParameters().size()));
        customSection.addData(new ReportData("report_date", config.getReportDate().toString()));

        report.addSection(customSection);
    }

    /**
     * 生成报表数据
     *
     * @param report 报表对象
     * @param config 报表配置
     * @return 报表数据字节数组
     * @throws Exception 生成异常
     */
    private static byte[] generateReportData(ProductionReport report, ReportConfig config) throws Exception {
        ReportGenerator generator = generatorRegistry.get(config.getReportFormat());
        if (generator == null) {
            throw new UnsupportedOperationException("No generator available for format: " + config.getReportFormat());
        }

        return generator.generateReport(report, config);
    }

    /**
     * 保存报表文件
     *
     * @param report     报表对象
     * @param reportData 报表数据
     * @param config     报表配置
     * @return 文件路径
     */
    private static String saveReportFile(ProductionReport report, byte[] reportData, ReportConfig config) {
        try {
            // 创建报表目录
            Path reportDir = Paths.get(config.getReportDirectory());
            if (!Files.exists(reportDir)) {
                Files.createDirectories(reportDir);
            }

            // 生成文件名
            String fileName = generateFileName(report, config);
            Path filePath = reportDir.resolve(fileName);

            // 写入文件
            Files.write(filePath, reportData);

            logger.debug("Saved report file: {}", filePath.toString());
            return filePath.toString();
        } catch (Exception e) {
            logger.error("Failed to save report file", e);
            return null;
        }
    }

    /**
     * 生成报表文件名
     *
     * @param report 报表对象
     * @param config 报表配置
     * @return 文件名
     */
    private static String generateFileName(ProductionReport report, ReportConfig config) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = config.getReportDate().format(formatter);
        String reportType = report.getReportType().name().toLowerCase();
        String extension = config.getReportFormat().getExtension();

        return "report_" + reportType + "_" + timestamp + "." + extension;
    }

    /**
     * 存储报表信息
     *
     * @param report 报表对象
     */
    private static void storeReport(ProductionReport report) {
        try {
            reportStorage.put(report.getReportId(), report);
            reportCounter.incrementAndGet();
            logger.debug("Stored report information: {}", report.getReportId());
        } catch (Exception e) {
            logger.error("Failed to store report information", e);
        }
    }

    /**
     * 实时报表推送
     *
     * @param recipient 接收者
     * @param report    报表对象
     * @param config    分发配置
     * @return 分发结果列表
     */
    public static List<DistributionResult> pushRealTimeReport(String recipient, ProductionReport report, DistributionConfig config) {
        if (recipient == null || recipient.isEmpty() || report == null) {
            logger.warn("Invalid parameters for real-time report push");
            return Arrays.asList(new DistributionResult(false, recipient, "N/A", "Invalid parameters"));
        }

        try {
            // 创建临时分发配置
            DistributionConfig distConfig = config != null ? config : new DistributionConfig();
            distConfig.addEmailRecipient(recipient);
            distConfig.setEnableEmail(true);

            // 分发报表
            return distributeReport(report, distConfig);
        } catch (Exception e) {
            String message = "Failed to push real-time report: " + e.getMessage();
            logger.error(message, e);
            return Arrays.asList(new DistributionResult(false, recipient, "EMAIL", message));
        }
    }

    /**
     * 批量分发报表
     *
     * @param report 报表对象
     * @param config 分发配置
     * @return 分发结果列表
     */
    public static List<DistributionResult> distributeReport(ProductionReport report, DistributionConfig config) {
        if (report == null || config == null) {
            logger.warn("Invalid parameters for report distribution");
            return new ArrayList<>();
        }

        List<DistributionResult> results = new ArrayList<>();

        try {
            // 邮件分发
            if (config.isEnableEmail() && !config.getEmailRecipients().isEmpty()) {
                ReportDistributor emailDistributor = distributorRegistry.get("email");
                if (emailDistributor != null) {
                    for (String recipient : config.getEmailRecipients()) {
                        DistributionResult result = emailDistributor.distributeReport(report, recipient, config);
                        results.add(result);
                        logger.debug("Email distribution result for {}: {}", recipient, result.isSuccess());
                    }
                } else {
                    String message = "No email distributor available";
                    logger.warn(message);
                    for (String recipient : config.getEmailRecipients()) {
                        results.add(new DistributionResult(false, recipient, "EMAIL", message));
                    }
                }
            }

            // FTP分发
            if (config.isEnableFtp() && !config.getFtpServers().isEmpty()) {
                ReportDistributor ftpDistributor = distributorRegistry.get("ftp");
                if (ftpDistributor != null) {
                    for (String server : config.getFtpServers()) {
                        DistributionResult result = ftpDistributor.distributeReport(report, server, config);
                        results.add(result);
                        logger.debug("FTP distribution result for {}: {}", server, result.isSuccess());
                    }
                } else {
                    String message = "No FTP distributor available";
                    logger.warn(message);
                    for (String server : config.getFtpServers()) {
                        results.add(new DistributionResult(false, server, "FTP", message));
                    }
                }
            }

            // HTTP分发
            if (config.isEnableHttp() && config.getHttpEndpoint() != null && !config.getHttpEndpoint().isEmpty()) {
                ReportDistributor httpDistributor = distributorRegistry.get("http");
                if (httpDistributor != null) {
                    DistributionResult result = httpDistributor.distributeReport(report, config.getHttpEndpoint(), config);
                    results.add(result);
                    logger.debug("HTTP distribution result: {}", result.isSuccess());
                } else {
                    String message = "No HTTP distributor available";
                    logger.warn(message);
                    results.add(new DistributionResult(false, config.getHttpEndpoint(), "HTTP", message));
                }
            }

            logger.info("Report distribution completed, total results: {}", results.size());
            return results;
        } catch (Exception e) {
            String message = "Failed to distribute report: " + e.getMessage();
            logger.error(message, e);
            return Arrays.asList(new DistributionResult(false, "N/A", "UNKNOWN", message));
        }
    }

    /**
     * 报表数据验证
     *
     * @param report 报表对象
     * @return 验证结果
     */
    public static ValidationResult validateReportData(ProductionReport report) {
        if (report == null) {
            String message = "Invalid report for validation";
            logger.warn(message);
            return new ValidationResult(false, message, Arrays.asList("Report is null"));
        }

        try {
            ReportValidator validator = validatorRegistry.get(report.getReportType());
            if (validator == null) {
                // 使用默认验证器
                ValidationResult result = performBasicValidation(report);
                logger.debug("Performed basic validation for report: {}", report.getReportId());
                return result;
            }

            ValidationResult result = validator.validateReport(report);
            logger.debug("Validation completed for report: {}, valid: {}", report.getReportId(), result.isValid());
            return result;
        } catch (Exception e) {
            String message = "Validation error: " + e.getMessage();
            logger.error(message, e);
            return new ValidationResult(false, message, Arrays.asList(e.getMessage()));
        }
    }

    /**
     * 执行基本验证
     *
     * @param report 报表对象
     * @return 验证结果
     */
    private static ValidationResult performBasicValidation(ProductionReport report) {
        List<String> errors = new ArrayList<>();

        // 检查必需字段
        if (report.getReportType() == null) {
            errors.add("Report type is required");
        }

        if (report.getReportFormat() == null) {
            errors.add("Report format is required");
        }

        if (report.getReportTitle() == null || report.getReportTitle().isEmpty()) {
            errors.add("Report title is required");
        }

        // 检查数据完整性
        if (report.getSections().isEmpty()) {
            errors.add("Report must contain at least one section");
        } else {
            for (ReportSection section : report.getSections()) {
                if (section.getSectionName() == null || section.getSectionName().isEmpty()) {
                    errors.add("Section name is required");
                }
                if (section.getSectionTitle() == null || section.getSectionTitle().isEmpty()) {
                    errors.add("Section title is required");
                }
            }
        }

        boolean valid = errors.isEmpty();
        String message = valid ? "Basic validation passed" : "Basic validation failed";

        return new ValidationResult(valid, message, errors);
    }

    /**
     * 报表历史查询
     *
     * @param query 查询条件
     * @return 报表列表
     */
    public static List<ProductionReport> queryHistoricalReports(ReportQuery query) {
        if (query == null) {
            logger.warn("Invalid query for historical reports");
            return new ArrayList<>();
        }

        try {
            List<ProductionReport> allReports = new ArrayList<>(reportStorage.values());

            // 应用过滤条件
            List<ProductionReport> filteredReports = allReports.stream()
                    .filter(report -> filterByType(report, query.getReportType()))
                    .filter(report -> filterByDateRange(report, query.getStartDate(), query.getEndDate()))
                    .filter(report -> filterByKeyword(report, query.getKeyword()))
                    .sorted((r1, r2) -> {
                        // 排序
                        int comparison = compareReports(r1, r2, query.getSortBy());
                        return query.isAscending() ? comparison : -comparison;
                    })
                    .collect(Collectors.toList());

            // 分页处理
            int total = filteredReports.size();
            int start = (query.getPage() - 1) * query.getPageSize();
            int end = Math.min(start + query.getPageSize(), total);

            if (start >= total) {
                return new ArrayList<>(); // 超出范围，返回空列表
            }

            List<ProductionReport> pagedReports = filteredReports.subList(start, end);
            logger.debug("Queried historical reports: {} found, returning {}", total, pagedReports.size());

            return pagedReports;
        } catch (Exception e) {
            logger.error("Failed to query historical reports", e);
            return new ArrayList<>();
        }
    }

    /**
     * 按报表类型过滤
     */
    private static boolean filterByType(ProductionReport report, ReportType type) {
        return type == null || report.getReportType() == type;
    }

    /**
     * 按日期范围过滤
     */
    private static boolean filterByDateRange(ProductionReport report, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime reportDate = report.getCreateTime();
        boolean afterStart = startDate == null || !reportDate.isBefore(startDate);
        boolean beforeEnd = endDate == null || !reportDate.isAfter(endDate);
        return afterStart && beforeEnd;
    }

    /**
     * 按关键字过滤
     */
    private static boolean filterByKeyword(ProductionReport report, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }

        // 在报表标题和元数据中搜索关键字
        if (report.getReportTitle() != null && report.getReportTitle().toLowerCase().contains(keyword.toLowerCase())) {
            return true;
        }

        // 在元数据中搜索
        for (Object value : report.getMetadata().values()) {
            if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 比较两个报表
     */
    private static int compareReports(ProductionReport r1, ProductionReport r2, String sortBy) {
        switch (sortBy != null ? sortBy.toLowerCase() : "createtime") {
            case "title":
                return compareStrings(r1.getReportTitle(), r2.getReportTitle());
            case "type":
                return compareEnums(r1.getReportType(), r2.getReportType());
            case "format":
                return compareEnums(r1.getReportFormat(), r2.getReportFormat());
            case "status":
                return compareEnums(r1.getStatus(), r2.getStatus());
            case "filesize":
                return Long.compare(r1.getFileSize(), r2.getFileSize());
            default: // createtime
                return r1.getCreateTime().compareTo(r2.getCreateTime());
        }
    }

    /**
     * 比较字符串
     */
    private static int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }

    /**
     * 比较枚举
     */
    private static <T extends Enum<T>> int compareEnums(T e1, T e2) {
        if (e1 == null && e2 == null) return 0;
        if (e1 == null) return -1;
        if (e2 == null) return 1;
        return e1.name().compareTo(e2.name());
    }

    /**
     * 获取报表统计信息
     *
     * @return 统计信息
     */
    public static ReportStatistics getReportStatistics() {
        try {
            long totalReports = reportStorage.size();
            long completedReports = reportStorage.values().stream()
                    .filter(report -> report.getStatus() == ReportStatus.COMPLETED)
                    .count();
            long failedReports = reportStorage.values().stream()
                    .filter(report -> report.getStatus() == ReportStatus.FAILED)
                    .count();

            Map<ReportType, Long> typeDistribution = reportStorage.values().stream()
                    .collect(Collectors.groupingBy(ProductionReport::getReportType, Collectors.counting()));

            Map<ReportFormat, Long> formatDistribution = reportStorage.values().stream()
                    .collect(Collectors.groupingBy(ProductionReport::getReportFormat, Collectors.counting()));

            ReportStatistics statistics = new ReportStatistics();
            statistics.setTotalReports(totalReports);
            statistics.setCompletedReports(completedReports);
            statistics.setFailedReports(failedReports);
            statistics.setTypeDistribution(typeDistribution);
            statistics.setFormatDistribution(formatDistribution);
            statistics.setTotalGenerated(reportCounter.get());

            logger.debug("Generated report statistics: total={}, completed={}, failed={}",
                    totalReports, completedReports, failedReports);

            return statistics;
        } catch (Exception e) {
            logger.error("Failed to generate report statistics", e);
            return new ReportStatistics();
        }
    }

    /**
     * 异步生成报表
     *
     * @param config   报表配置
     * @param callback 回调函数
     */
    public static void generateReportAsync(ReportConfig config, ReportGenerationCallback callback) {
        if (config == null || callback == null) {
            logger.warn("Invalid parameters for async report generation");
            if (callback != null) {
                callback.onComplete(new GenerationResult(false, null, "Invalid parameters", null));
            }
            return;
        }

        executorService.submit(() -> {
            try {
                GenerationResult result = generateStandardReport(config);
                callback.onComplete(result);
            } catch (Exception e) {
                String message = "Async report generation failed: " + e.getMessage();
                logger.error(message, e);
                callback.onComplete(new GenerationResult(false, null, message, e));
            }
        });
    }

    /**
     * 批量生成报表
     *
     * @param configs 报表配置列表
     * @return 生成结果列表
     */
    public static List<GenerationResult> generateBatchReports(List<ReportConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            logger.warn("Invalid configs for batch report generation");
            return new ArrayList<>();
        }

        List<GenerationResult> results = new ArrayList<>();

        for (int i = 0; i < configs.size(); i++) {
            ReportConfig config = configs.get(i);
            try {
                logger.info("Generating batch report {}/{}", i + 1, configs.size());
                GenerationResult result = generateStandardReport(config);
                results.add(result);
            } catch (Exception e) {
                String message = "Batch report generation failed: " + e.getMessage();
                logger.error(message, e);
                results.add(new GenerationResult(false, null, message, e));
            }
        }

        logger.info("Batch report generation completed: {} success, {} failed",
                results.stream().filter(GenerationResult::isSuccess).count(),
                results.stream().filter(result -> !result.isSuccess()).count());

        return results;
    }

    /**
     * 获取报表详情
     *
     * @param reportId 报表ID
     * @return 报表对象
     */
    public static ProductionReport getReportDetails(String reportId) {
        if (reportId == null || reportId.isEmpty()) {
            logger.warn("Invalid report ID for details query");
            return null;
        }

        ProductionReport report = reportStorage.get(reportId);
        if (report == null) {
            logger.warn("Report not found: {}", reportId);
            return null;
        }

        logger.debug("Retrieved report details: {}", reportId);
        return report;
    }

    /**
     * 删除报表
     *
     * @param reportId 报表ID
     * @return 是否删除成功
     */
    public static boolean deleteReport(String reportId) {
        if (reportId == null || reportId.isEmpty()) {
            logger.warn("Invalid report ID for deletion");
            return false;
        }

        ProductionReport report = reportStorage.remove(reportId);
        if (report == null) {
            logger.warn("Report not found for deletion: {}", reportId);
            return false;
        }

        // 删除物理文件
        try {
            if (report.getFilePath() != null && !report.getFilePath().isEmpty()) {
                Path filePath = Paths.get(report.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    logger.debug("Deleted report file: {}", report.getFilePath());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to delete report file: {}", report.getFilePath(), e);
        }

        logger.info("Deleted report: {}", reportId);
        return true;
    }

    /**
     * 导出报表数据
     *
     * @param reportId 报表ID
     * @param format   导出格式
     * @return 导出的数据
     */
    public static byte[] exportReportData(String reportId, ReportFormat format) {
        if (reportId == null || reportId.isEmpty() || format == null) {
            logger.warn("Invalid parameters for report data export");
            return new byte[0];
        }

        ProductionReport report = getReportDetails(reportId);
        if (report == null) {
            logger.warn("Report not found for export: {}", reportId);
            return new byte[0];
        }

        try {
            ReportConfig config = new ReportConfig();
            config.setReportFormat(format);

            ReportGenerator generator = generatorRegistry.get(format);
            if (generator == null) {
                logger.warn("No generator available for format: {}", format);
                return new byte[0];
            }

            byte[] data = generator.generateReport(report, config);
            logger.debug("Exported report data: {} bytes inspection {} format", data.length, format);
            return data;
        } catch (Exception e) {
            logger.error("Failed to export report data", e);
            return new byte[0];
        }
    }

    /**
     * 关闭报表生成器
     */
    public static void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            logger.info("ProductionReporter shutdown completed");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("ProductionReporter shutdown interrupted");
        }
    }

    // 报表格式枚举
    public enum ReportFormat {
        PDF("pdf", "Portable Document Format"),
        EXCEL("xlsx", "Microsoft Excel"),
        CSV("csv", "Comma-Separated Values"),
        HTML("html", "HyperText Markup Language"),
        JSON("json", "JavaScript Object Notation"),
        XML("xml", "eXtensible Markup Language");

        private final String extension;
        private final String description;

        ReportFormat(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }
    }

    // 报表类型枚举
    public enum ReportType {
        DAILY_PRODUCTION("Daily Production Report"),
        WEEKLY_SUMMARY("Weekly Production Summary"),
        MONTHLY_ANALYSIS("Monthly Production Analysis"),
        QUALITY_REPORT("Quality Control Report"),
        EQUIPMENT_UTILIZATION("Equipment Utilization Report"),
        YIELD_ANALYSIS("Yield Analysis Report"),
        CUSTOM_REPORT("Custom Report");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 报表状态枚举
    public enum ReportStatus {
        PENDING("Pending"),
        GENERATING("Generating"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        VALIDATED("Validated"),
        DISTRIBUTED("Distributed");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 报表生成器接口
    public interface ReportGenerator {
        byte[] generateReport(ProductionReport report, ReportConfig config) throws Exception;

        boolean supportsFormat(ReportFormat format);

        String getGeneratorName();
    }

    // 报表分发器接口
    public interface ReportDistributor {
        DistributionResult distributeReport(ProductionReport report, String recipient, DistributionConfig config);

        boolean supportsMethod(String method);

        String getDistributorName();
    }

    // 报表验证器接口
    public interface ReportValidator {
        ValidationResult validateReport(ProductionReport report);

        boolean supportsType(ReportType type);

        String getValidatorName();
    }

    /**
     * 报表生成回调接口
     */
    public interface ReportGenerationCallback {
        void onComplete(GenerationResult result);
    }

    // 报表配置类
    public static class ReportConfig {
        private ReportType reportType = ReportType.DAILY_PRODUCTION;
        private ReportFormat reportFormat = ReportFormat.PDF;
        private LocalDateTime reportDate = LocalDateTime.now();
        private String reportTitle;
        private String reportDirectory = DEFAULT_REPORT_DIRECTORY;
        private Map<String, Object> parameters = new HashMap<>();
        private List<String> recipients = new ArrayList<>();
        private boolean autoDistribute = false;
        private boolean enableValidation = true;
        private int maxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;
        private long retryDelay = DEFAULT_RETRY_DELAY;
        private String templatePath;
        private String timezone = "Asia/Shanghai";

        // Getters and Setters
        public ReportType getReportType() {
            return reportType;
        }

        public ReportConfig setReportType(ReportType reportType) {
            this.reportType = reportType;
            return this;
        }

        public ReportFormat getReportFormat() {
            return reportFormat;
        }

        public ReportConfig setReportFormat(ReportFormat reportFormat) {
            this.reportFormat = reportFormat;
            return this;
        }

        public LocalDateTime getReportDate() {
            return reportDate;
        }

        public ReportConfig setReportDate(LocalDateTime reportDate) {
            this.reportDate = reportDate;
            return this;
        }

        public String getReportTitle() {
            return reportTitle;
        }

        public ReportConfig setReportTitle(String reportTitle) {
            this.reportTitle = reportTitle;
            return this;
        }

        public String getReportDirectory() {
            return reportDirectory;
        }

        public ReportConfig setReportDirectory(String reportDirectory) {
            this.reportDirectory = reportDirectory;
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public ReportConfig setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return parameters.get(key);
        }

        public List<String> getRecipients() {
            return new ArrayList<>(recipients);
        }

        public ReportConfig setRecipients(List<String> recipients) {
            this.recipients = new ArrayList<>(recipients);
            return this;
        }

        public ReportConfig addRecipient(String recipient) {
            this.recipients.add(recipient);
            return this;
        }

        public boolean isAutoDistribute() {
            return autoDistribute;
        }

        public ReportConfig setAutoDistribute(boolean autoDistribute) {
            this.autoDistribute = autoDistribute;
            return this;
        }

        public boolean isEnableValidation() {
            return enableValidation;
        }

        public ReportConfig setEnableValidation(boolean enableValidation) {
            this.enableValidation = enableValidation;
            return this;
        }

        public int getMaxRetryAttempts() {
            return maxRetryAttempts;
        }

        public ReportConfig setMaxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public ReportConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public ReportConfig setTemplatePath(String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        public String getTimezone() {
            return timezone;
        }

        public ReportConfig setTimezone(String timezone) {
            this.timezone = timezone;
            return this;
        }
    }

    // 报表查询类
    public static class ReportQuery {
        private ReportType reportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String keyword;
        private int page = 1;
        private int pageSize = 50;
        private String sortBy = "createTime";
        private boolean ascending = false;

        // Getters and Setters
        public ReportType getReportType() {
            return reportType;
        }

        public ReportQuery setReportType(ReportType reportType) {
            this.reportType = reportType;
            return this;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public ReportQuery setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public ReportQuery setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public String getKeyword() {
            return keyword;
        }

        public ReportQuery setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public int getPage() {
            return page;
        }

        public ReportQuery setPage(int page) {
            this.page = page;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public ReportQuery setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public String getSortBy() {
            return sortBy;
        }

        public ReportQuery setSortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public boolean isAscending() {
            return ascending;
        }

        public ReportQuery setAscending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }
    }

    // 报表分发配置类
    public static class DistributionConfig {
        private List<String> emailRecipients = new ArrayList<>();
        private List<String> ftpServers = new ArrayList<>();
        private String httpEndpoint;
        private boolean enableEmail = true;
        private boolean enableFtp = false;
        private boolean enableHttp = false;
        private Map<String, Object> emailSettings = new HashMap<>();
        private Map<String, Object> ftpSettings = new HashMap<>();
        private Map<String, Object> httpSettings = new HashMap<>();

        // Getters and Setters
        public List<String> getEmailRecipients() {
            return new ArrayList<>(emailRecipients);
        }

        public DistributionConfig setEmailRecipients(List<String> recipients) {
            this.emailRecipients = new ArrayList<>(recipients);
            return this;
        }

        public DistributionConfig addEmailRecipient(String recipient) {
            this.emailRecipients.add(recipient);
            return this;
        }

        public List<String> getFtpServers() {
            return new ArrayList<>(ftpServers);
        }

        public DistributionConfig setFtpServers(List<String> servers) {
            this.ftpServers = new ArrayList<>(servers);
            return this;
        }

        public DistributionConfig addFtpServer(String server) {
            this.ftpServers.add(server);
            return this;
        }

        public String getHttpEndpoint() {
            return httpEndpoint;
        }

        public DistributionConfig setHttpEndpoint(String endpoint) {
            this.httpEndpoint = endpoint;
            return this;
        }

        public boolean isEnableEmail() {
            return enableEmail;
        }

        public DistributionConfig setEnableEmail(boolean enableEmail) {
            this.enableEmail = enableEmail;
            return this;
        }

        public boolean isEnableFtp() {
            return enableFtp;
        }

        public DistributionConfig setEnableFtp(boolean enableFtp) {
            this.enableFtp = enableFtp;
            return this;
        }

        public boolean isEnableHttp() {
            return enableHttp;
        }

        public DistributionConfig setEnableHttp(boolean enableHttp) {
            this.enableHttp = enableHttp;
            return this;
        }

        public Map<String, Object> getEmailSettings() {
            return new HashMap<>(emailSettings);
        }

        public DistributionConfig setEmailSetting(String key, Object value) {
            this.emailSettings.put(key, value);
            return this;
        }

        public Map<String, Object> getFtpSettings() {
            return new HashMap<>(ftpSettings);
        }

        public DistributionConfig setFtpSetting(String key, Object value) {
            this.ftpSettings.put(key, value);
            return this;
        }

        public Map<String, Object> getHttpSettings() {
            return new HashMap<>(httpSettings);
        }

        public DistributionConfig setHttpSetting(String key, Object value) {
            this.httpSettings.put(key, value);
            return this;
        }
    }

    // 报表类
    public static class ProductionReport {
        private final String reportId;
        private final ReportType reportType;
        private final ReportFormat reportFormat;
        private String reportTitle;
        private LocalDateTime createTime;
        private LocalDateTime generateTime;
        private ReportStatus status;
        private String filePath;
        private long fileSize;
        private Map<String, Object> metadata;
        private List<ReportSection> sections;
        private String createdBy;
        private List<String> recipients;
        private String errorMessage;
        private int retryCount;

        public ProductionReport(ReportType reportType, ReportFormat reportFormat) {
            this.reportId = UUID.randomUUID().toString();
            this.reportType = reportType;
            this.reportFormat = reportFormat;
            this.createTime = LocalDateTime.now();
            this.status = ReportStatus.PENDING;
            this.metadata = new HashMap<>();
            this.sections = new ArrayList<>();
            this.recipients = new ArrayList<>();
        }

        // Getters and Setters
        public String getReportId() {
            return reportId;
        }

        public ReportType getReportType() {
            return reportType;
        }

        public ReportFormat getReportFormat() {
            return reportFormat;
        }

        public String getReportTitle() {
            return reportTitle;
        }

        public void setReportTitle(String reportTitle) {
            this.reportTitle = reportTitle;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public ReportStatus getStatus() {
            return status;
        }

        public void setStatus(ReportStatus status) {
            this.status = status;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public void setMetadata(String key, Object value) {
            this.metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public List<ReportSection> getSections() {
            return new ArrayList<>(sections);
        }

        public void setSections(List<ReportSection> sections) {
            this.sections = new ArrayList<>(sections);
        }

        public void addSection(ReportSection section) {
            this.sections.add(section);
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public List<String> getRecipients() {
            return new ArrayList<>(recipients);
        }

        public void setRecipients(List<String> recipients) {
            this.recipients = new ArrayList<>(recipients);
        }

        public void addRecipient(String recipient) {
            this.recipients.add(recipient);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public void incrementRetryCount() {
            this.retryCount++;
        }

        @Override
        public String toString() {
            return "ProductionReport{" +
                    "reportId='" + reportId + '\'' +
                    ", reportType=" + reportType +
                    ", reportFormat=" + reportFormat +
                    ", reportTitle='" + reportTitle + '\'' +
                    ", status=" + status +
                    ", createTime=" + createTime +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }
    }

    // 报表章节类
    public static class ReportSection {
        private String sectionName;
        private String sectionTitle;
        private List<ReportData> data;
        private Map<String, Object> properties;

        public ReportSection(String sectionName, String sectionTitle) {
            this.sectionName = sectionName;
            this.sectionTitle = sectionTitle;
            this.data = new ArrayList<>();
            this.properties = new HashMap<>();
        }

        // Getters and Setters
        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }

        public void setSectionTitle(String sectionTitle) {
            this.sectionTitle = sectionTitle;
        }

        public List<ReportData> getData() {
            return new ArrayList<>(data);
        }

        public void setData(List<ReportData> data) {
            this.data = new ArrayList<>(data);
        }

        public void addData(ReportData dataItem) {
            this.data.add(dataItem);
        }

        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }

        public void setProperty(String key, Object value) {
            this.properties.put(key, value);
        }

        public Object getProperty(String key) {
            return this.properties.get(key);
        }
    }

    // 报表数据类
    public static class ReportData {
        private String dataKey;
        private Object dataValue;
        private String dataType;
        private Map<String, Object> attributes;

        public ReportData(String dataKey, Object dataValue) {
            this.dataKey = dataKey;
            this.dataValue = dataValue;
            this.dataType = dataValue != null ? dataValue.getClass().getSimpleName() : "null";
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public String getDataKey() {
            return dataKey;
        }

        public void setDataKey(String dataKey) {
            this.dataKey = dataKey;
        }

        public Object getDataValue() {
            return dataValue;
        }

        public void setDataValue(Object dataValue) {
            this.dataValue = dataValue;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            this.attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }
    }

    // 报表验证结果类
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> errors;
        private final long validationTime;

        public ValidationResult(boolean valid, String message, List<String> errors) {
            this.valid = valid;
            this.message = message != null ? message : "";
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
            this.validationTime = System.currentTimeMillis();
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public long getValidationTime() {
            return validationTime;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    ", errorCount=" + errors.size() +
                    ", validationTime=" + validationTime +
                    '}';
        }
    }

    // 报表生成结果类
    public static class GenerationResult {
        private final boolean success;
        private final ProductionReport report;
        private final String message;
        private final long generationTime;
        private final Exception exception;

        public GenerationResult(boolean success, ProductionReport report, String message, Exception exception) {
            this.success = success;
            this.report = report;
            this.message = message != null ? message : "";
            this.generationTime = System.currentTimeMillis();
            this.exception = exception;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public ProductionReport getReport() {
            return report;
        }

        public String getMessage() {
            return message;
        }

        public long getGenerationTime() {
            return generationTime;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "GenerationResult{" +
                    "success=" + success +
                    ", reportId=" + (report != null ? report.getReportId() : "null") +
                    ", message='" + message + '\'' +
                    ", generationTime=" + generationTime +
                    '}';
        }
    }

    // 报表分发结果类
    public static class DistributionResult {
        private final boolean success;
        private final String recipient;
        private final String method;
        private final String message;
        private final long distributionTime;

        public DistributionResult(boolean success, String recipient, String method, String message) {
            this.success = success;
            this.recipient = recipient != null ? recipient : "";
            this.method = method != null ? method : "";
            this.message = message != null ? message : "";
            this.distributionTime = System.currentTimeMillis();
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getMethod() {
            return method;
        }

        public String getMessage() {
            return message;
        }

        public long getDistributionTime() {
            return distributionTime;
        }

        @Override
        public String toString() {
            return "DistributionResult{" +
                    "success=" + success +
                    ", recipient='" + recipient + '\'' +
                    ", method='" + method + '\'' +
                    ", message='" + message + '\'' +
                    ", distributionTime=" + distributionTime +
                    '}';
        }
    }

    /**
     * 报表统计信息类
     */
    public static class ReportStatistics {
        private long totalReports;
        private long completedReports;
        private long failedReports;
        private long totalGenerated;
        private Map<ReportType, Long> typeDistribution;
        private Map<ReportFormat, Long> formatDistribution;

        public ReportStatistics() {
            this.typeDistribution = new HashMap<>();
            this.formatDistribution = new HashMap<>();
        }

        // Getters and Setters
        public long getTotalReports() {
            return totalReports;
        }

        public void setTotalReports(long totalReports) {
            this.totalReports = totalReports;
        }

        public long getCompletedReports() {
            return completedReports;
        }

        public void setCompletedReports(long completedReports) {
            this.completedReports = completedReports;
        }

        public long getFailedReports() {
            return failedReports;
        }

        public void setFailedReports(long failedReports) {
            this.failedReports = failedReports;
        }

        public long getTotalGenerated() {
            return totalGenerated;
        }

        public void setTotalGenerated(long totalGenerated) {
            this.totalGenerated = totalGenerated;
        }

        public Map<ReportType, Long> getTypeDistribution() {
            return new HashMap<>(typeDistribution);
        }

        public void setTypeDistribution(Map<ReportType, Long> typeDistribution) {
            this.typeDistribution = new HashMap<>(typeDistribution);
        }

        public Map<ReportFormat, Long> getFormatDistribution() {
            return new HashMap<>(formatDistribution);
        }

        public void setFormatDistribution(Map<ReportFormat, Long> formatDistribution) {
            this.formatDistribution = new HashMap<>(formatDistribution);
        }
    }
}
