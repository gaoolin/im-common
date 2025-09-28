package com.qtech.im.semiconductor.quality.spc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计过程控制工具类
 * <p>
 * 特性：
 * - 通用性：支持多种SPC控制图类型和质量分析方法
 * - 规范性：遵循SPC标准和统计学原理
 * - 专业性：半导体行业质量控制专业算法实现
 * - 灵活性：可配置的控制限和分析参数
 * - 可靠性：完善的异常检测和预警机制
 * - 安全性：数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/21
 */
public class SpcTool {

    // 默认配置
    public static final int DEFAULT_SAMPLE_SIZE = 5;
    public static final int DEFAULT_MOVING_RANGE_LENGTH = 2;
    public static final double DEFAULT_CONFIDENCE_LEVEL = 0.9973; // 3σ水平
    public static final int DEFAULT_TREND_PERIODS = 20;
    public static final long DEFAULT_MONITORING_INTERVAL = 30000; // 30秒
    private static final Logger logger = LoggerFactory.getLogger(SpcTool.class);
    // 内部存储和管理
    private static final Map<ChartType, SpcRuleChecker> ruleCheckerRegistry = new ConcurrentHashMap<>();
    private static final Map<DataType, QualityDataFilter> dataFilterRegistry = new ConcurrentHashMap<>();
    private static final Map<String, TrendPredictor> trendPredictorRegistry = new ConcurrentHashMap<>();
    private static final List<QualityAlert> activeAlerts = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    // 初始化默认组件
    static {
        registerDefaultComponents();
        startMonitoringTasks();
        logger.info("SpcTool initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认规则检查器、数据过滤器和趋势预测器
        // registerRuleChecker(new DefaultSpcRuleChecker());
        // registerDataFilter(new DefaultQualityDataFilter());
        // registerTrendPredictor(new DefaultTrendPredictor());
    }

    /**
     * 启动监控任务
     */
    private static void startMonitoringTasks() {
        // 启动预警清理任务
        scheduler.scheduleAtFixedRate(SpcTool::cleanupOldAlerts,
                30, 30, TimeUnit.MINUTES);

        logger.debug("SPC monitoring tasks started");
    }

    /**
     * 注册SPC规则检查器
     */
    public static void registerRuleChecker(SpcRuleChecker checker) {
        if (checker == null) {
            throw new IllegalArgumentException("Rule checker cannot be null");
        }

        for (ChartType type : ChartType.values()) {
            if (checker.supportsChartType(type)) {
                ruleCheckerRegistry.put(type, checker);
                logger.debug("Registered rule checker {} for chart type {}", checker.getCheckerName(), type);
            }
        }
    }

    /**
     * 注册质量数据过滤器
     */
    public static void registerDataFilter(QualityDataFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Data filter cannot be null");
        }

        for (DataType type : DataType.values()) {
            if (filter.supportsDataType(type)) {
                dataFilterRegistry.put(type, filter);
                logger.debug("Registered data filter {} for data type {}", filter.getFilterName(), type);
            }
        }
    }

    /**
     * 注册趋势预测器
     */
    public static void registerTrendPredictor(TrendPredictor predictor) {
        if (predictor == null) {
            throw new IllegalArgumentException("Trend predictor cannot be null");
        }

        // 注册支持的所有预测类型
        String[] predictionTypes = {"linear", "polynomial", "exponential"}; // 简化实现
        for (String type : predictionTypes) {
            if (predictor.supportsPredictionType(type)) {
                trendPredictorRegistry.put(type, predictor);
                logger.debug("Registered trend predictor {} for type {}", predictor.getPredictorName(), type);
            }
        }
    }

    /**
     * SPC控制图生成
     */
    public static ControlChart generateControlChart(List<Measurement> data, ChartType type) {
        if (data == null || data.isEmpty()) {
            logger.warn("Invalid data for control chart generation");
            return null;
        }

        if (type == null) {
            type = ChartType.X_BAR_R; // 默认类型
        }

        try {
            String chartName = "SPC Control Chart - " + type.getChineseName();
            ControlChart chart = new ControlChart(type, chartName);

            // 计算控制限
            ControlLimits limits = calculateControlLimits(data, type);
            chart.getControlLimits().setUpperControlLimit(limits.getUpperControlLimit());
            chart.getControlLimits().setLowerControlLimit(limits.getLowerControlLimit());
            chart.getControlLimits().setCenterLine(limits.getCenterLine());
            chart.getControlLimits().setUpperWarningLimit(limits.getUpperWarningLimit());
            chart.getControlLimits().setLowerWarningLimit(limits.getLowerWarningLimit());

            // 添加数据点
            for (Measurement measurement : data) {
                if (measurement.isValid()) {
                    ControlLimitStatus status = evaluateControlLimitStatus(measurement.getValue(), limits);
                    ChartDataPoint dataPoint = new ChartDataPoint(
                            measurement.getValue(), measurement.getTimestamp(), status);
                    chart.addDataPoint(dataPoint);
                }
            }

            // 检查控制规则违规
            SpcRuleChecker ruleChecker = ruleCheckerRegistry.get(type);
            if (ruleChecker != null) {
                List<ControlRuleViolation> violations = ruleChecker.checkViolations(chart, data);
                violations.forEach(chart::addViolation);
            }

            chart.setMetadata("dataPointCount", data.size());
            chart.setMetadata("validDataPointCount", data.stream().filter(Measurement::isValid).count());
            chart.setMetadata("generationTime", LocalDateTime.now());

            logger.debug("Generated control chart: {} with {} data points", chartName, data.size());
            return chart;
        } catch (Exception e) {
            logger.error("Failed to generate control chart", e);
            return null;
        }
    }

    /**
     * 计算控制限
     */
    private static ControlLimits calculateControlLimits(List<Measurement> data, ChartType type) {
        ControlLimits limits = new ControlLimits();

        // 过滤有效数据
        List<Double> validValues = data.stream()
                .filter(Measurement::isValid)
                .map(Measurement::getValue)
                .collect(Collectors.toList());

        if (validValues.isEmpty()) {
            return limits;
        }

        // 计算基本统计量
        double mean = calculateMean(validValues);
        double stdDev = calculateStandardDeviation(validValues, mean);

        // 根据图表类型计算控制限
        switch (type) {
            case X_BAR_R:
            case X_BAR_S:
                // 均值图控制限
                limits.setCenterLine(mean);
                limits.setUpperControlLimit(mean + 3 * stdDev);
                limits.setLowerControlLimit(mean - 3 * stdDev);
                limits.setUpperWarningLimit(mean + 2 * stdDev);
                limits.setLowerWarningLimit(mean - 2 * stdDev);
                break;

            case INDIVIDUALS:
                // 单值图控制限
                limits.setCenterLine(mean);
                limits.setUpperControlLimit(mean + 2 * stdDev); // 选择合适的标准差
                limits.setLowerControlLimit(mean - 2 * stdDev);
                limits.setUpperWarningLimit(mean + 1 * stdDev);
                limits.setLowerWarningLimit(mean - 1 * stdDev);
                break;

            case P_CHART:
                break;
            case NP_CHART:
                break;
            case C_CHART:
                break;
            case U_CHART:
                break;
            case EWMA:
                break;
            case CUSUM:
                break;
            default:
                // 默认控制限计算
                limits.setCenterLine(mean);
                limits.setUpperControlLimit(mean + 3 * stdDev);
                limits.setLowerControlLimit(mean - 3 * stdDev);
                break;
        }

        limits.setParameter("sampleCount", validValues.size());
        limits.setParameter("calculatedStdDev", stdDev);
        limits.setParameter("calculationTime", LocalDateTime.now());

        return limits;
    }

    /**
     * 计算均值
     */
    private static double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 计算标准差
     */
    private static double calculateStandardDeviation(List<Double> values, double mean) {
        if (values.size() <= 1) {
            return 0.0;
        }

        double sumSquaredDiffs = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum();

        return Math.sqrt(sumSquaredDiffs / (values.size() - 1));
    }

    /**
     * 评估控制限状态
     */
    private static ControlLimitStatus evaluateControlLimitStatus(double value, ControlLimits limits) {
        Double ucl = limits.getUpperControlLimit();
        Double lcl = limits.getLowerControlLimit();
        Double uwl = limits.getUpperWarningLimit();
        Double lwl = limits.getLowerWarningLimit();

        if (ucl != null && value > ucl) return ControlLimitStatus.OUT_OF_CONTROL;
        if (lcl != null && value < lcl) return ControlLimitStatus.OUT_OF_CONTROL;
        if (uwl != null && value > uwl) return ControlLimitStatus.TREND;
        if (lwl != null && value < lwl) return ControlLimitStatus.TREND;

        return ControlLimitStatus.IN_CONTROL;
    }

    /**
     * CPK计算
     */
    public static ProcessCapability calculateCPK(List<Measurement> data, SpecificationLimits limits) {
        if (data == null || data.isEmpty()) {
            logger.warn("Invalid data for CPK calculation");
            return null;
        }

        if (limits == null) {
            logger.warn("Invalid specification limits for CPK calculation");
            return null;
        }

        try {
            // 过滤有效数据
            List<Double> validValues = data.stream()
                    .filter(Measurement::isValid)
                    .map(Measurement::getValue)
                    .collect(Collectors.toList());

            if (validValues.isEmpty()) {
                logger.warn("No valid data for CPK calculation");
                return null;
            }

            // 计算统计量
            double mean = calculateMean(validValues);
            double stdDev = calculateStandardDeviation(validValues, mean);

            if (stdDev == 0) {
                logger.warn("Standard deviation is zero, cannot calculate CPK");
                return null;
            }

            // 计算CP, CPK, PP, PPK
            Double usl = limits.getUpperSpecificationLimit();
            Double lsl = limits.getLowerSpecificationLimit();

            double cp = 0.0, cpk = 0.0, pp = 0.0, ppk = 0.0;

            if (usl != null && lsl != null) {
                // 双边规格限
                double tolerance = usl - lsl;
                cp = tolerance / (6 * stdDev);

                double cpu = (usl - mean) / (3 * stdDev);
                double cpl = (mean - lsl) / (3 * stdDev);
                cpk = Math.min(cpu, cpl);

                // 长期能力指数
                pp = tolerance / (6 * stdDev);
                ppk = cpk; // 简化实现
            } else if (usl != null) {
                // 单边上限
                cp = (usl - mean) / (3 * stdDev);
                cpk = cp;
                pp = cp;
                ppk = cpk;
            } else if (lsl != null) {
                // 单边下限
                cp = (mean - lsl) / (3 * stdDev);
                cpk = cp;
                pp = cp;
                ppk = cpk;
            }

            ProcessCapability capability = new ProcessCapability(cp, cpk, pp, ppk, mean, stdDev, limits);

            // 添加元数据
            capability.setMetadata("dataPointCount", validValues.size());
            capability.setMetadata("calculationMethod", "Sample Standard Deviation");
            capability.setMetadata("confidenceLevel", DEFAULT_CONFIDENCE_LEVEL);

            logger.debug("Calculated process capability: Cp={:.2f}, Cpk={:.2f}", cp, cpk);
            return capability;
        } catch (Exception e) {
            logger.error("Failed to calculate CPK", e);
            return null;
        }
    }

    /**
     * 质量异常实时检测
     */
    public static List<QualityAlert> detectRealTimeAnomalies(Stream<Measurement> dataStream) {
        if (dataStream == null) {
            logger.warn("Invalid data stream for real-time anomaly detection");
            return new ArrayList<>();
        }

        try {
            List<QualityAlert> alerts = new ArrayList<>();

            // 收集最近的数据点进行分析
            List<Measurement> recentData = dataStream
                    .limit(100) // 限制分析的数据量
                    .collect(Collectors.toList());

            if (recentData.isEmpty()) {
                return alerts;
            }

            // 检测各种异常模式
            detectOutOfRangeAnomalies(recentData, alerts);
            detectTrendAnomalies(recentData, alerts);
            detectShiftAnomalies(recentData, alerts);

            // 添加到活动预警列表
            alerts.forEach(alert -> {
                if (activeAlerts.size() < 1000) { // 限制预警数量
                    activeAlerts.add(alert);
                }
            });

            logger.debug("Detected {} real-time quality anomalies", alerts.size());
            return alerts;
        } catch (Exception e) {
            logger.error("Failed to detect real-time anomalies", e);
            return new ArrayList<>();
        }
    }

    /**
     * 检测超出范围的异常
     */
    private static void detectOutOfRangeAnomalies(List<Measurement> data, List<QualityAlert> alerts) {
        // 简化实现：检测明显异常值
        List<Measurement> outliers = data.stream()
                .filter(Measurement::isValid)
                .filter(measurement -> Math.abs(measurement.getValue()) > 1000) // 简单阈值
                .collect(Collectors.toList());

        if (!outliers.isEmpty()) {
            QualityAlert alert = new QualityAlert(
                    "实时数据",
                    QualityAlertLevel.CRITICAL,
                    "检测到 " + outliers.size() + " 个异常值",
                    outliers,
                    "值超出正常范围"
            );
            alerts.add(alert);
        }
    }

    /**
     * 检测趋势异常
     */
    private static void detectTrendAnomalies(List<Measurement> data, List<QualityAlert> alerts) {
        if (data.size() < 5) return;

        // 检查连续上升或下降趋势
        int consecutiveIncreases = 0;
        int consecutiveDecreases = 0;

        for (int i = 1; i < Math.min(10, data.size()); i++) {
            double current = data.get(i).getValue();
            double previous = data.get(i - 1).getValue();

            if (current > previous) {
                consecutiveIncreases++;
                consecutiveDecreases = 0;
            } else if (current < previous) {
                consecutiveDecreases++;
                consecutiveIncreases = 0;
            } else {
                consecutiveIncreases = 0;
                consecutiveDecreases = 0;
            }

            // 如果连续7点同向，触发预警
            if (consecutiveIncreases >= 7 || consecutiveDecreases >= 7) {
                List<Measurement> trendData = data.subList(Math.max(0, i - 7), i + 1);
                QualityAlert alert = new QualityAlert(
                        "实时数据",
                        QualityAlertLevel.WARNING,
                        "检测到连续 " + Math.max(consecutiveIncreases, consecutiveDecreases) + " 点同向趋势",
                        trendData,
                        "连续点同向趋势"
                );
                alerts.add(alert);
                break;
            }
        }
    }

    /**
     * 检测偏移异常
     */
    private static void detectShiftAnomalies(List<Measurement> data, List<QualityAlert> alerts) {
        if (data.size() < 8) return;

        // 计算基准均值
        int baselineSize = Math.min(8, data.size() - 4);
        List<Measurement> baselineData = data.subList(0, baselineSize);
        double baselineMean = baselineData.stream()
                .filter(Measurement::isValid)
                .mapToDouble(Measurement::getValue)
                .average()
                .orElse(0.0);

        // 检查后续数据是否偏离基准
        List<Measurement> recentData = data.subList(baselineSize, data.size());
        long aboveBaseline = recentData.stream()
                .filter(Measurement::isValid)
                .mapToDouble(Measurement::getValue)
                .filter(value -> value > baselineMean)
                .count();

        long belowBaseline = recentData.stream()
                .filter(Measurement::isValid)
                .mapToDouble(Measurement::getValue)
                .filter(value -> value < baselineMean)
                .count();

        // 如果连续8点都在均值同一侧，触发预警
        if (aboveBaseline >= 8 || belowBaseline >= 8) {
            QualityAlert alert = new QualityAlert(
                    "实时数据",
                    QualityAlertLevel.WARNING,
                    "检测到数据偏移: 连续 " + recentData.size() + " 点在均值同一侧",
                    recentData,
                    "数据偏移"
            );
            alerts.add(alert);
        }
    }

    /**
     * 质量趋势预测
     */
    public static QualityTrend predictQualityTrend(HistoricalData historicalData) {
        if (historicalData == null) {
            logger.warn("Invalid historical data for trend prediction");
            return null;
        }

        try {
            List<Measurement> measurements = historicalData.getMeasurements();
            if (measurements.isEmpty()) {
                logger.warn("No measurements inspection historical data");
                return null;
            }

            String productName = historicalData.getProductId();
            LocalDateTime startTime = historicalData.getStartTime();
            LocalDateTime endTime = historicalData.getEndTime();

            QualityTrend trend = new QualityTrend(productName, measurements, startTime, endTime);

            logger.debug("Predicted quality trend for product: {}", productName);
            return trend;
        } catch (Exception e) {
            logger.error("Failed to predict quality trend", e);
            return null;
        }
    }

    /**
     * 清理旧预警
     */
    private static void cleanupOldAlerts() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 保留7天内的预警
            activeAlerts.removeIf(alert -> alert.getTimestamp().isBefore(cutoffTime));
            logger.debug("Cleaned up old quality alerts, remaining: {}", activeAlerts.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup old alerts", e);
        }
    }

    /**
     * 获取活动预警
     */
    public static List<QualityAlert> getActiveAlerts() {
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

        for (QualityAlert alert : activeAlerts) {
            if (alertId.equals(alert.getAlertId())) {
                alert.setAcknowledged(true);
                alert.setAcknowledgedTime(LocalDateTime.now());
                alert.setAcknowledgedBy(acknowledgedBy);
                logger.info("Alert {} acknowledged by {}", alertId, acknowledgedBy);
                return true;
            }
        }

        logger.warn("Alert not found: {}", alertId);
        return false;
    }

    /**
     * 过滤质量数据
     */
    public static List<Measurement> filterQualityData(List<Measurement> rawData,
                                                      Predicate<Measurement> filter,
                                                      DataType dataType) {
        if (rawData == null || rawData.isEmpty()) {
            return new ArrayList<>();
        }

        if (filter == null) {
            filter = Measurement::isValid; // 默认过滤器
        }

        if (dataType == null) {
            dataType = DataType.CONTINUOUS; // 默认数据类型
        }

        try {
            QualityDataFilter dataFilter = dataFilterRegistry.get(dataType);
            if (dataFilter != null) {
                return dataFilter.filterData(rawData, filter);
            } else {
                // 使用默认过滤
                return rawData.stream().filter(filter).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Failed to filter quality data", e);
            return new ArrayList<>();
        }
    }

    /**
     * 应用SPC规则
     */
    public static List<ControlRuleViolation> applySpcRules(ControlChart chart,
                                                           List<Measurement> data,
                                                           ChartType chartType) {
        if (chart == null || data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (chartType == null) {
            chartType = ChartType.X_BAR_R; // 默认类型
        }

        try {
            SpcRuleChecker ruleChecker = ruleCheckerRegistry.get(chartType);
            if (ruleChecker != null) {
                return ruleChecker.checkViolations(chart, data);
            } else {
                // 默认规则检查
                return checkDefaultRules(chart, data);
            }
        } catch (Exception e) {
            logger.error("Failed to apply SPC rules", e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查默认规则
     */
    private static List<ControlRuleViolation> checkDefaultRules(ControlChart chart, List<Measurement> data) {
        List<ControlRuleViolation> violations = new ArrayList<>();

        // 规则1: 1个点超出控制限
        checkRule1(chart, data, violations);

        // 规则2: 连续9点在中心线同一侧
        checkRule2(chart, data, violations);

        // 规则3: 连续6点持续递增或递减
        checkRule3(chart, data, violations);

        return violations;
    }

    /**
     * 检查规则1: 1个点超出控制限
     */
    private static void checkRule1(ControlChart chart, List<Measurement> data, List<ControlRuleViolation> violations) {
        ControlLimits limits = chart.getControlLimits();
        Double ucl = limits.getUpperControlLimit();
        Double lcl = limits.getLowerControlLimit();

        if (ucl == null || lcl == null) return;

        List<ChartDataPoint> violatingPoints = new ArrayList<>();
        for (ChartDataPoint point : chart.getDataPoints()) {
            if (point.getValue() > ucl || point.getValue() < lcl) {
                violatingPoints.add(point);
            }
        }

        if (!violatingPoints.isEmpty()) {
            violations.add(new ControlRuleViolation(
                    1,
                    "1个点超出控制限",
                    violatingPoints,
                    QualityAlertLevel.OUT_OF_CONTROL
            ));
        }
    }

    /**
     * 检查规则2: 连续9点在中心线同一侧
     */
    private static void checkRule2(ControlChart chart, List<Measurement> data, List<ControlRuleViolation> violations) {
        ControlLimits limits = chart.getControlLimits();
        Double centerLine = limits.getCenterLine();

        if (centerLine == null) return;

        List<ChartDataPoint> points = chart.getDataPoints();
        if (points.size() < 9) return;

        for (int i = 0; i <= points.size() - 9; i++) {
            List<ChartDataPoint> subPoints = points.subList(i, i + 9);
            boolean allAbove = subPoints.stream().allMatch(point -> point.getValue() > centerLine);
            boolean allBelow = subPoints.stream().allMatch(point -> point.getValue() < centerLine);

            if (allAbove || allBelow) {
                violations.add(new ControlRuleViolation(
                        2,
                        "连续9点在中心线同一侧",
                        subPoints,
                        QualityAlertLevel.WARNING
                ));
            }
        }
    }

    /**
     * 检查规则3: 连续6点持续递增或递减
     */
    private static void checkRule3(ControlChart chart, List<Measurement> data, List<ControlRuleViolation> violations) {
        List<ChartDataPoint> points = chart.getDataPoints();
        if (points.size() < 6) return;

        for (int i = 0; i <= points.size() - 6; i++) {
            List<ChartDataPoint> subPoints = points.subList(i, i + 6);

            boolean increasing = true;
            boolean decreasing = true;

            for (int j = 1; j < subPoints.size(); j++) {
                double current = subPoints.get(j).getValue();
                double previous = subPoints.get(j - 1).getValue();

                if (current <= previous) {
                    increasing = false;
                }
                if (current >= previous) {
                    decreasing = false;
                }
            }

            if (increasing || decreasing) {
                violations.add(new ControlRuleViolation(
                        3,
                        "连续6点持续递增或递减",
                        subPoints,
                        QualityAlertLevel.WARNING
                ));
            }
        }
    }

    /**
     * 获取SPC统计信息
     */
    public static SpcStatistics getSpcStatistics() {
        SpcStatistics stats = new SpcStatistics();
        stats.setTotalAlerts(activeAlerts.size());
        stats.setCriticalAlerts((int) activeAlerts.stream()
                .filter(alert -> alert.getAlertLevel() == QualityAlertLevel.CRITICAL).count());
        stats.setWarningAlerts((int) activeAlerts.stream()
                .filter(alert -> alert.getAlertLevel() == QualityAlertLevel.WARNING).count());
        stats.setOutOfControlAlerts((int) activeAlerts.stream()
                .filter(alert -> alert.getAlertLevel() == QualityAlertLevel.OUT_OF_CONTROL).count());

        return stats;
    }

    /**
     * 关闭SPC工具
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("SpcTool shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("SpcTool shutdown interrupted");
        }
    }

    // 控制图类型枚举
    public enum ChartType {
        X_BAR_R("均值-极差图", "X̄-R Chart"),
        X_BAR_S("均值-标准差图", "X̄-S Chart"),
        INDIVIDUALS("单值图", "I-MR Chart"),
        P_CHART("不合格品率图", "P Chart"),
        NP_CHART("不合格品数图", "NP Chart"),
        C_CHART("缺陷数图", "C Chart"),
        U_CHART("单位缺陷数图", "U Chart"),
        EWMA("指数加权移动平均图", "EWMA Chart"),
        CUSUM("累积和控制图", "CUSUM Chart");

        private final String chineseName;
        private final String englishName;

        ChartType(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 质量数据类型枚举
    public enum DataType {
        CONTINUOUS("连续型数据"),
        ATTRIBUTE("计数型数据"),
        DEFECT("缺陷数据");

        private final String description;

        DataType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 控制限状态枚举
    public enum ControlLimitStatus {
        IN_CONTROL("受控"),
        OUT_OF_CONTROL("失控"),
        TREND("趋势"),
        SHIFT("偏移"),
        CYCLE("周期性变化");

        private final String description;

        ControlLimitStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 质量预警级别枚举
    public enum QualityAlertLevel {
        NORMAL("正常"),
        WARNING("警告"),
        CRITICAL("严重"),
        OUT_OF_CONTROL("失控");

        private final String description;

        QualityAlertLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 过程能力评级枚举
    public enum ProcessCapabilityRating {
        EXCELLENT("优秀", "Cpk ≥ 1.67"),
        ADEQUATE("良好", "1.33 ≤ Cpk < 1.67"),
        MARGINAL("一般", "1.0 ≤ Cpk < 1.33"),
        POOR("较差", "Cpk < 1.0");

        private final String description;
        private final String criteria;

        ProcessCapabilityRating(String description, String criteria) {
            this.description = description;
            this.criteria = criteria;
        }

        public String getDescription() {
            return description;
        }

        public String getCriteria() {
            return criteria;
        }
    }

    // SPC规则检查器接口
    public interface SpcRuleChecker {
        List<ControlRuleViolation> checkViolations(ControlChart chart, List<Measurement> data);

        boolean supportsChartType(ChartType chartType);

        String getCheckerName();
    }

    // 质量数据过滤器接口
    public interface QualityDataFilter {
        List<Measurement> filterData(List<Measurement> rawData, Predicate<Measurement> filter);

        boolean supportsDataType(DataType dataType);

        String getFilterName();
    }

    // 趋势预测器接口
    public interface TrendPredictor {
        QualityTrend predictTrend(HistoricalData historicalData);

        boolean supportsPredictionType(String predictionType);

        String getPredictorName();
    }

    // 测量数据类
    public static class Measurement {
        private final String id;
        private final double value;
        private final LocalDateTime timestamp;
        private final String batchId;
        private final String productId;
        private final Map<String, Object> attributes;
        private final boolean isValid;

        public Measurement(double value, LocalDateTime timestamp) {
            this.id = UUID.randomUUID().toString();
            this.value = value;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
            this.batchId = "";
            this.productId = "";
            this.attributes = new ConcurrentHashMap<>();
            this.isValid = true;
        }

        public Measurement(String id, double value, LocalDateTime timestamp,
                           String batchId, String productId, boolean isValid) {
            this.id = id != null ? id : UUID.randomUUID().toString();
            this.value = value;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
            this.batchId = batchId != null ? batchId : "";
            this.productId = productId != null ? productId : "";
            this.attributes = new ConcurrentHashMap<>();
            this.isValid = isValid;
        }

        // Getters
        public String getId() {
            return id;
        }

        public double getValue() {
            return value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getBatchId() {
            return batchId;
        }

        public String getProductId() {
            return productId;
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

        public boolean isValid() {
            return isValid;
        }

        @Override
        public String toString() {
            return "Measurement{" +
                    "id='" + id + '\'' +
                    ", value=" + value +
                    ", timestamp=" + timestamp +
                    ", batchId='" + batchId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", isValid=" + isValid +
                    '}';
        }
    }

    // 规格限类
    public static class SpecificationLimits {
        private final Double upperSpecificationLimit; // USL
        private final Double lowerSpecificationLimit; // LSL
        private final Double targetValue;             // 目标值
        private final String unit;

        public SpecificationLimits(Double usl, Double lsl, Double target, String unit) {
            this.upperSpecificationLimit = usl;
            this.lowerSpecificationLimit = lsl;
            this.targetValue = target;
            this.unit = unit != null ? unit : "";
        }

        // Getters
        public Double getUpperSpecificationLimit() {
            return upperSpecificationLimit;
        }

        public Double getLowerSpecificationLimit() {
            return lowerSpecificationLimit;
        }

        public Double getTargetValue() {
            return targetValue;
        }

        public String getUnit() {
            return unit;
        }

        public boolean hasUpperLimit() {
            return upperSpecificationLimit != null;
        }

        public boolean hasLowerLimit() {
            return lowerSpecificationLimit != null;
        }

        public boolean hasTarget() {
            return targetValue != null;
        }

        @Override
        public String toString() {
            return "SpecificationLimits{" +
                    "USL=" + upperSpecificationLimit +
                    ", LSL=" + lowerSpecificationLimit +
                    ", Target=" + targetValue +
                    ", Unit='" + unit + '\'' +
                    '}';
        }
    }

    // 控制图类
    public static class ControlChart {
        private final ChartType chartType;
        private final String chartName;
        private final List<ChartDataPoint> dataPoints;
        private final ControlLimits controlLimits;
        private final LocalDateTime createTime;
        private final Map<String, Object> metadata;
        private final List<ControlRuleViolation> violations;

        public ControlChart(ChartType chartType, String chartName) {
            this.chartType = chartType != null ? chartType : ChartType.X_BAR_R;
            this.chartName = chartName != null ? chartName : "SPC Control Chart";
            this.dataPoints = new CopyOnWriteArrayList<>();
            this.controlLimits = new ControlLimits();
            this.createTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
            this.violations = new CopyOnWriteArrayList<>();
        }

        // Getters
        public ChartType getChartType() {
            return chartType;
        }

        public String getChartName() {
            return chartName;
        }

        public List<ChartDataPoint> getDataPoints() {
            return new ArrayList<>(dataPoints);
        }

        public void addDataPoint(ChartDataPoint dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        public ControlLimits getControlLimits() {
            return controlLimits;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
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

        public List<ControlRuleViolation> getViolations() {
            return new ArrayList<>(violations);
        }

        public void addViolation(ControlRuleViolation violation) {
            this.violations.add(violation);
        }

        @Override
        public String toString() {
            return "ControlChart{" +
                    "chartType=" + chartType +
                    ", chartName='" + chartName + '\'' +
                    ", dataPointCount=" + dataPoints.size() +
                    ", createTime=" + createTime +
                    '}';
        }
    }

    // 控制图数据点类
    public static class ChartDataPoint {
        private final double value;
        private final LocalDateTime timestamp;
        private final ControlLimitStatus status;
        private final Map<String, Object> attributes;

        public ChartDataPoint(double value, LocalDateTime timestamp, ControlLimitStatus status) {
            this.value = value;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
            this.status = status != null ? status : ControlLimitStatus.IN_CONTROL;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public double getValue() {
            return value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public ControlLimitStatus getStatus() {
            return status;
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

        @Override
        public String toString() {
            return "ChartDataPoint{" +
                    "value=" + value +
                    ", timestamp=" + timestamp +
                    ", status=" + status +
                    '}';
        }
    }

    // 控制限类
    public static class ControlLimits {
        private final Map<String, Object> parameters;
        private Double upperControlLimit;     // UCL
        private Double lowerControlLimit;     // LCL
        private Double centerLine;            // CL
        private Double upperWarningLimit;     // UWL
        private Double lowerWarningLimit;     // LWL

        public ControlLimits() {
            this.parameters = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public Double getUpperControlLimit() {
            return upperControlLimit;
        }

        public void setUpperControlLimit(Double upperControlLimit) {
            this.upperControlLimit = upperControlLimit;
        }

        public Double getLowerControlLimit() {
            return lowerControlLimit;
        }

        public void setLowerControlLimit(Double lowerControlLimit) {
            this.lowerControlLimit = lowerControlLimit;
        }

        public Double getCenterLine() {
            return centerLine;
        }

        public void setCenterLine(Double centerLine) {
            this.centerLine = centerLine;
        }

        public Double getUpperWarningLimit() {
            return upperWarningLimit;
        }

        public void setUpperWarningLimit(Double upperWarningLimit) {
            this.upperWarningLimit = upperWarningLimit;
        }

        public Double getLowerWarningLimit() {
            return lowerWarningLimit;
        }

        public void setLowerWarningLimit(Double lowerWarningLimit) {
            this.lowerWarningLimit = lowerWarningLimit;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public void setParameter(String key, Object value) {
            this.parameters.put(key, value);
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public boolean hasUpperControlLimit() {
            return upperControlLimit != null;
        }

        public boolean hasLowerControlLimit() {
            return lowerControlLimit != null;
        }

        public boolean hasCenterLine() {
            return centerLine != null;
        }

        public boolean hasUpperWarningLimit() {
            return upperWarningLimit != null;
        }

        public boolean hasLowerWarningLimit() {
            return lowerWarningLimit != null;
        }

        @Override
        public String toString() {
            return "ControlLimits{" +
                    "UCL=" + upperControlLimit +
                    ", LCL=" + lowerControlLimit +
                    ", CL=" + centerLine +
                    ", UWL=" + upperWarningLimit +
                    ", LWL=" + lowerWarningLimit +
                    '}';
        }
    }

    // 控制规则违规类
    public static class ControlRuleViolation {
        private final int ruleNumber;
        private final String ruleDescription;
        private final List<ChartDataPoint> violatingPoints;
        private final LocalDateTime violationTime;
        private final QualityAlertLevel alertLevel;

        public ControlRuleViolation(int ruleNumber, String ruleDescription,
                                    List<ChartDataPoint> violatingPoints,
                                    QualityAlertLevel alertLevel) {
            this.ruleNumber = ruleNumber;
            this.ruleDescription = ruleDescription != null ? ruleDescription : "";
            this.violatingPoints = violatingPoints != null ? new ArrayList<>(violatingPoints) : new ArrayList<>();
            this.violationTime = LocalDateTime.now();
            this.alertLevel = alertLevel != null ? alertLevel : QualityAlertLevel.WARNING;
        }

        // Getters
        public int getRuleNumber() {
            return ruleNumber;
        }

        public String getRuleDescription() {
            return ruleDescription;
        }

        public List<ChartDataPoint> getViolatingPoints() {
            return new ArrayList<>(violatingPoints);
        }

        public LocalDateTime getViolationTime() {
            return violationTime;
        }

        public QualityAlertLevel getAlertLevel() {
            return alertLevel;
        }

        @Override
        public String toString() {
            return "ControlRuleViolation{" +
                    "ruleNumber=" + ruleNumber +
                    ", ruleDescription='" + ruleDescription + '\'' +
                    ", pointCount=" + violatingPoints.size() +
                    ", violationTime=" + violationTime +
                    ", alertLevel=" + alertLevel +
                    '}';
        }
    }

    // 过程能力类
    public static class ProcessCapability {
        private final double cp;      // 过程能力指数
        private final double cpk;     // 过程能力性能指数
        private final double pp;      // 过程性能指数
        private final double ppk;     // 过程性能性能指数
        private final double mean;    // 均值
        private final double stdDev;  // 标准差
        private final SpecificationLimits limits;
        private final LocalDateTime calculationTime;
        private final Map<String, Object> metadata;
        private final ProcessCapabilityRating rating;

        public ProcessCapability(double cp, double cpk, double pp, double ppk,
                                 double mean, double stdDev, SpecificationLimits limits) {
            this.cp = cp;
            this.cpk = cpk;
            this.pp = pp;
            this.ppk = ppk;
            this.mean = mean;
            this.stdDev = stdDev;
            this.limits = limits;
            this.calculationTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
            this.rating = determineRating(cpk);
        }

        // 确定过程能力评级
        private ProcessCapabilityRating determineRating(double cpk) {
            if (cpk >= 1.67) return ProcessCapabilityRating.EXCELLENT;
            if (cpk >= 1.33) return ProcessCapabilityRating.ADEQUATE;
            if (cpk >= 1.0) return ProcessCapabilityRating.MARGINAL;
            return ProcessCapabilityRating.POOR;
        }

        // Getters
        public double getCp() {
            return cp;
        }

        public double getCpk() {
            return cpk;
        }

        public double getPp() {
            return pp;
        }

        public double getPpk() {
            return ppk;
        }

        public double getMean() {
            return mean;
        }

        public double getStdDev() {
            return stdDev;
        }

        public SpecificationLimits getLimits() {
            return limits;
        }

        public LocalDateTime getCalculationTime() {
            return calculationTime;
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

        public ProcessCapabilityRating getRating() {
            return rating;
        }

        @Override
        public String toString() {
            return "ProcessCapability{" +
                    "Cp=" + String.format("%.2f", cp) +
                    ", Cpk=" + String.format("%.2f", cpk) +
                    ", Pp=" + String.format("%.2f", pp) +
                    ", Ppk=" + String.format("%.2f", ppk) +
                    ", Mean=" + String.format("%.4f", mean) +
                    ", StdDev=" + String.format("%.4f", stdDev) +
                    ", Rating=" + rating +
                    ", calculationTime=" + calculationTime +
                    '}';
        }
    }

    // 质量预警类
    public static class QualityAlert {
        private final String alertId;
        private final String productName;
        private final QualityAlertLevel alertLevel;
        private final String message;
        private final List<Measurement> relatedMeasurements;
        private final LocalDateTime timestamp;
        private final Map<String, Object> attributes;
        private final String ruleViolated;
        private boolean acknowledged;
        private LocalDateTime acknowledgedTime;
        private String acknowledgedBy;

        public QualityAlert(String productName, QualityAlertLevel alertLevel,
                            String message, List<Measurement> relatedMeasurements, String ruleViolated) {
            this.alertId = UUID.randomUUID().toString();
            this.productName = productName != null ? productName : "Unknown";
            this.alertLevel = alertLevel != null ? alertLevel : QualityAlertLevel.WARNING;
            this.message = message != null ? message : "";
            this.relatedMeasurements = relatedMeasurements != null ? new ArrayList<>(relatedMeasurements) : new ArrayList<>();
            this.timestamp = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
            this.ruleViolated = ruleViolated != null ? ruleViolated : "";
            this.acknowledged = false;
            this.acknowledgedTime = null;
            this.acknowledgedBy = null;
        }

        // Getters and Setters
        public String getAlertId() {
            return alertId;
        }

        public String getProductName() {
            return productName;
        }

        public QualityAlertLevel getAlertLevel() {
            return alertLevel;
        }

        public String getMessage() {
            return message;
        }

        public List<Measurement> getRelatedMeasurements() {
            return new ArrayList<>(relatedMeasurements);
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
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

        public String getRuleViolated() {
            return ruleViolated;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public void setAcknowledged(boolean acknowledged) {
            this.acknowledged = acknowledged;
        }

        public LocalDateTime getAcknowledgedTime() {
            return acknowledgedTime;
        }

        public void setAcknowledgedTime(LocalDateTime acknowledgedTime) {
            this.acknowledgedTime = acknowledgedTime;
        }

        public String getAcknowledgedBy() {
            return acknowledgedBy;
        }

        public void setAcknowledgedBy(String acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
        }

        @Override
        public String toString() {
            return "QualityAlert{" +
                    "alertId='" + alertId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", alertLevel=" + alertLevel +
                    ", message='" + message + '\'' +
                    ", measurementCount=" + relatedMeasurements.size() +
                    ", timestamp=" + timestamp +
                    ", acknowledged=" + acknowledged +
                    '}';
        }
    }

    // 质量趋势类
    public static class QualityTrend {
        private final String productName;
        private final List<Measurement> historicalData;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final double trendSlope;
        private final String trendDirection;
        private final double correlationCoefficient;
        private final Map<String, Object> analysisResults;
        private final TrendPrediction prediction;

        public QualityTrend(String productName, List<Measurement> historicalData,
                            LocalDateTime startTime, LocalDateTime endTime) {
            this.productName = productName != null ? productName : "Unknown";
            this.historicalData = historicalData != null ? new ArrayList<>(historicalData) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.analysisResults = new ConcurrentHashMap<>();

            // 计算趋势指标
            TrendAnalysisResult analysis = calculateTrend();
            this.trendSlope = analysis.getSlope();
            this.trendDirection = analysis.getDirection();
            this.correlationCoefficient = analysis.getCorrelation();
            this.prediction = analyzePrediction();
        }

        // 计算趋势
        private TrendAnalysisResult calculateTrend() {
            if (historicalData.size() < 2) {
                return new TrendAnalysisResult(0.0, "平稳", 0.0);
            }

            try {
                // 简单线性回归计算
                List<Measurement> validData = historicalData.stream()
                        .filter(Measurement::isValid)
                        .collect(Collectors.toList());

                if (validData.size() < 2) {
                    return new TrendAnalysisResult(0.0, "平稳", 0.0);
                }

                double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
                for (int i = 0; i < validData.size(); i++) {
                    double x = i;
                    double y = validData.get(i).getValue();
                    sumX += x;
                    sumY += y;
                    sumXY += x * y;
                    sumXX += x * x;
                }

                int n = validData.size();
                double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
                double correlation = calculateCorrelation(validData, slope);

                String direction = slope > 0 ? "上升" : (slope < 0 ? "下降" : "平稳");

                return new TrendAnalysisResult(slope, direction, correlation);
            } catch (Exception e) {
                logger.warn("Failed to calculate trend for product: " + productName, e);
                return new TrendAnalysisResult(0.0, "未知", 0.0);
            }
        }

        // 计算相关系数
        private double calculateCorrelation(List<Measurement> data, double slope) {
            // 简化实现
            return Math.abs(slope) > 0.1 ? 0.8 : 0.2;
        }

        // 分析预测
        private TrendPrediction analyzePrediction() {
            // 基于趋势进行简单预测
            if (historicalData.isEmpty()) {
                return new TrendPrediction("稳定", 0.0, 0.0, "数据不足");
            }

            double lastValue = historicalData.get(historicalData.size() - 1).getValue();
            double predictedValue = lastValue + trendSlope * 5; // 预测5个周期后

            String prediction = Math.abs(trendSlope) > 0.01 ?
                    (trendSlope > 0 ? "上升趋势" : "下降趋势") : "稳定趋势";

            return new TrendPrediction(prediction, predictedValue, trendSlope, "基于历史趋势分析");
        }

        // Getters
        public String getProductName() {
            return productName;
        }

        public List<Measurement> getHistoricalData() {
            return new ArrayList<>(historicalData);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public double getTrendSlope() {
            return trendSlope;
        }

        public String getTrendDirection() {
            return trendDirection;
        }

        public double getCorrelationCoefficient() {
            return correlationCoefficient;
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

        public TrendPrediction getPrediction() {
            return prediction;
        }

        @Override
        public String toString() {
            return "QualityTrend{" +
                    "productName='" + productName + '\'' +
                    ", dataCount=" + historicalData.size() +
                    ", trendSlope=" + String.format("%.4f", trendSlope) +
                    ", trendDirection='" + trendDirection + '\'' +
                    ", correlation=" + String.format("%.2f", correlationCoefficient) +
                    '}';
        }

        // 趋势分析结果内部类
        private static class TrendAnalysisResult {
            private final double slope;
            private final String direction;
            private final double correlation;

            public TrendAnalysisResult(double slope, String direction, double correlation) {
                this.slope = slope;
                this.direction = direction;
                this.correlation = correlation;
            }

            public double getSlope() {
                return slope;
            }

            public String getDirection() {
                return direction;
            }

            public double getCorrelation() {
                return correlation;
            }
        }

        // 趋势预测类
        public static class TrendPrediction {
            private final String prediction;
            private final double predictedValue;
            private final double trendRate;
            private final String methodology;

            public TrendPrediction(String prediction, double predictedValue, double trendRate, String methodology) {
                this.prediction = prediction != null ? prediction : "未知";
                this.predictedValue = predictedValue;
                this.trendRate = trendRate;
                this.methodology = methodology != null ? methodology : "";
            }

            // Getters
            public String getPrediction() {
                return prediction;
            }

            public double getPredictedValue() {
                return predictedValue;
            }

            public double getTrendRate() {
                return trendRate;
            }

            public String getMethodology() {
                return methodology;
            }
        }
    }

    // 历史数据类
    public static class HistoricalData {
        private final List<Measurement> measurements;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String productId;
        private final Map<String, Object> metadata;

        public HistoricalData(List<Measurement> measurements, LocalDateTime startTime,
                              LocalDateTime endTime, String productId) {
            this.measurements = measurements != null ? new ArrayList<>(measurements) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.productId = productId != null ? productId : "";
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters
        public List<Measurement> getMeasurements() {
            return new ArrayList<>(measurements);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public String getProductId() {
            return productId;
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

        @Override
        public String toString() {
            return "HistoricalData{" +
                    "measurementCount=" + measurements.size() +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", productId='" + productId + '\'' +
                    '}';
        }
    }

    /**
     * SPC统计信息类
     */
    public static class SpcStatistics {
        private int totalAlerts;
        private int criticalAlerts;
        private int warningAlerts;
        private int outOfControlAlerts;

        // Getters and Setters
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

        public int getOutOfControlAlerts() {
            return outOfControlAlerts;
        }

        public void setOutOfControlAlerts(int outOfControlAlerts) {
            this.outOfControlAlerts = outOfControlAlerts;
        }

        @Override
        public String toString() {
            return "SpcStatistics{" +
                    "totalAlerts=" + totalAlerts +
                    ", criticalAlerts=" + criticalAlerts +
                    ", warningAlerts=" + warningAlerts +
                    ", outOfControlAlerts=" + outOfControlAlerts +
                    '}';
        }
    }
}
