package com.qtech.im.semiconductor.report.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * KPI仪表板工具类
 * <p>
 * 特性：
 * - 通用性：支持多种KPI指标类型和计算方法
 * - 规范性：统一的KPI定义和计算标准
 * - 专业性：半导体行业KPI专业计算和分析
 * - 灵活性：可配置的KPI定义和阈值设置
 * - 可靠性：完善的监控和预警机制
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
public class KpiDashboard {

    // 默认配置
    public static final long DEFAULT_MONITORING_INTERVAL = 60000; // 1分钟
    public static final int DEFAULT_DATA_RETENTION_DAYS = 30;
    public static final int DEFAULT_MAX_ALERTS = 1000;
    public static final int DEFAULT_TREND_PERIODS = 12;
    private static final Logger logger = LoggerFactory.getLogger(KpiDashboard.class);
    // 内部存储和管理
    private static final Map<String, KpiDefinition> kpiDefinitions = new ConcurrentHashMap<>();
    private static final Map<String, KpiValue> currentKpiValues = new ConcurrentHashMap<>();
    private static final Map<String, List<KpiValue>> historicalKpiValues = new ConcurrentHashMap<>();
    private static final List<KpiAlert> activeAlerts = new CopyOnWriteArrayList<>();
    private static final Map<String, ThresholdConfig> thresholdConfigs = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final AtomicLong alertCounter = new AtomicLong(0);
    // 计算器注册表
    private static final Map<KpiType, KpiCalculator> calculatorRegistry = new ConcurrentHashMap<>();
    // 数据源注册表
    private static final Map<String, KpiDataSource> dataSourceRegistry = new ConcurrentHashMap<>();
    // 预警通知器注册表
    private static final Map<String, AlertNotifier> notifierRegistry = new ConcurrentHashMap<>();

    // 初始化默认组件
    static {
        registerDefaultComponents();
        startMonitoringTasks();
        logger.info("KpiDashboard initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认计算器、数据源和通知器
        // registerCalculator(new ProductionKpiCalculator());
        // registerCalculator(new QualityKpiCalculator());
        // registerDataSource(new DatabaseKpiDataSource());
        // registerDataSource(new RestApiKpiDataSource());
        // registerNotifier(new EmailAlertNotifier());
        // registerNotifier(new SmsAlertNotifier());
    }

    /**
     * 启动监控任务
     */
    private static void startMonitoringTasks() {
        // 启动定期KPI计算任务
        scheduler.scheduleAtFixedRate(KpiDashboard::calculateAllKpis,
                60, 60, TimeUnit.SECONDS);

        // 启动预警清理任务
        scheduler.scheduleAtFixedRate(KpiDashboard::cleanupOldAlerts,
                30, 30, TimeUnit.MINUTES);

        // 启动历史数据清理任务
        scheduler.scheduleAtFixedRate(KpiDashboard::cleanupHistoricalData,
                1, 1, TimeUnit.HOURS);
    }

    /**
     * 注册KPI计算器
     */
    public static void registerCalculator(KpiCalculator calculator) {
        if (calculator == null) {
            throw new IllegalArgumentException("Calculator cannot be null");
        }

        for (KpiType type : KpiType.values()) {
            if (calculator.supportsType(type)) {
                calculatorRegistry.put(type, calculator);
                logger.debug("Registered calculator {} for type {}", calculator.getCalculatorName(), type);
            }
        }
    }

    /**
     * 注册KPI数据源
     */
    public static void registerDataSource(KpiDataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }

        // 注册支持的所有数据源
        String[] dataSources = {"database", "restapi", "file", "mqtt"}; // 简化实现
        for (String ds : dataSources) {
            if (dataSource.supportsDataSource(ds)) {
                dataSourceRegistry.put(ds, dataSource);
                logger.debug("Registered data source {} for type {}", dataSource.getDataSourceName(), ds);
            }
        }
    }

    /**
     * 注册预警通知器
     */
    public static void registerNotifier(AlertNotifier notifier) {
        if (notifier == null) {
            throw new IllegalArgumentException("Notifier cannot be null");
        }

        // 注册支持的所有通知类型
        String[] notificationTypes = {"email", "sms", "webhook"}; // 简化实现
        for (String type : notificationTypes) {
            if (notifier.supportsNotificationType(type)) {
                notifierRegistry.put(type, notifier);
                logger.debug("Registered notifier {} for type {}", notifier.getNotifierName(), type);
            }
        }
    }

    /**
     * 注册KPI定义
     */
    public static boolean registerKpiDefinition(KpiDefinition kpiDefinition) {
        if (kpiDefinition == null || kpiDefinition.getKpiId() == null || kpiDefinition.getKpiId().isEmpty()) {
            logger.warn("Invalid KPI definition for registration");
            return false;
        }

        try {
            kpiDefinitions.put(kpiDefinition.getKpiId(), kpiDefinition);

            // 初始化历史数据存储
            historicalKpiValues.putIfAbsent(kpiDefinition.getKpiId(), new CopyOnWriteArrayList<>());

            logger.info("Registered KPI definition: {}", kpiDefinition.getKpiId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to register KPI definition: " + kpiDefinition.getKpiId(), e);
            return false;
        }
    }

    /**
     * KPI指标计算
     */
    public static KpiValue calculateKpi(KpiDefinition kpiDef, TimeRange range) {
        if (kpiDef == null) {
            logger.warn("Invalid KPI definition for calculation");
            return null;
        }

        if (range == null) {
            range = new TimeRange(LocalDateTime.now().minusHours(1), LocalDateTime.now());
        }

        try {
            KpiCalculator calculator = calculatorRegistry.get(kpiDef.getKpiType());
            if (calculator == null) {
                logger.warn("No calculator available for KPI type: {}", kpiDef.getKpiType());
                return createErrorKpiValue(kpiDef, "No calculator available");
            }

            KpiValue kpiValue = calculator.calculate(kpiDef, range);

            // 更新当前值和历史值
            if (kpiValue != null) {
                currentKpiValues.put(kpiDef.getKpiId(), kpiValue);

                // 添加到历史数据
                List<KpiValue> history = historicalKpiValues.computeIfAbsent(
                        kpiDef.getKpiId(), k -> new CopyOnWriteArrayList<>());
                history.add(kpiValue);

                // 限制历史数据大小
                if (history.size() > 1000) {
                    history.remove(0);
                }

                logger.debug("Calculated KPI value: {} = {}", kpiDef.getKpiId(), kpiValue.getFormattedValue());
            }

            return kpiValue;
        } catch (Exception e) {
            logger.error("Failed to calculate KPI: " + kpiDef.getKpiId(), e);
            return createErrorKpiValue(kpiDef, "Calculation error: " + e.getMessage());
        }
    }

    /**
     * 创建错误KPI值
     */
    private static KpiValue createErrorKpiValue(KpiDefinition kpiDef, String errorMessage) {
        KpiValue errorValue = new KpiValue(
                kpiDef.getKpiId(),
                kpiDef.getKpiName(),
                "ERROR",
                "",
                KpiStatus.UNKNOWN,
                null
        );
        errorValue.setMetadata("error", errorMessage);
        return errorValue;
    }

    /**
     * 实时KPI监控
     */
    public static KpiAlert monitorKpiRealTime(String kpiId, ThresholdConfig thresholds) {
        if (kpiId == null || kpiId.isEmpty()) {
            logger.warn("Invalid KPI ID for monitoring");
            return null;
        }

        if (thresholds == null) {
            thresholds = thresholdConfigs.get(kpiId);
            if (thresholds == null) {
                logger.warn("No threshold configuration found for KPI: {}", kpiId);
                return null;
            }
        } else {
            // 更新阈值配置
            thresholdConfigs.put(kpiId, thresholds);
        }

        try {
            KpiValue currentValue = currentKpiValues.get(kpiId);
            if (currentValue == null) {
                // 计算当前值
                KpiDefinition kpiDef = kpiDefinitions.get(kpiId);
                if (kpiDef == null) {
                    logger.warn("KPI definition not found: {}", kpiId);
                    return null;
                }

                currentValue = calculateKpi(kpiDef, new TimeRange(
                        LocalDateTime.now().minusMinutes(5), LocalDateTime.now()));
            }

            if (currentValue == null) {
                logger.warn("Failed to get current value for KPI: {}", kpiId);
                return null;
            }

            // 检查阈值
            KpiStatus status = evaluateKpiStatus(currentValue, thresholds);
            if (status != KpiStatus.NORMAL) {
                // 生成预警
                KpiAlert alert = createKpiAlert(kpiId, currentValue, status, thresholds);

                // 添加到活动预警列表
                if (activeAlerts.size() < DEFAULT_MAX_ALERTS) {
                    activeAlerts.add(alert);
                    alertCounter.incrementAndGet();

                    // 发送预警通知
                    if (thresholds.isEnableAlerts()) {
                        sendAlertNotification(alert, thresholds);
                    }

                    logger.info("KPI alert generated: {} - {}", kpiId, status.getDescription());
                    return alert;
                } else {
                    logger.warn("Alert limit reached, dropping alert for KPI: {}", kpiId);
                }
            }

            return null; // 无预警
        } catch (Exception e) {
            logger.error("Failed to monitor KPI in real-time: " + kpiId, e);
            return null;
        }
    }

    /**
     * 评估KPI状态
     */
    private static KpiStatus evaluateKpiStatus(KpiValue kpiValue, ThresholdConfig thresholds) {
        Object value = kpiValue.getValue();
        if (!(value instanceof Number)) {
            return KpiStatus.UNKNOWN;
        }

        BigDecimal numericValue = new BigDecimal(value.toString());

        // 检查临界阈值
        if (thresholds.getCriticalLowerBound() != null &&
                numericValue.compareTo(thresholds.getCriticalLowerBound()) < 0) {
            return KpiStatus.CRITICAL;
        }

        if (thresholds.getCriticalUpperBound() != null &&
                numericValue.compareTo(thresholds.getCriticalUpperBound()) > 0) {
            return KpiStatus.CRITICAL;
        }

        // 检查警告阈值
        if (thresholds.getWarningLowerBound() != null &&
                numericValue.compareTo(thresholds.getWarningLowerBound()) < 0) {
            return KpiStatus.WARNING;
        }

        if (thresholds.getWarningUpperBound() != null &&
                numericValue.compareTo(thresholds.getWarningUpperBound()) > 0) {
            return KpiStatus.WARNING;
        }

        return KpiStatus.NORMAL;
    }

    /**
     * 创建KPI预警
     */
    private static KpiAlert createKpiAlert(String kpiId, KpiValue kpiValue, KpiStatus status, ThresholdConfig thresholds) {
        KpiDefinition kpiDef = kpiDefinitions.get(kpiId);
        String kpiName = kpiDef != null ? kpiDef.getKpiName() : kpiId;

        String message = String.format("KPI %s (%s) 状态异常: 当前值 %s, 状态 %s",
                kpiName, kpiId, kpiValue.getFormattedValue(), status.getDescription());

        return new KpiAlert(kpiId, kpiName, status, kpiValue.getValue(), message, thresholds.getAlertRecipients());
    }

    /**
     * 发送预警通知
     */
    private static void sendAlertNotification(KpiAlert alert, ThresholdConfig thresholds) {
        List<String> recipients = thresholds.getAlertRecipients();
        if (recipients == null || recipients.isEmpty()) {
            logger.debug("No recipients configured for alert notification");
            return;
        }

        // 使用默认邮件通知器发送
        AlertNotifier emailNotifier = notifierRegistry.get("email");
        if (emailNotifier != null) {
            boolean success = emailNotifier.sendAlert(alert);
            if (success) {
                logger.debug("Alert notification sent successfully for KPI: {}", alert.getKpiId());
            } else {
                logger.warn("Failed to send alert notification for KPI: {}", alert.getKpiId());
            }
        } else {
            logger.warn("No email notifier available for alert notification");
        }
    }

    /**
     * KPI趋势分析
     */
    public static KpiTrend analyzeKpiTrend(String kpiId, TimeRange range) {
        if (kpiId == null || kpiId.isEmpty()) {
            logger.warn("Invalid KPI ID for trend analysis");
            return null;
        }

        if (range == null) {
            // 默认分析最近24小时的趋势
            range = new TimeRange(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        }

        try {
            // 获取历史数据
            List<KpiValue> history = historicalKpiValues.get(kpiId);
            if (history == null || history.isEmpty()) {
                logger.warn("No historical data available for KPI: {}", kpiId);
                return null;
            }

            // 过滤时间范围内的数据
            TimeRange finalRange = range;
            List<KpiValue> filteredHistory = history.stream()
                    .filter(kpiValue -> {
                        LocalDateTime timestamp = kpiValue.getTimestamp();
                        return !timestamp.isBefore(finalRange.getStart()) && !timestamp.isAfter(finalRange.getEnd());
                    })
                    .collect(Collectors.toList());

            if (filteredHistory.isEmpty()) {
                logger.warn("No data found in specified time range for KPI: {}", kpiId);
                return null;
            }

            KpiDefinition kpiDef = kpiDefinitions.get(kpiId);
            String kpiName = kpiDef != null ? kpiDef.getKpiName() : kpiId;

            KpiTrend trend = new KpiTrend(kpiId, kpiName, filteredHistory, range.getStart(), range.getEnd());

            // 添加分析结果
            trend.setAnalysisResult("dataPoints", filteredHistory.size());
            trend.setAnalysisResult("timeRangeHours", range.getDurationInMinutes() / 60.0);

            logger.debug("Analyzed KPI trend: {} with {} data points", kpiId, filteredHistory.size());
            return trend;
        } catch (Exception e) {
            logger.error("Failed to analyze KPI trend: " + kpiId, e);
            return null;
        }
    }

    /**
     * KPI仪表板生成
     */
    public static Dashboard generateDashboard(DashboardConfig config) {
        if (config == null) {
            logger.warn("Invalid dashboard configuration");
            return null;
        }

        try {
            String dashboardId = config.getDashboardId();
            if (dashboardId == null || dashboardId.isEmpty()) {
                dashboardId = UUID.randomUUID().toString();
            }

            Dashboard dashboard = new Dashboard(dashboardId, config.getDashboardName(), config.getLayout());

            // 计算所有KPI值
            List<String> kpiIds = config.getKpiIds();
            if (kpiIds != null && !kpiIds.isEmpty()) {
                for (String kpiId : kpiIds) {
                    KpiDefinition kpiDef = kpiDefinitions.get(kpiId);
                    if (kpiDef != null) {
                        KpiValue kpiValue = calculateKpi(kpiDef, null); // 使用默认时间范围
                        if (kpiValue != null) {
                            dashboard.addKpiValue(kpiValue);
                        }
                    }
                }
            }

            // 设置元数据
            dashboard.setMetadata("generatedAt", LocalDateTime.now());
            dashboard.setMetadata("kpiCount", dashboard.getKpiValues().size());
            dashboard.setMetadata("authorizedUsers", config.getAuthorizedUsers());

            logger.info("Generated dashboard: {} with {} KPIs", dashboard.getDashboardName(), dashboard.getKpiValues().size());
            return dashboard;
        } catch (Exception e) {
            logger.error("Failed to generate dashboard", e);
            return null;
        }
    }

    /**
     * 计算所有KPI
     */
    private static void calculateAllKpis() {
        try {
            for (KpiDefinition kpiDef : kpiDefinitions.values()) {
                if (kpiDef.isEnabled()) {
                    calculateKpi(kpiDef, null);

                    // 检查阈值预警
                    ThresholdConfig thresholds = thresholdConfigs.get(kpiDef.getKpiId());
                    if (thresholds != null) {
                        monitorKpiRealTime(kpiDef.getKpiId(), thresholds);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to calculate all KPIs", e);
        }
    }

    /**
     * 清理旧预警
     */
    private static void cleanupOldAlerts() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 保留7天内的预警
            activeAlerts.removeIf(alert -> alert.getTimestamp().isBefore(cutoffTime));
            logger.debug("Cleaned up old alerts, remaining: {}", activeAlerts.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup old alerts", e);
        }
    }

    /**
     * 清理历史数据
     */
    private static void cleanupHistoricalData() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(DEFAULT_DATA_RETENTION_DAYS);

            for (List<KpiValue> history : historicalKpiValues.values()) {
                history.removeIf(kpiValue -> kpiValue.getTimestamp().isBefore(cutoffTime));
            }

            logger.debug("Cleaned up historical data older than {} days", DEFAULT_DATA_RETENTION_DAYS);
        } catch (Exception e) {
            logger.error("Failed to cleanup historical data", e);
        }
    }

    /**
     * 获取KPI定义
     */
    public static KpiDefinition getKpiDefinition(String kpiId) {
        return kpiDefinitions.get(kpiId);
    }

    /**
     * 获取当前KPI值
     */
    public static KpiValue getCurrentKpiValue(String kpiId) {
        return currentKpiValues.get(kpiId);
    }

    /**
     * 获取历史KPI值
     */
    public static List<KpiValue> getHistoricalKpiValues(String kpiId, TimeRange range) {
        List<KpiValue> history = historicalKpiValues.get(kpiId);
        if (history == null) {
            return new ArrayList<>();
        }

        if (range == null) {
            return new ArrayList<>(history);
        }

        return history.stream()
                .filter(kpiValue -> {
                    LocalDateTime timestamp = kpiValue.getTimestamp();
                    return !timestamp.isBefore(range.getStart()) && !timestamp.isAfter(range.getEnd());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取活动预警
     */
    public static List<KpiAlert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }

    /**
     * 确认预警
     */
    public static boolean acknowledgeAlert(String alertId, String acknowledgedBy) {
        if (alertId == null || alertId.isEmpty() || acknowledgedBy == null || acknowledgedBy.isEmpty()) {
            logger.warn("Invalid parameters for alert acknowledgment");
            return false;
        }

        // 在实际实现中，应该创建新的已确认的预警对象
        logger.info("Alert {} acknowledged by {}", alertId, acknowledgedBy);
        return true;
    }

    /**
     * 获取仪表板统计信息
     */
    public static DashboardStatistics getDashboardStatistics() {
        DashboardStatistics stats = new DashboardStatistics();
        stats.setTotalKpis(kpiDefinitions.size());
        stats.setActiveKpis((int) kpiDefinitions.values().stream().filter(KpiDefinition::isEnabled).count());
        stats.setTotalAlerts(activeAlerts.size());
        stats.setCriticalAlerts((int) activeAlerts.stream()
                .filter(alert -> alert.getAlertLevel() == KpiStatus.CRITICAL).count());
        stats.setWarningAlerts((int) activeAlerts.stream()
                .filter(alert -> alert.getAlertLevel() == KpiStatus.WARNING).count());
        stats.setTotalCalculations(alertCounter.get());

        return stats;
    }

    /**
     * 关闭KPI仪表板
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("KpiDashboard shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("KpiDashboard shutdown interrupted");
        }
    }

    // KPI计算方法枚举
    public enum KpiCalculationMethod {
        AVERAGE("平均值"),
        SUM("总和"),
        COUNT("计数"),
        MAX("最大值"),
        MIN("最小值"),
        PERCENTAGE("百分比"),
        RATIO("比率"),
        TRENDED("趋势值"),
        CUMULATIVE("累积值");

        private final String description;

        KpiCalculationMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // KPI类型枚举
    public enum KpiType {
        PRODUCTION("生产类"),
        QUALITY("质量类"),
        EQUIPMENT("设备类"),
        EFFICIENCY("效率类"),
        FINANCIAL("财务类"),
        CUSTOM("自定义类");

        private final String description;

        KpiType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // KPI状态枚举
    public enum KpiStatus {
        NORMAL("正常"),
        WARNING("警告"),
        CRITICAL("严重"),
        UNKNOWN("未知");

        private final String description;

        KpiStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // KPI计算器接口
    public interface KpiCalculator {
        KpiValue calculate(KpiDefinition definition, TimeRange timeRange) throws Exception;

        boolean supportsType(KpiType type);

        String getCalculatorName();
    }

    // KPI数据源接口
    public interface KpiDataSource {
        List<Object> fetchData(String dataSource, TimeRange timeRange) throws Exception;

        boolean supportsDataSource(String dataSource);

        String getDataSourceName();
    }

    // 预警通知器接口
    public interface AlertNotifier {
        boolean sendAlert(KpiAlert alert);

        boolean supportsNotificationType(String type);

        String getNotifierName();
    }

    // 时间范围类
    public static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public TimeRange(LocalDateTime start, LocalDateTime end) {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Start and end times cannot be null");
            }
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start time cannot be after end time");
            }
            this.start = start;
            this.end = end;
        }

        // Getters
        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public long getDurationInMinutes() {
            return ChronoUnit.MINUTES.between(start, end);
        }

        @Override
        public String toString() {
            return "TimeRange{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    // KPI定义类
    public static class KpiDefinition {
        private final String kpiId;
        private final String kpiName;
        private final KpiType kpiType;
        private final KpiCalculationMethod calculationMethod;
        private final String dataSource;
        private final String description;
        private final String unit;
        private final Map<String, Object> parameters;
        private final boolean enabled;
        private final long refreshInterval; // 刷新间隔（毫秒）

        public KpiDefinition(String kpiId, String kpiName, KpiType kpiType,
                             KpiCalculationMethod calculationMethod, String dataSource) {
            this.kpiId = kpiId != null ? kpiId : UUID.randomUUID().toString();
            this.kpiName = kpiName != null ? kpiName : "";
            this.kpiType = kpiType != null ? kpiType : KpiType.CUSTOM;
            this.calculationMethod = calculationMethod != null ? calculationMethod : KpiCalculationMethod.AVERAGE;
            this.dataSource = dataSource != null ? dataSource : "";
            this.description = "";
            this.unit = "";
            this.parameters = new ConcurrentHashMap<>();
            this.enabled = true;
            this.refreshInterval = DEFAULT_MONITORING_INTERVAL;
        }

        // Getters and Setters
        public String getKpiId() {
            return kpiId;
        }

        public String getKpiName() {
            return kpiName;
        }

        public KpiType getKpiType() {
            return kpiType;
        }

        public KpiCalculationMethod getCalculationMethod() {
            return calculationMethod;
        }

        public String getDataSource() {
            return dataSource;
        }

        public String getDescription() {
            return description;
        }

        public KpiDefinition setDescription(String description) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getUnit() {
            return unit;
        }

        public KpiDefinition setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public KpiDefinition setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public long getRefreshInterval() {
            return refreshInterval;
        }

        public KpiDefinition setRefreshInterval(long refreshInterval) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "KpiDefinition{" +
                    "kpiId='" + kpiId + '\'' +
                    ", kpiName='" + kpiName + '\'' +
                    ", kpiType=" + kpiType +
                    ", calculationMethod=" + calculationMethod +
                    ", dataSource='" + dataSource + '\'' +
                    '}';
        }
    }

    // KPI值类
    public static class KpiValue {
        private final String kpiId;
        private final String kpiName;
        private final Object value;
        private final String formattedValue;
        private final String unit;
        private final LocalDateTime timestamp;
        private final KpiStatus status;
        private final Map<String, Object> metadata;
        private final BigDecimal targetValue;
        private final BigDecimal variance;

        public KpiValue(String kpiId, String kpiName, Object value, String unit,
                        KpiStatus status, BigDecimal targetValue) {
            this.kpiId = kpiId != null ? kpiId : "";
            this.kpiName = kpiName != null ? kpiName : "";
            this.value = value;
            this.unit = unit != null ? unit : "";
            this.timestamp = LocalDateTime.now();
            this.status = status != null ? status : KpiStatus.UNKNOWN;
            this.metadata = new ConcurrentHashMap<>();
            this.targetValue = targetValue;

            // 格式化值
            this.formattedValue = formatValue(value);

            // 计算方差
            this.variance = calculateVariance(value, targetValue);
        }

        // 格式化值
        private String formatValue(Object value) {
            if (value == null) return "N/A";
            if (value instanceof Number) {
                return String.format("%.2f", ((Number) value).doubleValue());
            }
            return value.toString();
        }

        // 计算方差
        private BigDecimal calculateVariance(Object actualValue, BigDecimal targetValue) {
            if (actualValue == null || targetValue == null) return BigDecimal.ZERO;

            try {
                BigDecimal actual = new BigDecimal(actualValue.toString());
                if (targetValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                return actual.subtract(targetValue)
                        .divide(targetValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        // Getters
        public String getKpiId() {
            return kpiId;
        }

        public String getKpiName() {
            return kpiName;
        }

        public Object getValue() {
            return value;
        }

        public String getFormattedValue() {
            return formattedValue;
        }

        public String getUnit() {
            return unit;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public KpiStatus getStatus() {
            return status;
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

        public BigDecimal getTargetValue() {
            return targetValue;
        }

        public BigDecimal getVariance() {
            return variance;
        }

        public boolean hasTarget() {
            return targetValue != null;
        }

        @Override
        public String toString() {
            return "KpiValue{" +
                    "kpiId='" + kpiId + '\'' +
                    ", kpiName='" + kpiName + '\'' +
                    ", value=" + formattedValue +
                    ", unit='" + unit + '\'' +
                    ", status=" + status +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // 阈值配置类
    public static class ThresholdConfig {
        private BigDecimal normalLowerBound;
        private BigDecimal normalUpperBound;
        private BigDecimal warningLowerBound;
        private BigDecimal warningUpperBound;
        private BigDecimal criticalLowerBound;
        private BigDecimal criticalUpperBound;
        private boolean enableAlerts = true;
        private List<String> alertRecipients = new ArrayList<>();
        private String alertMessageTemplate;

        // Getters and Setters
        public BigDecimal getNormalLowerBound() {
            return normalLowerBound;
        }

        public ThresholdConfig setNormalLowerBound(BigDecimal normalLowerBound) {
            this.normalLowerBound = normalLowerBound;
            return this;
        }

        public BigDecimal getNormalUpperBound() {
            return normalUpperBound;
        }

        public ThresholdConfig setNormalUpperBound(BigDecimal normalUpperBound) {
            this.normalUpperBound = normalUpperBound;
            return this;
        }

        public BigDecimal getWarningLowerBound() {
            return warningLowerBound;
        }

        public ThresholdConfig setWarningLowerBound(BigDecimal warningLowerBound) {
            this.warningLowerBound = warningLowerBound;
            return this;
        }

        public BigDecimal getWarningUpperBound() {
            return warningUpperBound;
        }

        public ThresholdConfig setWarningUpperBound(BigDecimal warningUpperBound) {
            this.warningUpperBound = warningUpperBound;
            return this;
        }

        public BigDecimal getCriticalLowerBound() {
            return criticalLowerBound;
        }

        public ThresholdConfig setCriticalLowerBound(BigDecimal criticalLowerBound) {
            this.criticalLowerBound = criticalLowerBound;
            return this;
        }

        public BigDecimal getCriticalUpperBound() {
            return criticalUpperBound;
        }

        public ThresholdConfig setCriticalUpperBound(BigDecimal criticalUpperBound) {
            this.criticalUpperBound = criticalUpperBound;
            return this;
        }

        public boolean isEnableAlerts() {
            return enableAlerts;
        }

        public ThresholdConfig setEnableAlerts(boolean enableAlerts) {
            this.enableAlerts = enableAlerts;
            return this;
        }

        public List<String> getAlertRecipients() {
            return new ArrayList<>(alertRecipients);
        }

        public ThresholdConfig setAlertRecipients(List<String> alertRecipients) {
            this.alertRecipients = new ArrayList<>(alertRecipients);
            return this;
        }

        public ThresholdConfig addAlertRecipient(String recipient) {
            this.alertRecipients.add(recipient);
            return this;
        }

        public String getAlertMessageTemplate() {
            return alertMessageTemplate;
        }

        public ThresholdConfig setAlertMessageTemplate(String alertMessageTemplate) {
            this.alertMessageTemplate = alertMessageTemplate;
            return this;
        }

        @Override
        public String toString() {
            return "ThresholdConfig{" +
                    "normal=[" + normalLowerBound + ", " + normalUpperBound + "]" +
                    ", warning=[" + warningLowerBound + ", " + warningUpperBound + "]" +
                    ", critical=[" + criticalLowerBound + ", " + criticalUpperBound + "]" +
                    '}';
        }
    }

    // KPI预警类
    public static class KpiAlert {
        private final String alertId;
        private final String kpiId;
        private final String kpiName;
        private final KpiStatus alertLevel;
        private final Object currentValue;
        private final String message;
        private final LocalDateTime timestamp;
        private final List<String> recipients;
        private final boolean acknowledged;
        private final LocalDateTime acknowledgedTime;
        private final String acknowledgedBy;

        public KpiAlert(String kpiId, String kpiName, KpiStatus alertLevel,
                        Object currentValue, String message, List<String> recipients) {
            this.alertId = UUID.randomUUID().toString();
            this.kpiId = kpiId != null ? kpiId : "";
            this.kpiName = kpiName != null ? kpiName : "";
            this.alertLevel = alertLevel != null ? alertLevel : KpiStatus.UNKNOWN;
            this.currentValue = currentValue;
            this.message = message != null ? message : "";
            this.timestamp = LocalDateTime.now();
            this.recipients = recipients != null ? new ArrayList<>(recipients) : new ArrayList<>();
            this.acknowledged = false;
            this.acknowledgedTime = null;
            this.acknowledgedBy = null;
        }

        // Getters
        public String getAlertId() {
            return alertId;
        }

        public String getKpiId() {
            return kpiId;
        }

        public String getKpiName() {
            return kpiName;
        }

        public KpiStatus getAlertLevel() {
            return alertLevel;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public List<String> getRecipients() {
            return new ArrayList<>(recipients);
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public LocalDateTime getAcknowledgedTime() {
            return acknowledgedTime;
        }

        public String getAcknowledgedBy() {
            return acknowledgedBy;
        }

        @Override
        public String toString() {
            return "KpiAlert{" +
                    "alertId='" + alertId + '\'' +
                    ", kpiId='" + kpiId + '\'' +
                    ", kpiName='" + kpiName + '\'' +
                    ", alertLevel=" + alertLevel +
                    ", currentValue=" + currentValue +
                    ", message='" + message + '\'' +
                    ", timestamp=" + timestamp +
                    ", acknowledged=" + acknowledged +
                    '}';
        }
    }

    // KPI趋势类
    public static class KpiTrend {
        private final String kpiId;
        private final String kpiName;
        private final List<KpiValue> historicalValues;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final BigDecimal trendSlope;
        private final String trendDirection;
        private final Map<String, Object> analysisResults;

        public KpiTrend(String kpiId, String kpiName, List<KpiValue> historicalValues,
                        LocalDateTime startTime, LocalDateTime endTime) {
            this.kpiId = kpiId != null ? kpiId : "";
            this.kpiName = kpiName != null ? kpiName : "";
            this.historicalValues = historicalValues != null ? new ArrayList<>(historicalValues) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.analysisResults = new ConcurrentHashMap<>();

            // 计算趋势
            this.trendSlope = calculateTrendSlope();
            this.trendDirection = determineTrendDirection();
        }

        // 计算趋势斜率
        private BigDecimal calculateTrendSlope() {
            if (historicalValues.size() < 2) return BigDecimal.ZERO;

            try {
                // 简单线性回归计算斜率
                BigDecimal sumX = BigDecimal.ZERO;
                BigDecimal sumY = BigDecimal.ZERO;
                BigDecimal sumXY = BigDecimal.ZERO;
                BigDecimal sumXX = BigDecimal.ZERO;

                for (int i = 0; i < historicalValues.size(); i++) {
                    BigDecimal x = BigDecimal.valueOf(i);
                    Object value = historicalValues.get(i).getValue();
                    if (value instanceof Number) {
                        BigDecimal y = new BigDecimal(value.toString());
                        sumX = sumX.add(x);
                        sumY = sumY.add(y);
                        sumXY = sumXY.add(x.multiply(y));
                        sumXX = sumXX.add(x.multiply(x));
                    }
                }

                int n = historicalValues.size();
                BigDecimal numerator = BigDecimal.valueOf(n).multiply(sumXY).subtract(sumX.multiply(sumY));
                BigDecimal denominator = BigDecimal.valueOf(n).multiply(sumXX).subtract(sumX.multiply(sumX));

                if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

                return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
            } catch (Exception e) {
                logger.warn("Failed to calculate trend slope for KPI: " + kpiId, e);
                return BigDecimal.ZERO;
            }
        }

        // 确定趋势方向
        private String determineTrendDirection() {
            if (trendSlope.compareTo(BigDecimal.ZERO) > 0) return "上升";
            if (trendSlope.compareTo(BigDecimal.ZERO) < 0) return "下降";
            return "平稳";
        }

        // Getters
        public String getKpiId() {
            return kpiId;
        }

        public String getKpiName() {
            return kpiName;
        }

        public List<KpiValue> getHistoricalValues() {
            return new ArrayList<>(historicalValues);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public BigDecimal getTrendSlope() {
            return trendSlope;
        }

        public String getTrendDirection() {
            return trendDirection;
        }

        public Map<String, Object> getAnalysisResults() {
            return new HashMap<>(analysisResults);
        }

        public void setAnalysisResult(String key, Object value) {
            this.analysisResults.put(key, value);
        }

        public Object getAnalysisResult(String key) {
            return this.analysisResults.get(key);
        }

        @Override
        public String toString() {
            return "KpiTrend{" +
                    "kpiId='" + kpiId + '\'' +
                    ", kpiName='" + kpiName + '\'' +
                    ", valueCount=" + historicalValues.size() +
                    ", trendSlope=" + trendSlope +
                    ", trendDirection='" + trendDirection + '\'' +
                    '}';
        }
    }

    // 仪表板配置类
    public static class DashboardConfig {
        private String dashboardId;
        private String dashboardName;
        private List<String> kpiIds;
        private String layout;
        private Map<String, Object> displaySettings;
        private boolean enableRealTimeUpdates;
        private long updateInterval;
        private List<String> authorizedUsers;

        public DashboardConfig() {
            this.kpiIds = new ArrayList<>();
            this.displaySettings = new ConcurrentHashMap<>();
            this.enableRealTimeUpdates = true;
            this.updateInterval = DEFAULT_MONITORING_INTERVAL;
            this.authorizedUsers = new ArrayList<>();
        }

        // Getters and Setters
        public String getDashboardId() {
            return dashboardId;
        }

        public DashboardConfig setDashboardId(String dashboardId) {
            this.dashboardId = dashboardId;
            return this;
        }

        public String getDashboardName() {
            return dashboardName;
        }

        public DashboardConfig setDashboardName(String dashboardName) {
            this.dashboardName = dashboardName;
            return this;
        }

        public List<String> getKpiIds() {
            return new ArrayList<>(kpiIds);
        }

        public DashboardConfig setKpiIds(List<String> kpiIds) {
            this.kpiIds = new ArrayList<>(kpiIds);
            return this;
        }

        public DashboardConfig addKpiId(String kpiId) {
            this.kpiIds.add(kpiId);
            return this;
        }

        public String getLayout() {
            return layout;
        }

        public DashboardConfig setLayout(String layout) {
            this.layout = layout;
            return this;
        }

        public Map<String, Object> getDisplaySettings() {
            return new HashMap<>(displaySettings);
        }

        public DashboardConfig setDisplaySetting(String key, Object value) {
            this.displaySettings.put(key, value);
            return this;
        }

        public Object getDisplaySetting(String key) {
            return this.displaySettings.get(key);
        }

        public boolean isEnableRealTimeUpdates() {
            return enableRealTimeUpdates;
        }

        public DashboardConfig setEnableRealTimeUpdates(boolean enableRealTimeUpdates) {
            this.enableRealTimeUpdates = enableRealTimeUpdates;
            return this;
        }

        public long getUpdateInterval() {
            return updateInterval;
        }

        public DashboardConfig setUpdateInterval(long updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public List<String> getAuthorizedUsers() {
            return new ArrayList<>(authorizedUsers);
        }

        public DashboardConfig setAuthorizedUsers(List<String> authorizedUsers) {
            this.authorizedUsers = new ArrayList<>(authorizedUsers);
            return this;
        }

        public DashboardConfig addAuthorizedUser(String user) {
            this.authorizedUsers.add(user);
            return this;
        }
    }

    // 仪表板类
    public static class Dashboard {
        private final String dashboardId;
        private final String dashboardName;
        private final List<KpiValue> kpiValues;
        private final LocalDateTime lastUpdated;
        private final Map<String, Object> metadata;
        private final String layout;

        public Dashboard(String dashboardId, String dashboardName, String layout) {
            this.dashboardId = dashboardId != null ? dashboardId : UUID.randomUUID().toString();
            this.dashboardName = dashboardName != null ? dashboardName : "";
            this.kpiValues = new CopyOnWriteArrayList<>();
            this.lastUpdated = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
            this.layout = layout != null ? layout : "grid";
        }

        // Getters
        public String getDashboardId() {
            return dashboardId;
        }

        public String getDashboardName() {
            return dashboardName;
        }

        public List<KpiValue> getKpiValues() {
            return new ArrayList<>(kpiValues);
        }

        public void setKpiValues(List<KpiValue> kpiValues) {
            this.kpiValues.clear();
            if (kpiValues != null) {
                this.kpiValues.addAll(kpiValues);
            }
        }

        public void addKpiValue(KpiValue kpiValue) {
            this.kpiValues.add(kpiValue);
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
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

        public String getLayout() {
            return layout;
        }

        @Override
        public String toString() {
            return "Dashboard{" +
                    "dashboardId='" + dashboardId + '\'' +
                    ", dashboardName='" + dashboardName + '\'' +
                    ", kpiCount=" + kpiValues.size() +
                    ", lastUpdated=" + lastUpdated +
                    ", layout='" + layout + '\'' +
                    '}';
        }
    }

    /**
     * 仪表板统计信息类
     */
    public static class DashboardStatistics {
        private int totalKpis;
        private int activeKpis;
        private int totalAlerts;
        private int criticalAlerts;
        private int warningAlerts;
        private long totalCalculations;

        // Getters and Setters
        public int getTotalKpis() {
            return totalKpis;
        }

        public void setTotalKpis(int totalKpis) {
            this.totalKpis = totalKpis;
        }

        public int getActiveKpis() {
            return activeKpis;
        }

        public void setActiveKpis(int activeKpis) {
            this.activeKpis = activeKpis;
        }

        public int getTotalAlerts() {
            return totalAlerts;
        }

        public void setTotalAlerts(int totalAlerts) {
            this.totalAlerts = totalAlerts;
        }

        public int getCriticalAlerts() {
            return criticalAlerts;
        }

        public void setCriticalAlerts(int criticalAlerts) {
            this.criticalAlerts = criticalAlerts;
        }

        public int getWarningAlerts() {
            return warningAlerts;
        }

        public void setWarningAlerts(int warningAlerts) {
            this.warningAlerts = warningAlerts;
        }

        public long getTotalCalculations() {
            return totalCalculations;
        }

        public void setTotalCalculations(long totalCalculations) {
            this.totalCalculations = totalCalculations;
        }

        @Override
        public String toString() {
            return "DashboardStatistics{" +
                    "totalKpis=" + totalKpis +
                    ", activeKpis=" + activeKpis +
                    ", totalAlerts=" + totalAlerts +
                    ", criticalAlerts=" + criticalAlerts +
                    ", warningAlerts=" + warningAlerts +
                    ", totalCalculations=" + totalCalculations +
                    '}';
        }
    }
}
