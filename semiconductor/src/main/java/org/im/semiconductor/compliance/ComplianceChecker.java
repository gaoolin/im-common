package org.im.semiconductor.compliance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 合规性检查工具类
 * <p>
 * 特性：
 * - 通用性：支持多种行业标准和合规性要求
 * - 规范性：统一的合规性检查接口和数据结构
 * - 专业性：半导体行业合规性专业实现
 * - 灵活性：可配置的检查规则和评估标准
 * - 可靠性：完善的检查流程和错误处理机制
 * - 安全性：数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class ComplianceChecker {

    // 默认配置
    public static final long DEFAULT_CHECK_TIMEOUT = 30000; // 30秒
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final long DEFAULT_CACHE_TIMEOUT = 24 * 60 * 60 * 1000; // 24小时
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_MAX_AUDIT_ITEMS = 10000;
    private static final Logger logger = LoggerFactory.getLogger(ComplianceChecker.class);
    // 内部存储和管理
    private static final Map<StandardType, StandardChecker> checkerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ComplianceRule> ruleRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ComplianceTemplate> templateRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ComplianceResult> resultCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private static final ExecutorService auditExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    // 统计信息
    private static final AtomicLong totalChecks = new AtomicLong(0);
    private static final AtomicLong successfulChecks = new AtomicLong(0);
    private static final AtomicLong failedChecks = new AtomicLong(0);
    private static final AtomicInteger activeAudits = new AtomicInteger(0);

    // 初始化默认组件
    static {
        registerDefaultComponents();
        startMaintenanceTasks();
        logger.info("ComplianceChecker initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认检查器、规则和模板
        // registerStandardChecker(StandardType.ISO9001, new ISO9001Checker());
        // registerStandardChecker(StandardType.ISO14001, new ISO14001Checker());
        // registerStandardChecker(StandardType.IATF16949, new IATF16949Checker());
        // registerComplianceRule("document_control", new DocumentControlRule());
        // registerComplianceTemplate("quality_audit", new QualityAuditTemplate());
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(ComplianceChecker::cleanupCache,
                60, 60, TimeUnit.MINUTES);

        // 启动统计报告任务
        scheduler.scheduleAtFixedRate(ComplianceChecker::generatePeriodicReport,
                24, 24, TimeUnit.HOURS);

        logger.debug("Compliance checker maintenance tasks started");
    }

    /**
     * 注册标准检查器
     */
    public static void registerStandardChecker(StandardType standard, StandardChecker checker) {
        if (standard == null || checker == null) {
            throw new IllegalArgumentException("Standard and checker cannot be null");
        }

        checkerRegistry.put(standard, checker);
        logger.debug("Registered standard checker for: {}", standard);
    }

    /**
     * 注册合规性规则
     */
    public static void registerComplianceRule(String ruleName, ComplianceRule rule) {
        if (ruleName == null || ruleName.isEmpty() || rule == null) {
            throw new IllegalArgumentException("Rule name and rule cannot be null or empty");
        }

        ruleRegistry.put(ruleName, rule);
        logger.debug("Registered compliance rule: {}", ruleName);
    }

    /**
     * 注册合规性模板
     */
    public static void registerComplianceTemplate(String templateName, ComplianceTemplate template) {
        if (templateName == null || templateName.isEmpty() || template == null) {
            throw new IllegalArgumentException("Template name and template cannot be null or empty");
        }

        templateRegistry.put(templateName, template);
        logger.debug("Registered compliance template: {}", templateName);
    }

    /**
     * 标准符合性检查
     */
    public static ComplianceResult checkStandardCompliance(Standard standard, AuditData data) {
        return checkStandardCompliance(standard, data, new ComplianceConfig());
    }

    /**
     * 标准符合性检查（带配置）
     */
    public static ComplianceResult checkStandardCompliance(Standard standard, AuditData data, ComplianceConfig config) {
        if (standard == null) {
            logger.error("Invalid standard for compliance check");
            return new ComplianceResult(ComplianceStatus.FAILURE, "Invalid standard", null);
        }

        if (data == null) {
            logger.error("Invalid audit data for compliance check");
            return new ComplianceResult(ComplianceStatus.FAILURE, "Invalid audit data", null);
        }

        if (config == null) {
            config = new ComplianceConfig();
        }

        totalChecks.incrementAndGet();

        try {
            long startTime = System.currentTimeMillis();

            // 检查缓存
            String cacheKey = generateComplianceCacheKey(standard, data);
            if (config.isEnableCache()) {
                ComplianceResult cachedResult = resultCache.get(cacheKey);
                if (cachedResult != null && !isCacheExpired(cachedResult)) {
                    logger.debug("Returning cached compliance result for standard: {}", standard.getStandardType());
                    successfulChecks.incrementAndGet();
                    return cachedResult;
                }
            }

            // 获取对应的标准检查器
            StandardChecker checker = checkerRegistry.get(standard.getStandardType());
            if (checker == null) {
                logger.error("No checker found for standard: {}", standard.getStandardType());
                failedChecks.incrementAndGet();
                return new ComplianceResult(ComplianceStatus.FAILURE, "No checker available", null);
            }

            // 执行合规性检查
            ComplianceResult result = null;
            int retryCount = 0;

            while (retryCount <= config.getMaxRetries()) {
                try {
                    result = checker.checkCompliance(standard, data, config);

                    if (result != null && result.getStatus() != ComplianceStatus.FAILURE) {
                        break; // 成功，退出重试循环
                    }
                } catch (Exception e) {
                    logger.warn("Compliance check failed for standard: {} (attempt {}/{})",
                            standard.getStandardType(), retryCount + 1, config.getMaxRetries() + 1, e);
                }

                retryCount++;

                // 如果需要重试，等待后重试
                if (retryCount <= config.getMaxRetries()) {
                    try {
                        Thread.sleep(config.getRetryDelay() * retryCount); // 递增延迟
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // 处理结果
            if (result != null && result.getStatus() != ComplianceStatus.FAILURE) {
                successfulChecks.incrementAndGet();

                // 缓存结果
                if (config.isEnableCache()) {
                    resultCache.put(cacheKey, result);
                }

                logger.debug("Compliance check completed successfully for standard: {} in {}ms",
                        standard.getStandardType(), duration);
            } else {
                failedChecks.incrementAndGet();
                logger.warn("Compliance check failed for standard: {} after {} attempts in {}ms",
                        standard.getStandardType(), retryCount, duration);
            }

            return result != null ? result : new ComplianceResult(ComplianceStatus.FAILURE, "Check execution failed", null);
        } catch (Exception e) {
            failedChecks.incrementAndGet();
            logger.error("Exception occurred while checking compliance for standard: {}", standard.getStandardType(), e);
            return new ComplianceResult(ComplianceStatus.FAILURE, "Exception: " + e.getMessage(), null);
        }
    }

    /**
     * 异步标准符合性检查
     */
    public static CompletableFuture<ComplianceResult> checkStandardComplianceAsync(Standard standard, AuditData data) {
        return checkStandardComplianceAsync(standard, data, new ComplianceConfig());
    }

    /**
     * 异步标准符合性检查（带配置）
     */
    public static CompletableFuture<ComplianceResult> checkStandardComplianceAsync(Standard standard, AuditData data, ComplianceConfig config) {
        return CompletableFuture.supplyAsync(() -> checkStandardCompliance(standard, data, config), auditExecutor);
    }

    /**
     * 批量标准符合性检查
     */
    public static List<ComplianceResult> checkBatchCompliance(List<Standard> standards, AuditData data) {
        return checkBatchCompliance(standards, data, new ComplianceConfig());
    }

    /**
     * 批量标准符合性检查（带配置）
     */
    public static List<ComplianceResult> checkBatchCompliance(List<Standard> standards, AuditData data, ComplianceConfig config) {
        if (standards == null || standards.isEmpty()) {
            logger.warn("No standards provided for batch compliance check");
            return new ArrayList<>();
        }

        List<ComplianceResult> results = new ArrayList<>();

        for (Standard standard : standards) {
            ComplianceResult result = checkStandardCompliance(standard, data, config);
            results.add(result);
        }

        return results;
    }

    /**
     * 质量体系审核
     */
    public static AuditReport conductQualityAudit(QualityAudit audit) {
        return conductQualityAudit(audit, new AuditConfig());
    }

    /**
     * 质量体系审核（带配置）
     */
    public static AuditReport conductQualityAudit(QualityAudit audit, AuditConfig config) {
        if (audit == null) {
            logger.error("Invalid quality audit");
            return new AuditReport(AuditStatus.FAILURE, "Invalid audit", null);
        }

        if (config == null) {
            config = new AuditConfig();
        }

        activeAudits.incrementAndGet();

        try {
            long startTime = System.currentTimeMillis();

            // 验证审核数据
            if (audit.getAuditItems() == null || audit.getAuditItems().isEmpty()) {
                logger.warn("No audit items found for quality audit: {}", audit.getAuditId());
                AuditReport emptyReport = new AuditReport(AuditStatus.COMPLETED, "No audit items", new ArrayList<>());
                return emptyReport;
            }

            if (audit.getAuditItems().size() > DEFAULT_MAX_AUDIT_ITEMS) {
                logger.warn("Too many audit items ({}), limiting to {}",
                        audit.getAuditItems().size(), DEFAULT_MAX_AUDIT_ITEMS);
            }

            // 执行审核
            List<AuditFinding> findings = new ArrayList<>();
            int processedItems = 0;

            // 按优先级排序审核项
            List<AuditItem> sortedItems = audit.getAuditItems().stream()
                    .sorted(Comparator.comparing(AuditItem::getPriority).reversed())
                    .collect(Collectors.toList());

            for (AuditItem item : sortedItems) {
                if (processedItems >= DEFAULT_MAX_AUDIT_ITEMS) {
                    logger.warn("Reached maximum audit items limit, stopping audit");
                    break;
                }

                try {
                    AuditFinding finding = evaluateAuditItem(item, config);
                    if (finding != null) {
                        findings.add(finding);
                    }
                    processedItems++;
                } catch (Exception e) {
                    logger.error("Error evaluating audit item: {}", item.getItemId(), e);
                    AuditFinding errorFinding = new AuditFinding(
                            "ERROR_" + item.getItemId(),
                            AuditFinding.Severity.HIGH,
                            "Error evaluating audit item: " + e.getMessage(),
                            item.getItemId()
                    );
                    findings.add(errorFinding);
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // 生成审核报告
            AuditStatus status = findings.stream()
                    .anyMatch(f -> f.getSeverity() == AuditFinding.Severity.CRITICAL) ?
                    AuditStatus.FAILURE : AuditStatus.COMPLETED;

            String summary = String.format("Audit completed with %d findings in %dms", findings.size(), duration);

            AuditReport report = new AuditReport(status, summary, findings);
            report.setMetadata("processedItems", processedItems);
            report.setMetadata("duration", duration);
            report.setMetadata("criticalFindings", findings.stream()
                    .filter(f -> f.getSeverity() == AuditFinding.Severity.CRITICAL)
                    .count());

            logger.info("Quality audit {} completed: {} findings in {}ms",
                    audit.getAuditId(), findings.size(), duration);

            return report;
        } catch (Exception e) {
            logger.error("Exception occurred while conducting quality audit: {}", audit.getAuditId(), e);
            return new AuditReport(AuditStatus.FAILURE, "Exception: " + e.getMessage(), null);
        } finally {
            activeAudits.decrementAndGet();
        }
    }

    /**
     * 评估审核项
     */
    private static AuditFinding evaluateAuditItem(AuditItem item, AuditConfig config) {
        // 应用合规性规则
        ComplianceRule rule = ruleRegistry.get(item.getRuleName());
        if (rule == null) {
            logger.warn("No rule found for audit item: {}", item.getItemId());
            return null;
        }

        // 评估合规性
        boolean compliant = rule.evaluate(item.getEvidence(), config.getRuleParameters());

        // 如果不符合，创建发现项
        if (!compliant) {
            AuditFinding finding = new AuditFinding(
                    UUID.randomUUID().toString(),
                    item.getSeverity(),
                    "Non-compliance with rule: " + item.getRuleName(),
                    item.getItemId(),
                    rule.getRecommendation(),
                    item.getEvidence()
            );

            return finding;
        }

        return null;
    }

    /**
     * 异步质量体系审核
     */
    public static CompletableFuture<AuditReport> conductQualityAuditAsync(QualityAudit audit) {
        return conductQualityAuditAsync(audit, new AuditConfig());
    }

    /**
     * 异步质量体系审核（带配置）
     */
    public static CompletableFuture<AuditReport> conductQualityAuditAsync(QualityAudit audit, AuditConfig config) {
        return CompletableFuture.supplyAsync(() -> conductQualityAudit(audit, config), auditExecutor);
    }

    /**
     * 合规性报告生成
     */
    public static ComplianceReport generateComplianceReport(ComplianceScope scope) {
        return generateComplianceReport(scope, new ReportConfig());
    }

    /**
     * 合规性报告生成（带配置）
     */
    public static ComplianceReport generateComplianceReport(ComplianceScope scope, ReportConfig config) {
        if (scope == null) {
            logger.error("Invalid compliance scope for report generation");
            return null;
        }

        if (config == null) {
            config = new ReportConfig();
        }

        try {
            long startTime = System.currentTimeMillis();

            // 将ComplianceScope转换为AuditComplianceScope
            AuditComplianceScope internalScope = convertToInternalScope(scope);

            // 获取相关审核数据
            List<AuditReport> auditReports = getRelevantAuditReports(scope, config);

            // 计算合规性指标
            ComplianceMetrics metrics = calculateComplianceMetrics(auditReports);

            // 识别主要问题
            List<ComplianceIssue> majorIssues = identifyMajorIssues(auditReports);

            // 生成改进建议
            List<ImprovementRecommendation> recommendations = generateRecommendations(auditReports);

            // 创建报告
            ComplianceReport report = new ComplianceReport(
                    internalScope,  // 使用转换后的internalScope
                    metrics,
                    majorIssues,
                    recommendations,
                    auditReports
            );

            // 应用模板
            ComplianceTemplate template = templateRegistry.get(config.getTemplate());
            if (template != null) {
                report = template.applyTemplate(report);
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Compliance report generated for scope: {} in {}ms", scope, duration);

            return report;
        } catch (Exception e) {
            logger.error("Exception occurred while generating compliance report for scope: {}", scope, e);
            return null;
        }
    }

    /**
     * 将ComplianceScope转换为AuditComplianceScope
     */
    private static AuditComplianceScope convertToInternalScope(ComplianceScope scope) {
        if (scope == null) {
            return new AuditComplianceScope(
                    AuditComplianceScope.ComplianceScopeType.ORGANIZATION,
                    "Default Scope"
            );
        }

        // 转换范围类型
        AuditComplianceScope.ComplianceScopeType internalType;
        switch (scope) {
            case ORGANIZATION:
                internalType = AuditComplianceScope.ComplianceScopeType.ORGANIZATION;
                break;
            case DEPARTMENT:
                internalType = AuditComplianceScope.ComplianceScopeType.DEPARTMENT;
                break;
            case PROCESS:
                internalType = AuditComplianceScope.ComplianceScopeType.PROCESS;
                break;
            case PRODUCT:
                internalType = AuditComplianceScope.ComplianceScopeType.PRODUCT;
                break;
            case PROJECT:
                internalType = AuditComplianceScope.ComplianceScopeType.PROJECT;
                break;
            default:
                internalType = AuditComplianceScope.ComplianceScopeType.ORGANIZATION;
        }

        return new AuditComplianceScope(internalType, scope.getDescription());
    }

    /**
     * 获取相关审核报告
     */
    private static List<AuditReport> getRelevantAuditReports(ComplianceScope scope, ReportConfig config) {
        // 实现获取相关审核报告的逻辑
        // 这可能涉及数据库查询、文件读取等操作
        return new ArrayList<>();
    }

    /**
     * 计算合规性指标
     */
    private static ComplianceMetrics calculateComplianceMetrics(List<AuditReport> auditReports) {
        if (auditReports == null || auditReports.isEmpty()) {
            return new ComplianceMetrics(0, 0, 0, 0, new HashMap<>());
        }

        int totalAudits = auditReports.size();
        int passedAudits = (int) auditReports.stream()
                .filter(report -> report.getStatus() == AuditStatus.COMPLETED)
                .count();
        int failedAudits = totalAudits - passedAudits;

        double complianceRate = totalAudits > 0 ? (double) passedAudits / totalAudits : 0;

        // 按严重程度统计发现项
        Map<AuditFinding.Severity, Integer> findingSeverityCount = new EnumMap<>(AuditFinding.Severity.class);
        for (AuditReport report : auditReports) {
            if (report.getFindings() != null) {
                for (AuditFinding finding : report.getFindings()) {
                    findingSeverityCount.merge(finding.getSeverity(), 1, Integer::sum);
                }
            }
        }

        return new ComplianceMetrics(
                totalAudits,
                passedAudits,
                failedAudits,
                complianceRate,
                findingSeverityCount
        );
    }

    /**
     * 识别主要问题
     */
    private static List<ComplianceIssue> identifyMajorIssues(List<AuditReport> auditReports) {
        List<ComplianceIssue> issues = new ArrayList<>();

        if (auditReports == null || auditReports.isEmpty()) {
            return issues;
        }

        // 统计问题出现频率
        Map<String, Long> issueFrequency = auditReports.stream()
                .filter(report -> report.getFindings() != null)
                .flatMap(report -> report.getFindings().stream())
                .collect(Collectors.groupingBy(
                        finding -> finding.getDescription().split(":")[0], // 简化的问题分类
                        Collectors.counting()
                ));

        // 识别高频问题
        issueFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 3) // 出现3次以上的问题
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10) // 最多10个主要问题
                .forEach(entry -> {
                    ComplianceIssue issue = new ComplianceIssue(
                            entry.getKey(),
                            entry.getValue().intValue(),
                            "Frequent occurrence in audits"
                    );
                    issues.add(issue);
                });

        return issues;
    }

    /**
     * 生成改进建议
     */
    private static List<ImprovementRecommendation> generateRecommendations(List<AuditReport> auditReports) {
        List<ImprovementRecommendation> recommendations = new ArrayList<>();

        if (auditReports == null || auditReports.isEmpty()) {
            return recommendations;
        }

        // 收集所有建议
        Set<String> uniqueRecommendations = auditReports.stream()
                .filter(report -> report.getFindings() != null)
                .flatMap(report -> report.getFindings().stream())
                .map(AuditFinding::getRecommendation)
                .filter(Objects::nonNull)
                .filter(rec -> !rec.isEmpty())
                .collect(Collectors.toSet());

        // 创建建议对象
        int priority = uniqueRecommendations.size();
        for (String rec : uniqueRecommendations) {
            ImprovementRecommendation recommendation = new ImprovementRecommendation(
                    rec,
                    priority--,
                    "Based on audit findings"
            );
            recommendations.add(recommendation);
        }

        return recommendations;
    }

    /**
     * 不符合项跟踪
     */
    public static NonConformanceTracker trackNonConformances(List<NonConformance> ncList) {
        if (ncList == null || ncList.isEmpty()) {
            logger.warn("No non-conformances to track");
            return new NonConformanceTracker(new ArrayList<>());
        }

        try {
            NonConformanceTracker tracker = new NonConformanceTracker(ncList);

            // 分析不符合项趋势
            analyzeNonConformanceTrends(tracker);

            // 识别重复问题
            identifyRecurringIssues(tracker);

            // 评估纠正措施有效性
            evaluateCorrectiveActions(tracker);

            logger.debug("Tracked {} non-conformances", ncList.size());
            return tracker;
        } catch (Exception e) {
            logger.error("Exception occurred while tracking non-conformances", e);
            return new NonConformanceTracker(new ArrayList<>());
        }
    }

    /**
     * 分析不符合项趋势
     */
    private static void analyzeNonConformanceTrends(NonConformanceTracker tracker) {
        List<NonConformance> ncList = tracker.getNonConformances();
        if (ncList == null || ncList.isEmpty()) {
            return;
        }

        // 按月份统计不符合项
        Map<String, Long> monthlyTrend = ncList.stream()
                .collect(Collectors.groupingBy(
                        nc -> nc.getDetectionDate().getMonth().toString() + "-" + nc.getDetectionDate().getYear(),
                        Collectors.counting()
                ));

        tracker.setTrendData(monthlyTrend);
    }

    /**
     * 识别重复问题
     */
    private static void identifyRecurringIssues(NonConformanceTracker tracker) {
        List<NonConformance> ncList = tracker.getNonConformances();
        if (ncList == null || ncList.isEmpty()) {
            return;
        }

        // 按问题类型统计
        Map<String, Long> issueTypeCount = ncList.stream()
                .collect(Collectors.groupingBy(
                        NonConformance::getIssueType,
                        Collectors.counting()
                ));

        // 识别高频问题类型
        List<String> recurringIssues = issueTypeCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2) // 出现2次以上
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        tracker.setRecurringIssues(recurringIssues);
    }

    /**
     * 评估纠正措施有效性
     */
    private static void evaluateCorrectiveActions(NonConformanceTracker tracker) {
        List<NonConformance> ncList = tracker.getNonConformances();
        if (ncList == null || ncList.isEmpty()) {
            return;
        }

        long closedCount = ncList.stream()
                .filter(nc -> nc.getStatus() == NonConformance.Status.CLOSED)
                .count();

        long totalCount = ncList.size();
        double closureRate = totalCount > 0 ? (double) closedCount / totalCount : 0;

        tracker.setClosureRate(closureRate);
    }

    /**
     * 生成合规性缓存键
     */
    private static String generateComplianceCacheKey(Standard standard, AuditData data) {
        return standard.getStandardType().name() + "_" +
                standard.getVersion() + "_" +
                data.hashCode();
    }

    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(ComplianceResult result) {
        return System.currentTimeMillis() - result.getTimestamp() > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;
            resultCache.entrySet().removeIf(entry ->
                    entry.getValue().getTimestamp() < cutoffTime
            );
            logger.debug("Cleaned up compliance checker cache, remaining entries: {}", resultCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup compliance checker cache", e);
        }
    }

    /**
     * 生成定期报告
     */
    private static void generatePeriodicReport() {
        try {
            // 生成系统健康报告
            ComplianceStatistics stats = getStatistics();
            logger.info("Periodic compliance checker report: {}", stats);
        } catch (Exception e) {
            logger.error("Failed to generate periodic compliance report", e);
        }
    }

    /**
     * 获取统计信息
     */
    public static ComplianceStatistics getStatistics() {
        ComplianceStatistics stats = new ComplianceStatistics();
        stats.setTotalChecks(totalChecks.get());
        stats.setSuccessfulChecks(successfulChecks.get());
        stats.setFailedChecks(failedChecks.get());
        stats.setActiveAudits(activeAudits.get());
        stats.setResultCacheSize(resultCache.size());
        stats.setRegisteredCheckers(checkerRegistry.size());
        stats.setRegisteredRules(ruleRegistry.size());
        stats.setRegisteredTemplates(templateRegistry.size());
        return stats;
    }

    /**
     * 关闭合规性检查器
     */
    public static void shutdown() {
        try {
            // 关闭线程池
            auditExecutor.shutdown();
            scheduler.shutdown();

            try {
                if (!auditExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    auditExecutor.shutdownNow();
                }

                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                auditExecutor.shutdownNow();
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("ComplianceChecker shutdown completed");
        } catch (Exception e) {
            logger.error("Failed to shutdown ComplianceChecker", e);
        }
    }

    // 标准类型枚举
    public enum StandardType {
        ISO9001("质量管理体系"),
        ISO14001("环境管理体系"),
        ISO45001("职业健康安全管理体系"),
        IATF16949("汽车质量管理体系"),
        ISO13485("医疗器械质量管理体系"),
        AS9100("航空航天质量管理体系"),
        ISO22000("食品安全管理体系"),
        CUSTOM("自定义标准");

        private final String description;

        StandardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 合规性状态枚举
    public enum ComplianceStatus {
        COMPLIANT("符合"),
        NON_COMPLIANT("不符合"),
        PARTIALLY_COMPLIANT("部分符合"),
        FAILURE("检查失败"),
        PENDING("待检查");

        private final String description;

        ComplianceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 审核状态枚举
    public enum AuditStatus {
        PLANNED("已计划"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        SUSPENDED("已暂停"),
        FAILURE("失败");

        private final String description;

        AuditStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 合规性范围枚举
    public enum ComplianceScope {
        ORGANIZATION("组织级"),
        DEPARTMENT("部门级"),
        PROCESS("流程级"),
        PRODUCT("产品级"),
        PROJECT("项目级");

        private final String description;

        ComplianceScope(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 非符合项状态枚举
    public enum NonConformanceStatus {
        OPEN("打开"),
        INVESTIGATING("调查中"),
        ACTION_PLANNED("已制定措施"),
        IMPLEMENTING("实施中"),
        VERIFICATION("验证中"),
        CLOSED("已关闭"),
        REJECTED("已拒绝");

        private final String description;

        NonConformanceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 标准检查器接口
    public interface StandardChecker {
        ComplianceResult checkCompliance(Standard standard, AuditData data, ComplianceConfig config) throws Exception;

        boolean supportsStandard(StandardType standardType);

        String getCheckerName();
    }

    // 合规性规则接口
    public interface ComplianceRule {
        boolean evaluate(Object evidence, Map<String, Object> parameters);

        String getRuleName();

        String getRecommendation();

        boolean supportsEvidenceType(Class<?> evidenceType);
    }

    // 合规性模板接口
    public interface ComplianceTemplate {
        ComplianceReport applyTemplate(ComplianceReport report);

        String getTemplateName();

        boolean supportsScope(ComplianceScope scope);
    }

    // 合规性配置类
    public static class ComplianceConfig {
        private long checkTimeout = DEFAULT_CHECK_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private long retryDelay = DEFAULT_RETRY_DELAY;
        private boolean enableCache = true;
        private Map<String, Object> checkParameters = new ConcurrentHashMap<>();

        // Getters and Setters
        public long getCheckTimeout() {
            return checkTimeout;
        }

        public ComplianceConfig setCheckTimeout(long checkTimeout) {
            this.checkTimeout = checkTimeout;
            return this;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public ComplianceConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public ComplianceConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public ComplianceConfig setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public Map<String, Object> getCheckParameters() {
            return new HashMap<>(checkParameters);
        }

        public ComplianceConfig setCheckParameter(String key, Object value) {
            this.checkParameters.put(key, value);
            return this;
        }

        public Object getCheckParameter(String key) {
            return this.checkParameters.get(key);
        }

        @Override
        public String toString() {
            return "ComplianceConfig{" +
                    "checkTimeout=" + checkTimeout +
                    ", maxRetries=" + maxRetries +
                    ", retryDelay=" + retryDelay +
                    ", enableCache=" + enableCache +
                    ", parameterCount=" + checkParameters.size() +
                    '}';
        }
    }

    // 审核配置类
    public static class AuditConfig {
        private long auditTimeout = DEFAULT_CHECK_TIMEOUT;
        private int maxConcurrentAudits = DEFAULT_THREAD_POOL_SIZE;
        private Map<String, Object> ruleParameters = new ConcurrentHashMap<>();

        // Getters and Setters
        public long getAuditTimeout() {
            return auditTimeout;
        }

        public AuditConfig setAuditTimeout(long auditTimeout) {
            this.auditTimeout = auditTimeout;
            return this;
        }

        public int getMaxConcurrentAudits() {
            return maxConcurrentAudits;
        }

        public AuditConfig setMaxConcurrentAudits(int maxConcurrentAudits) {
            this.maxConcurrentAudits = maxConcurrentAudits;
            return this;
        }

        public Map<String, Object> getRuleParameters() {
            return new HashMap<>(ruleParameters);
        }

        public AuditConfig setRuleParameter(String key, Object value) {
            this.ruleParameters.put(key, value);
            return this;
        }

        public Object getRuleParameter(String key) {
            return this.ruleParameters.get(key);
        }

        @Override
        public String toString() {
            return "AuditConfig{" +
                    "auditTimeout=" + auditTimeout +
                    ", maxConcurrentAudits=" + maxConcurrentAudits +
                    ", parameterCount=" + ruleParameters.size() +
                    '}';
        }
    }

    // 报告配置类
    public static class ReportConfig {
        private String template = "default";
        private boolean includeCharts = true;
        private boolean includeRecommendations = true;
        private int maxIssuesToShow = 50;

        // Getters and Setters
        public String getTemplate() {
            return template;
        }

        public ReportConfig setTemplate(String template) {
            this.template = template;
            return this;
        }

        public boolean isIncludeCharts() {
            return includeCharts;
        }

        public ReportConfig setIncludeCharts(boolean includeCharts) {
            this.includeCharts = includeCharts;
            return this;
        }

        public boolean isIncludeRecommendations() {
            return includeRecommendations;
        }

        public ReportConfig setIncludeRecommendations(boolean includeRecommendations) {
            this.includeRecommendations = includeRecommendations;
            return this;
        }

        public int getMaxIssuesToShow() {
            return maxIssuesToShow;
        }

        public ReportConfig setMaxIssuesToShow(int maxIssuesToShow) {
            this.maxIssuesToShow = maxIssuesToShow;
            return this;
        }

        @Override
        public String toString() {
            return "ReportConfig{" +
                    "template='" + template + '\'' +
                    ", includeCharts=" + includeCharts +
                    ", includeRecommendations=" + includeRecommendations +
                    ", maxIssuesToShow=" + maxIssuesToShow +
                    '}';
        }
    }

    // 标准类
    public static class Standard {
        private final String standardId;
        private final StandardType standardType;
        private final String version;
        private final String description;
        private final LocalDateTime effectiveDate;
        private final Map<String, Object> attributes;

        public Standard(StandardType standardType, String version) {
            this.standardId = UUID.randomUUID().toString();
            this.standardType = standardType != null ? standardType : StandardType.CUSTOM;
            this.version = version != null ? version : "1.0";
            this.description = "";
            this.effectiveDate = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getStandardId() {
            return standardId;
        }

        public StandardType getStandardType() {
            return standardType;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public Standard setDescription(String description) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getEffectiveDate() {
            return effectiveDate;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public Standard setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "Standard{" +
                    "standardId='" + standardId + '\'' +
                    ", standardType=" + standardType +
                    ", version='" + version + '\'' +
                    ", effectiveDate=" + effectiveDate +
                    ", attributeCount=" + attributes.size() +
                    '}';
        }
    }

    // 审核数据类
    public static class AuditData {
        private final String dataId;
        private final Map<String, Object> data;
        private final LocalDateTime createTime;
        private final String source;

        public AuditData(String source) {
            this.dataId = UUID.randomUUID().toString();
            this.data = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
            this.source = source != null ? source : "unknown";
        }

        // Getters and Setters
        public String getDataId() {
            return dataId;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getSource() {
            return source;
        }

        public Map<String, Object> getData() {
            return new HashMap<>(data);
        }

        public AuditData setData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public Object getData(String key) {
            return this.data.get(key);
        }

        @Override
        public String toString() {
            return "AuditData{" +
                    "dataId='" + dataId + '\'' +
                    ", createTime=" + createTime +
                    ", source='" + source + '\'' +
                    ", dataCount=" + data.size() +
                    '}';
        }
    }

    // 合规性结果类
    public static class ComplianceResult {
        private final String resultId;
        private final ComplianceStatus status;
        private final String message;
        private final Object details;
        private final long timestamp;
        private final long duration;
        private final Map<String, Object> metadata;

        public ComplianceResult(ComplianceStatus status, String message, Object details) {
            this.resultId = UUID.randomUUID().toString();
            this.status = status != null ? status : ComplianceStatus.FAILURE;
            this.message = message != null ? message : "";
            this.details = details;
            this.timestamp = System.currentTimeMillis();
            this.duration = 0;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public ComplianceStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Object getDetails() {
            return details;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDuration() {
            return duration;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ComplianceResult setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ComplianceResult{" +
                    "resultId='" + resultId + '\'' +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    ", detailsPresent=" + (details != null) +
                    ", timestamp=" + timestamp +
                    ", duration=" + duration + "ms" +
                    '}';
        }
    }

    // 质量审核类
    public static class QualityAudit {
        private final String auditId;
        private final String auditName;
        private final List<AuditItem> auditItems;
        private final LocalDateTime plannedDate;
        private final LocalDateTime actualDate;
        private final String auditor;
        private final Map<String, Object> attributes;

        public QualityAudit(String auditName) {
            this.auditId = UUID.randomUUID().toString();
            this.auditName = auditName != null ? auditName : "Unnamed Audit";
            this.auditItems = new CopyOnWriteArrayList<>();
            this.plannedDate = LocalDateTime.now();
            this.actualDate = null;
            this.auditor = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getAuditId() {
            return auditId;
        }

        public String getAuditName() {
            return auditName;
        }

        public LocalDateTime getPlannedDate() {
            return plannedDate;
        }

        public LocalDateTime getActualDate() {
            return actualDate;
        }

        public String getAuditor() {
            return auditor;
        }

        public List<AuditItem> getAuditItems() {
            return new ArrayList<>(auditItems);
        }

        public void addAuditItem(AuditItem item) {
            this.auditItems.add(item);
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public QualityAudit setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "QualityAudit{" +
                    "auditId='" + auditId + '\'' +
                    ", auditName='" + auditName + '\'' +
                    ", itemCount=" + auditItems.size() +
                    ", plannedDate=" + plannedDate +
                    ", auditor='" + auditor + '\'' +
                    '}';
        }
    }

    // 审核项类
    public static class AuditItem {
        private final String itemId;
        private final String description;
        private final String ruleName;
        private final AuditFinding.Severity severity;
        private final Priority priority;
        private final Object evidence;
        private final Map<String, Object> attributes;

        public AuditItem(String description, String ruleName, AuditFinding.Severity severity) {
            this.itemId = UUID.randomUUID().toString();
            this.description = description != null ? description : "";
            this.ruleName = ruleName != null ? ruleName : "";
            this.severity = severity != null ? severity : AuditFinding.Severity.LOW;
            this.priority = Priority.MEDIUM;
            this.evidence = null;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public String getRuleName() {
            return ruleName;
        }

        public AuditFinding.Severity getSeverity() {
            return severity;
        }

        public Priority getPriority() {
            return priority;
        }

        public Object getEvidence() {
            return evidence;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public AuditItem setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "AuditItem{" +
                    "itemId='" + itemId + '\'' +
                    ", description='" + description + '\'' +
                    ", ruleName='" + ruleName + '\'' +
                    ", severity=" + severity +
                    ", priority=" + priority +
                    ", evidencePresent=" + (evidence != null) +
                    '}';
        }

        public enum Priority {
            LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

            private final int level;

            Priority(int level) {
                this.level = level;
            }

            public int getLevel() {
                return level;
            }
        }
    }

    // 审核发现类
    // 修改AuditFinding类，添加带推荐和证据的构造函数
    public static class AuditFinding {
        private final String findingId;
        private final Severity severity;
        private final String description;
        private final String relatedItemId;
        private final LocalDateTime detectionTime;
        private final String recommendation;
        private final Object evidence;
        private final Map<String, Object> attributes;

        // 原始构造函数
        public AuditFinding(String findingId, Severity severity, String description, String relatedItemId) {
            this(findingId, severity, description, relatedItemId, "", null);
        }

        // 新增构造函数，包含推荐和证据
        public AuditFinding(String findingId, Severity severity, String description, String relatedItemId,
                            String recommendation, Object evidence) {
            this.findingId = findingId != null ? findingId : UUID.randomUUID().toString();
            this.severity = severity != null ? severity : Severity.LOW;
            this.description = description != null ? description : "";
            this.relatedItemId = relatedItemId != null ? relatedItemId : "";
            this.detectionTime = LocalDateTime.now();
            this.recommendation = recommendation != null ? recommendation : "";
            this.evidence = evidence;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getFindingId() {
            return findingId;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getDescription() {
            return description;
        }

        public String getRelatedItemId() {
            return relatedItemId;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public Object getEvidence() {
            return evidence;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public AuditFinding setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "AuditFinding{" +
                    "findingId='" + findingId + '\'' +
                    ", severity=" + severity +
                    ", description='" + description + '\'' +
                    ", relatedItemId='" + relatedItemId + '\'' +
                    ", detectionTime=" + detectionTime +
                    ", recommendation='" + recommendation + '\'' +
                    ", evidencePresent=" + (evidence != null) +
                    '}';
        }

        public enum Severity {
            LOW("低"),
            MEDIUM("中"),
            HIGH("高"),
            CRITICAL("严重");

            private final String description;

            Severity(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }
    }


    // 审核报告类
    public static class AuditReport {
        private final String reportId;
        private final AuditStatus status;
        private final String summary;
        private final List<AuditFinding> findings;
        private final LocalDateTime generationTime;
        private final Map<String, Object> metadata;

        public AuditReport(AuditStatus status, String summary, List<AuditFinding> findings) {
            this.reportId = UUID.randomUUID().toString();
            this.status = status != null ? status : AuditStatus.FAILURE;
            this.summary = summary != null ? summary : "";
            this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
            this.generationTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getReportId() {
            return reportId;
        }

        public AuditStatus getStatus() {
            return status;
        }

        public String getSummary() {
            return summary;
        }

        public List<AuditFinding> getFindings() {
            return new ArrayList<>(findings);
        }

        public LocalDateTime getGenerationTime() {
            return generationTime;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public AuditReport setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "AuditReport{" +
                    "reportId='" + reportId + '\'' +
                    ", status=" + status +
                    ", summary='" + summary + '\'' +
                    ", findingCount=" + findings.size() +
                    ", generationTime=" + generationTime +
                    '}';
        }
    }

    // 合规性范围类
    public static class AuditComplianceScope {
        private final String scopeId;
        private final ComplianceScopeType scopeType;
        private final String scopeName;
        private final List<String> includedEntities;
        private final LocalDateTime createTime;

        public AuditComplianceScope(ComplianceScopeType scopeType, String scopeName) {
            this.scopeId = UUID.randomUUID().toString();
            this.scopeType = scopeType != null ? scopeType : ComplianceScopeType.ORGANIZATION;
            this.scopeName = scopeName != null ? scopeName : "Unnamed Scope";
            this.includedEntities = new CopyOnWriteArrayList<>();
            this.createTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getScopeId() {
            return scopeId;
        }

        public ComplianceScopeType getScopeType() {
            return scopeType;
        }

        public String getScopeName() {
            return scopeName;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public List<String> getIncludedEntities() {
            return new ArrayList<>(includedEntities);
        }

        public void addIncludedEntity(String entity) {
            this.includedEntities.add(entity);
        }

        @Override
        public String toString() {
            return "AuditComplianceScope{" +
                    "scopeId='" + scopeId + '\'' +
                    ", scopeType=" + scopeType +
                    ", scopeName='" + scopeName + '\'' +
                    ", entityCount=" + includedEntities.size() +
                    ", createTime=" + createTime +
                    '}';
        }

        // 范围类型枚举
        public enum ComplianceScopeType {
            ORGANIZATION("组织"),
            DEPARTMENT("部门"),
            PROCESS("流程"),
            PRODUCT("产品"),
            PROJECT("项目");

            private final String description;

            ComplianceScopeType(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }
    }


    // 合规性指标类
    public static class ComplianceMetrics {
        private final int totalAudits;
        private final int passedAudits;
        private final int failedAudits;
        private final double complianceRate;
        private final Map<AuditFinding.Severity, Integer> findingSeverityDistribution;

        public ComplianceMetrics(int totalAudits, int passedAudits, int failedAudits,
                                 double complianceRate, Map<AuditFinding.Severity, Integer> findingSeverityDistribution) {
            this.totalAudits = totalAudits;
            this.passedAudits = passedAudits;
            this.failedAudits = failedAudits;
            this.complianceRate = complianceRate;
            this.findingSeverityDistribution = findingSeverityDistribution != null ?
                    new EnumMap<>(findingSeverityDistribution) : new EnumMap<>(AuditFinding.Severity.class);
        }

        // Getters
        public int getTotalAudits() {
            return totalAudits;
        }

        public int getPassedAudits() {
            return passedAudits;
        }

        public int getFailedAudits() {
            return failedAudits;
        }

        public double getComplianceRate() {
            return complianceRate;
        }

        public Map<AuditFinding.Severity, Integer> getFindingSeverityDistribution() {
            return new EnumMap<>(findingSeverityDistribution);
        }

        @Override
        public String toString() {
            return "ComplianceMetrics{" +
                    "totalAudits=" + totalAudits +
                    ", passedAudits=" + passedAudits +
                    ", failedAudits=" + failedAudits +
                    ", complianceRate=" + String.format("%.2f", complianceRate * 100) + "%" +
                    ", findingSeverityDistribution=" + findingSeverityDistribution +
                    '}';
        }
    }

    // 合规性问题类
    public static class ComplianceIssue {
        private final String issueId;
        private final String issueType;
        private final int occurrenceCount;
        private final String description;
        private final LocalDateTime firstOccurrence;
        private final LocalDateTime lastOccurrence;

        public ComplianceIssue(String issueType, int occurrenceCount, String description) {
            this.issueId = UUID.randomUUID().toString();
            this.issueType = issueType != null ? issueType : "Unknown";
            this.occurrenceCount = occurrenceCount;
            this.description = description != null ? description : "";
            this.firstOccurrence = LocalDateTime.now();
            this.lastOccurrence = LocalDateTime.now();
        }

        // Getters
        public String getIssueId() {
            return issueId;
        }

        public String getIssueType() {
            return issueType;
        }

        public int getOccurrenceCount() {
            return occurrenceCount;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getFirstOccurrence() {
            return firstOccurrence;
        }

        public LocalDateTime getLastOccurrence() {
            return lastOccurrence;
        }

        @Override
        public String toString() {
            return "ComplianceIssue{" +
                    "issueId='" + issueId + '\'' +
                    ", issueType='" + issueType + '\'' +
                    ", occurrenceCount=" + occurrenceCount +
                    ", description='" + description + '\'' +
                    ", firstOccurrence=" + firstOccurrence +
                    ", lastOccurrence=" + lastOccurrence +
                    '}';
        }
    }

    // 改进建议类
    public static class ImprovementRecommendation {
        private final String recommendationId;
        private final String description;
        private final int priority;
        private final String rationale;
        private final LocalDateTime createTime;

        public ImprovementRecommendation(String description, int priority, String rationale) {
            this.recommendationId = UUID.randomUUID().toString();
            this.description = description != null ? description : "";
            this.priority = priority;
            this.rationale = rationale != null ? rationale : "";
            this.createTime = LocalDateTime.now();
        }

        // Getters
        public String getRecommendationId() {
            return recommendationId;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public String getRationale() {
            return rationale;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        @Override
        public String toString() {
            return "ImprovementRecommendation{" +
                    "recommendationId='" + recommendationId + '\'' +
                    ", description='" + description + '\'' +
                    ", priority=" + priority +
                    ", rationale='" + rationale + '\'' +
                    ", createTime=" + createTime +
                    '}';
        }
    }

    // 合规性报告类
    public static class ComplianceReport {
        private final String reportId;
        private final AuditComplianceScope scope;  // 修改这里
        private final ComplianceMetrics metrics;
        private final List<ComplianceIssue> majorIssues;
        private final List<ImprovementRecommendation> recommendations;
        private final List<AuditReport> auditReports;
        private final LocalDateTime generationTime;
        private final Map<String, Object> additionalData;

        public ComplianceReport(AuditComplianceScope scope, ComplianceMetrics metrics,  // 修改这里
                                List<ComplianceIssue> majorIssues, List<ImprovementRecommendation> recommendations,
                                List<AuditReport> auditReports) {
            this.reportId = UUID.randomUUID().toString();
            this.scope = scope;
            this.metrics = metrics;
            this.majorIssues = majorIssues != null ? new ArrayList<>(majorIssues) : new ArrayList<>();
            this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
            this.auditReports = auditReports != null ? new ArrayList<>(auditReports) : new ArrayList<>();
            this.generationTime = LocalDateTime.now();
            this.additionalData = new ConcurrentHashMap<>();
        }

        // Getters
        public String getReportId() {
            return reportId;
        }

        public AuditComplianceScope getScope() {
            return scope;
        }  // 修改这里

        public ComplianceMetrics getMetrics() {
            return metrics;
        }

        public List<ComplianceIssue> getMajorIssues() {
            return new ArrayList<>(majorIssues);
        }

        public List<ImprovementRecommendation> getRecommendations() {
            return new ArrayList<>(recommendations);
        }

        public List<AuditReport> getAuditReports() {
            return new ArrayList<>(auditReports);
        }

        public LocalDateTime getGenerationTime() {
            return generationTime;
        }

        public Map<String, Object> getAdditionalData() {
            return new HashMap<>(additionalData);
        }

        public ComplianceReport setAdditionalData(String key, Object value) {
            this.additionalData.put(key, value);
            return this;
        }

        public Object getAdditionalData(String key) {
            return this.additionalData.get(key);
        }

        @Override
        public String toString() {
            return "ComplianceReport{" +
                    "reportId='" + reportId + '\'' +
                    ", scope=" + scope.getScopeName() +
                    ", complianceRate=" + String.format("%.2f", metrics.getComplianceRate() * 100) + "%" +
                    ", majorIssues=" + majorIssues.size() +
                    ", recommendations=" + recommendations.size() +
                    ", auditReports=" + auditReports.size() +
                    ", generationTime=" + generationTime +
                    '}';
        }
    }


    // 非符合项类
    public static class NonConformance {
        private final String ncId;
        private final String issueType;
        private final String description;
        private final LocalDateTime detectionDate;
        private final LocalDateTime dueDate;
        private final Status status;
        private final String responsiblePerson;
        private final List<String> correctiveActions;
        private final Map<String, Object> attributes;

        public NonConformance(String issueType, String description) {
            this.ncId = UUID.randomUUID().toString();
            this.issueType = issueType != null ? issueType : "Unknown";
            this.description = description != null ? description : "";
            this.detectionDate = LocalDateTime.now();
            this.dueDate = detectionDate.plusDays(30); // 默认30天期限
            this.status = Status.OPEN;
            this.responsiblePerson = "";
            this.correctiveActions = new CopyOnWriteArrayList<>();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getNcId() {
            return ncId;
        }

        public String getIssueType() {
            return issueType;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getDetectionDate() {
            return detectionDate;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public Status getStatus() {
            return status;
        }

        public String getResponsiblePerson() {
            return responsiblePerson;
        }

        public List<String> getCorrectiveActions() {
            return new ArrayList<>(correctiveActions);
        }

        public void addCorrectiveAction(String action) {
            this.correctiveActions.add(action);
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public NonConformance setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "NonConformance{" +
                    "ncId='" + ncId + '\'' +
                    ", issueType='" + issueType + '\'' +
                    ", description='" + description + '\'' +
                    ", detectionDate=" + detectionDate +
                    ", dueDate=" + dueDate +
                    ", status=" + status +
                    ", responsiblePerson='" + responsiblePerson + '\'' +
                    ", actionCount=" + correctiveActions.size() +
                    '}';
        }

        public enum Status {
            OPEN("打开"),
            INVESTIGATING("调查中"),
            ACTION_PLANNED("已制定措施"),
            IMPLEMENTING("实施中"),
            VERIFICATION("验证中"),
            CLOSED("已关闭"),
            REJECTED("已拒绝");

            private final String description;

            Status(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }
        }
    }

    // 非符合项跟踪器类
    public static class NonConformanceTracker {
        private final List<NonConformance> nonConformances;
        private Map<String, Long> trendData;
        private List<String> recurringIssues;
        private double closureRate;

        public NonConformanceTracker(List<NonConformance> nonConformances) {
            this.nonConformances = nonConformances != null ? new ArrayList<>(nonConformances) : new ArrayList<>();
            this.trendData = new ConcurrentHashMap<>();
            this.recurringIssues = new ArrayList<>();
            this.closureRate = 0.0;
        }

        // Getters and Setters
        public List<NonConformance> getNonConformances() {
            return new ArrayList<>(nonConformances);
        }

        public Map<String, Long> getTrendData() {
            return new HashMap<>(trendData);
        }

        public void setTrendData(Map<String, Long> trendData) {
            this.trendData = trendData;
        }

        public List<String> getRecurringIssues() {
            return new ArrayList<>(recurringIssues);
        }

        public void setRecurringIssues(List<String> recurringIssues) {
            this.recurringIssues = recurringIssues;
        }

        public double getClosureRate() {
            return closureRate;
        }

        public void setClosureRate(double closureRate) {
            this.closureRate = closureRate;
        }

        @Override
        public String toString() {
            return "NonConformanceTracker{" +
                    "ncCount=" + nonConformances.size() +
                    ", trendDataPoints=" + trendData.size() +
                    ", recurringIssues=" + recurringIssues.size() +
                    ", closureRate=" + String.format("%.2f", closureRate * 100) + "%" +
                    '}';
        }
    }

    // 合规性统计信息类
    public static class ComplianceStatistics {
        private final LocalDateTime lastUpdate;
        private long totalChecks;
        private long successfulChecks;
        private long failedChecks;
        private int activeAudits;
        private int resultCacheSize;
        private int registeredCheckers;
        private int registeredRules;
        private int registeredTemplates;

        public ComplianceStatistics() {
            this.totalChecks = 0;
            this.successfulChecks = 0;
            this.failedChecks = 0;
            this.activeAudits = 0;
            this.resultCacheSize = 0;
            this.registeredCheckers = 0;
            this.registeredRules = 0;
            this.registeredTemplates = 0;
            this.lastUpdate = LocalDateTime.now();
        }

        // Getters and Setters
        public long getTotalChecks() {
            return totalChecks;
        }

        public void setTotalChecks(long totalChecks) {
            this.totalChecks = totalChecks;
        }

        public long getSuccessfulChecks() {
            return successfulChecks;
        }

        public void setSuccessfulChecks(long successfulChecks) {
            this.successfulChecks = successfulChecks;
        }

        public long getFailedChecks() {
            return failedChecks;
        }

        public void setFailedChecks(long failedChecks) {
            this.failedChecks = failedChecks;
        }

        public int getActiveAudits() {
            return activeAudits;
        }

        public void setActiveAudits(int activeAudits) {
            this.activeAudits = activeAudits;
        }

        public int getResultCacheSize() {
            return resultCacheSize;
        }

        public void setResultCacheSize(int resultCacheSize) {
            this.resultCacheSize = resultCacheSize;
        }

        public int getRegisteredCheckers() {
            return registeredCheckers;
        }

        public void setRegisteredCheckers(int registeredCheckers) {
            this.registeredCheckers = registeredCheckers;
        }

        public int getRegisteredRules() {
            return registeredRules;
        }

        public void setRegisteredRules(int registeredRules) {
            this.registeredRules = registeredRules;
        }

        public int getRegisteredTemplates() {
            return registeredTemplates;
        }

        public void setRegisteredTemplates(int registeredTemplates) {
            this.registeredTemplates = registeredTemplates;
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public double getSuccessRate() {
            return totalChecks > 0 ? (double) successfulChecks / totalChecks * 100 : 0.0;
        }

        @Override
        public String toString() {
            return "ComplianceStatistics{" +
                    "totalChecks=" + totalChecks +
                    ", successfulChecks=" + successfulChecks +
                    ", failedChecks=" + failedChecks +
                    ", successRate=" + String.format("%.2f", getSuccessRate()) + "%" +
                    ", activeAudits=" + activeAudits +
                    ", resultCacheSize=" + resultCacheSize +
                    ", registeredCheckers=" + registeredCheckers +
                    ", registeredRules=" + registeredRules +
                    ", registeredTemplates=" + registeredTemplates +
                    ", lastUpdate=" + lastUpdate +
                    '}';
        }
    }
}
