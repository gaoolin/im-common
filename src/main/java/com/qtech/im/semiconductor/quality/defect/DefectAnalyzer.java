package com.qtech.im.semiconductor.quality.defect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缺陷分析工具类
 * <p>
 * 特性：
 * - 通用性：支持多种缺陷类型和分析方法
 * - 规范性：统一的缺陷数据标准和分析流程
 * - 专业性：半导体行业缺陷分析专业算法实现
 * - 灵活性：可配置的分析参数和规则
 * - 可靠性：完善的异常处理和数据验证机制
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
public class DefectAnalyzer {

    // 默认配置
    public static final int DEFAULT_ANALYSIS_BATCH_SIZE = 1000;
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RECOMMENDATIONS = 10;
    public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.7;
    private static final Logger logger = LoggerFactory.getLogger(DefectAnalyzer.class);
    // 内部存储和管理
    private static final Map<DefectType, DefectAnalyzerEngine> analyzerRegistry = new ConcurrentHashMap<>();
    private static final Map<PatternType, PatternRecognizer> recognizerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, RecommendationGenerator> generatorRegistry = new ConcurrentHashMap<>();
    private static final Map<String, StandardDefectRecord> defectCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // 初始化默认组件
    static {
        registerDefaultComponents();
        startMaintenanceTasks();
        logger.info("DefectAnalyzer initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认分析器、识别器和生成器
        // registerAnalyzer(new DefaultDefectAnalyzerEngine());
        // registerRecognizer(new DefaultPatternRecognizer());
        // registerGenerator(new DefaultRecommendationGenerator());
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(DefectAnalyzer::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        logger.debug("Defect analyzer maintenance tasks started");
    }

    /**
     * 注册缺陷分析器
     */
    public static void registerAnalyzer(DefectAnalyzerEngine analyzer) {
        if (analyzer == null) {
            throw new IllegalArgumentException("Analyzer cannot be null");
        }

        for (DefectType type : DefectType.values()) {
            if (analyzer.supportsDefectType(type)) {
                analyzerRegistry.put(type, analyzer);
                logger.debug("Registered analyzer {} for defect type {}", analyzer.getAnalyzerName(), type);
            }
        }
    }

    /**
     * 注册模式识别器
     */
    public static void registerRecognizer(PatternRecognizer recognizer) {
        if (recognizer == null) {
            throw new IllegalArgumentException("Recognizer cannot be null");
        }

        for (PatternType type : PatternType.values()) {
            if (recognizer.supportsPatternType(type)) {
                recognizerRegistry.put(type, recognizer);
                logger.debug("Registered recognizer {} for pattern type {}", recognizer.getRecognizerName(), type);
            }
        }
    }

    /**
     * 注册建议生成器
     */
    public static void registerGenerator(RecommendationGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator cannot be null");
        }

        // 注册支持的所有类别
        String[] categories = {"process", "equipment", "material", "human", "environment"}; // 简化实现
        for (String category : categories) {
            if (generator.supportsCategory(category)) {
                generatorRegistry.put(category, generator);
                logger.debug("Registered generator {} for category {}", generator.getGeneratorName(), category);
            }
        }
    }

    /**
     * 缺陷数据标准化
     */
    public static StandardDefectRecord standardizeDefect(DefectRecord rawDefect) {
        if (rawDefect == null) {
            logger.warn("Invalid defect record for standardization");
            return null;
        }

        try {
            String cacheKey = generateCacheKey(rawDefect);
            StandardDefectRecord cached = defectCache.get(cacheKey);

            if (cached != null && !isCacheExpired(cached)) {
                logger.debug("Returning cached standardized defect: {}", rawDefect.getDefectId());
                return cached;
            }

            StandardDefectRecord standardized = new StandardDefectRecord(rawDefect);

            // 缓存标准化结果
            defectCache.put(cacheKey, standardized);

            logger.debug("Standardized defect record: {}", rawDefect.getDefectId());
            return standardized;
        } catch (Exception e) {
            logger.error("Failed to standardize defect record: " + rawDefect.getDefectId(), e);
            return null;
        }
    }

    /**
     * 生成缓存键
     */
    private static String generateCacheKey(DefectRecord defect) {
        return defect.getDefectId() + "_" + defect.getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(StandardDefectRecord defect) {
        return System.currentTimeMillis() - defect.getLastUpdateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 批量缺陷数据标准化
     */
    public static List<StandardDefectRecord> standardizeDefects(List<DefectRecord> rawDefects) {
        if (rawDefects == null || rawDefects.isEmpty()) {
            logger.warn("Invalid defect records for standardization");
            return new ArrayList<>();
        }

        try {
            List<StandardDefectRecord> standardizedDefects = new ArrayList<>();

            for (DefectRecord rawDefect : rawDefects) {
                StandardDefectRecord standardized = standardizeDefect(rawDefect);
                if (standardized != null) {
                    standardizedDefects.add(standardized);
                }
            }

            logger.debug("Standardized {} defect records", standardizedDefects.size());
            return standardizedDefects;
        } catch (Exception e) {
            logger.error("Failed to standardize defect records", e);
            return new ArrayList<>();
        }
    }

    /**
     * 缺陷根因分析
     */
    public static RootCauseAnalysisResult analyzeRootCause(List<DefectRecord> defects) {
        if (defects == null || defects.isEmpty()) {
            logger.warn("Invalid defects for root cause analysis");
            return null;
        }

        try {
            long startTime = System.currentTimeMillis();

            // 标准化缺陷数据
            List<StandardDefectRecord> standardizedDefects = standardizeDefects(defects);
            if (standardizedDefects.isEmpty()) {
                logger.warn("No valid defects for root cause analysis");
                return null;
            }

            // 按缺陷类型分组并分析
            Map<DefectType, List<StandardDefectRecord>> defectsByType = standardizedDefects.stream()
                    .collect(Collectors.groupingBy(StandardDefectRecord::getDefectType));

            List<RootCauseFinding> allFindings = new ArrayList<>();
            RootCauseCategory primaryCause = RootCauseCategory.UNKNOWN;
            double maxConfidence = 0.0;
            String methodology = "综合分析";

            for (Map.Entry<DefectType, List<StandardDefectRecord>> entry : defectsByType.entrySet()) {
                DefectType type = entry.getKey();
                List<StandardDefectRecord> typeDefects = entry.getValue();

                DefectAnalyzerEngine analyzer = analyzerRegistry.get(type);
                if (analyzer != null) {
                    RootCauseAnalysisResult typeResult = analyzer.analyzeRootCause(typeDefects);
                    if (typeResult != null) {
                        allFindings.addAll(typeResult.getFindings());

                        // 更新主要根因
                        if (typeResult.getConfidenceLevel() > maxConfidence) {
                            maxConfidence = typeResult.getConfidenceLevel();
                            primaryCause = typeResult.getPrimaryCause();
                        }
                    }
                }
            }

            // 如果没有专门的分析器，使用默认分析
            if (allFindings.isEmpty()) {
                RootCauseAnalysisResult defaultResult = performDefaultRootCauseAnalysis(standardizedDefects);
                if (defaultResult != null) {
                    allFindings.addAll(defaultResult.getFindings());
                    primaryCause = defaultResult.getPrimaryCause();
                    maxConfidence = defaultResult.getConfidenceLevel();
                    methodology = defaultResult.getMethodology();
                }
            }

            long analysisDuration = System.currentTimeMillis() - startTime;

            RootCauseAnalysisResult result = new RootCauseAnalysisResult(
                    allFindings, primaryCause, maxConfidence, methodology);
            result.setMetadata("analysisDuration", analysisDuration);
            result.setMetadata("defectCount", standardizedDefects.size());

            logger.debug("Root cause analysis completed: {} findings, primary cause: {}, confidence: {}",
                    allFindings.size(), primaryCause, String.format("%.2f", maxConfidence));

            return result;
        } catch (Exception e) {
            logger.error("Failed to analyze root cause", e);
            return null;
        }
    }

    /**
     * 执行默认根因分析
     */
    private static RootCauseAnalysisResult performDefaultRootCauseAnalysis(List<StandardDefectRecord> defects) {
        try {
            List<RootCauseFinding> findings = new ArrayList<>();

            // 统计分析
            Map<DefectType, Long> typeCount = defects.stream()
                    .collect(Collectors.groupingBy(
                            StandardDefectRecord::getDefectType,
                            Collectors.counting()
                    ));

            Map<RootCauseCategory, Double> causeProbabilities = new HashMap<>();

            // 根据缺陷类型推断可能的根因
            for (Map.Entry<DefectType, Long> entry : typeCount.entrySet()) {
                DefectType type = entry.getKey();
                long count = entry.getValue();
                double probability = (double) count / defects.size();

                RootCauseCategory inferredCause = inferRootCauseFromDefectType(type);
                causeProbabilities.merge(inferredCause, probability, Double::sum);
            }

            // 创建发现
            RootCauseCategory primaryCause = RootCauseCategory.UNKNOWN;
            double maxProbability = 0.0;

            for (Map.Entry<RootCauseCategory, Double> entry : causeProbabilities.entrySet()) {
                RootCauseCategory cause = entry.getKey();
                double probability = entry.getValue();

                RootCauseFinding finding = new RootCauseFinding(
                        cause,
                        "基于缺陷类型统计推断的根因",
                        probability,
                        Arrays.asList("缺陷类型分布分析")
                );
                findings.add(finding);

                if (probability > maxProbability) {
                    maxProbability = probability;
                    primaryCause = cause;
                }
            }

            return new RootCauseAnalysisResult(findings, primaryCause, maxProbability, "统计推断");
        } catch (Exception e) {
            logger.warn("Failed to perform default root cause analysis", e);
            return null;
        }
    }

    /**
     * 根据缺陷类型推断根因
     */
    private static RootCauseCategory inferRootCauseFromDefectType(DefectType defectType) {
        switch (defectType) {
            case EQUIPMENT:
                return RootCauseCategory.EQUIPMENT_FAILURE;
            case PROCESS:
                return RootCauseCategory.PROCESS_VARIATION;
            case MATERIAL:
                return RootCauseCategory.MATERIAL_QUALITY;
            case HUMAN:
                return RootCauseCategory.HUMAN_ERROR;
            case ENVIRONMENTAL:
                return RootCauseCategory.ENVIRONMENTAL_FACTOR;
            case DESIGN:
                return RootCauseCategory.DESIGN_FLAW;
            case MEASUREMENT:
                return RootCauseCategory.MEASUREMENT_ERROR;
            default:
                return RootCauseCategory.UNKNOWN;
        }
    }

    /**
     * 缺陷模式识别
     */
    public static DefectPattern identifyPattern(List<DefectRecord> defects) {
        List<DefectPattern> patterns = identifyPatterns(defects);
        return patterns.isEmpty() ? null : patterns.get(0);
    }

    /**
     * 缺陷模式识别（批量）
     */
    public static List<DefectPattern> identifyPatterns(List<DefectRecord> defects) {
        if (defects == null || defects.isEmpty()) {
            logger.warn("Invalid defects for pattern identification");
            return new ArrayList<>();
        }

        try {
            long startTime = System.currentTimeMillis();

            // 标准化缺陷数据
            List<StandardDefectRecord> standardizedDefects = standardizeDefects(defects);
            if (standardizedDefects.isEmpty()) {
                logger.warn("No valid defects for pattern identification");
                return new ArrayList<>();
            }

            List<DefectPattern> allPatterns = new ArrayList<>();

            // 使用注册的识别器进行模式识别
            for (PatternRecognizer recognizer : recognizerRegistry.values()) {
                List<DefectPattern> patterns = recognizer.identifyPatterns(standardizedDefects);
                if (patterns != null) {
                    allPatterns.addAll(patterns);
                }
            }

            // 如果没有专门的识别器，使用默认识别
            if (allPatterns.isEmpty()) {
                List<DefectPattern> defaultPatterns = performDefaultPatternRecognition(standardizedDefects);
                if (defaultPatterns != null) {
                    allPatterns.addAll(defaultPatterns);
                }
            }

            long analysisDuration = System.currentTimeMillis() - startTime;
            logger.debug("Pattern identification completed: {} patterns found in {}ms",
                    allPatterns.size(), analysisDuration);

            return allPatterns;
        } catch (Exception e) {
            logger.error("Failed to identify defect patterns", e);
            return new ArrayList<>();
        }
    }

    /**
     * 执行默认模式识别
     */
    private static List<DefectPattern> performDefaultPatternRecognition(List<StandardDefectRecord> defects) {
        List<DefectPattern> patterns = new ArrayList<>();

        try {
            // 1. 时间聚集模式识别
            DefectPattern clusterPattern = identifyTimeClusterPattern(defects);
            if (clusterPattern != null) {
                patterns.add(clusterPattern);
            }

            // 2. 趋势模式识别
            DefectPattern trendPattern = identifyTrendPattern(defects);
            if (trendPattern != null) {
                patterns.add(trendPattern);
            }

            // 3. 类型分布模式识别
            DefectPattern typePattern = identifyTypeDistributionPattern(defects);
            if (typePattern != null) {
                patterns.add(typePattern);
            }

        } catch (Exception e) {
            logger.warn("Failed to perform default pattern recognition", e);
        }

        return patterns;
    }

    /**
     * 识别时间聚集模式
     */
    private static DefectPattern identifyTimeClusterPattern(List<StandardDefectRecord> defects) {
        if (defects.size() < 5) return null;

        try {
            // 按时间排序
            List<StandardDefectRecord> sortedDefects = defects.stream()
                    .sorted(Comparator.comparing(StandardDefectRecord::getCreateTime))
                    .collect(Collectors.toList());

            // 计算缺陷间的时间间隔
            List<Long> timeIntervals = new ArrayList<>();
            for (int i = 1; i < sortedDefects.size(); i++) {
                long interval = sortedDefects.get(i).getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                        - sortedDefects.get(i - 1).getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                timeIntervals.add(interval);
            }

            // 计算平均间隔和标准差
            double meanInterval = timeIntervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
            double stdDev = Math.sqrt(timeIntervals.stream()
                    .mapToDouble(interval -> Math.pow(interval - meanInterval, 2))
                    .average().orElse(0.0));

            // 如果标准差相对较小，说明存在聚集模式
            double coefficientOfVariation = stdDev / meanInterval;
            if (coefficientOfVariation < 0.5) { // 变异系数小于0.5认为是聚集
                double strength = 1.0 - coefficientOfVariation;
                return new DefectPattern(
                        PatternType.CLUSTER,
                        "缺陷在时间上呈现聚集分布",
                        sortedDefects,
                        strength
                );
            }
        } catch (Exception e) {
            logger.debug("Failed to identify time cluster pattern", e);
        }

        return null;
    }

    /**
     * 识别趋势模式
     */
    private static DefectPattern identifyTrendPattern(List<StandardDefectRecord> defects) {
        if (defects.size() < 10) return null;

        try {
            // 按时间分组统计
            Map<Integer, Long> defectsByDay = defects.stream()
                    .collect(Collectors.groupingBy(
                            defect -> defect.getCreateTime().getDayOfYear(),
                            Collectors.counting()
                    ));

            if (defectsByDay.size() < 3) return null;

            // 简单线性回归检测趋势
            List<Integer> days = new ArrayList<>(defectsByDay.keySet());
            Collections.sort(days);

            double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
            for (int i = 0; i < days.size(); i++) {
                double x = i;
                double y = defectsByDay.get(days.get(i));
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumXX += x * x;
            }

            int n = days.size();
            double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);

            // 如果斜率显著不为零，说明存在趋势
            if (Math.abs(slope) > 0.1) {
                double strength = Math.min(1.0, Math.abs(slope) / 2.0);
                PatternType patternType = slope > 0 ? PatternType.TREND : PatternType.TREND;

                return new DefectPattern(
                        patternType,
                        slope > 0 ? "缺陷数量呈上升趋势" : "缺陷数量呈下降趋势",
                        defects,
                        strength
                );
            }
        } catch (Exception e) {
            logger.debug("Failed to identify trend pattern", e);
        }

        return null;
    }

    /**
     * 识别类型分布模式
     */
    private static DefectPattern identifyTypeDistributionPattern(List<StandardDefectRecord> defects) {
        if (defects.size() < 5) return null;

        try {
            // 统计各类缺陷的比例
            Map<DefectType, Long> typeCount = defects.stream()
                    .collect(Collectors.groupingBy(
                            StandardDefectRecord::getDefectType,
                            Collectors.counting()
                    ));

            long total = defects.size();
            double maxProportion = typeCount.values().stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0) / (double) total;

            // 如果某一类缺陷占比超过50%，说明存在主导类型
            if (maxProportion > 0.5) {
                DefectType dominantType = typeCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(DefectType.UNKNOWN);

                double strength = maxProportion;

                List<StandardDefectRecord> dominantDefects = defects.stream()
                        .filter(defect -> defect.getDefectType() == dominantType)
                        .collect(Collectors.toList());

                return new DefectPattern(
                        PatternType.CORRELATION,
                        "缺陷类型分布不均匀，" + dominantType.getDescription() + "占主导地位",
                        dominantDefects,
                        strength
                );
            }
        } catch (Exception e) {
            logger.debug("Failed to identify type distribution pattern", e);
        }

        return null;
    }

    /**
     * 缺陷预防建议生成
     */
    public static List<PreventionRecommendation> generatePreventionRecommendations(DefectAnalysisResult analysis) {
        if (analysis == null) {
            logger.warn("Invalid analysis result for recommendation generation");
            return new ArrayList<>();
        }

        try {
            long startTime = System.currentTimeMillis();

            List<PreventionRecommendation> allRecommendations = new ArrayList<>();

            // 使用注册的生成器生成建议
            for (RecommendationGenerator generator : generatorRegistry.values()) {
                List<PreventionRecommendation> recommendations = generator.generateRecommendations(analysis);
                if (recommendations != null) {
                    allRecommendations.addAll(recommendations);
                }
            }

            // 如果没有专门的生成器，使用默认生成
            if (allRecommendations.isEmpty()) {
                List<PreventionRecommendation> defaultRecommendations =
                        performDefaultRecommendationGeneration(analysis);
                if (defaultRecommendations != null) {
                    allRecommendations.addAll(defaultRecommendations);
                }
            }

            // 按优先级排序并限制数量
            allRecommendations.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
            if (allRecommendations.size() > DEFAULT_MAX_RECOMMENDATIONS) {
                allRecommendations = allRecommendations.subList(0, DEFAULT_MAX_RECOMMENDATIONS);
            }

            long generationDuration = System.currentTimeMillis() - startTime;
            logger.debug("Prevention recommendations generated: {} recommendations in {}ms",
                    allRecommendations.size(), generationDuration);

            return allRecommendations;
        } catch (Exception e) {
            logger.error("Failed to generate prevention recommendations", e);
            return new ArrayList<>();
        }
    }

    /**
     * 执行默认建议生成
     */
    private static List<PreventionRecommendation> performDefaultRecommendationGeneration(DefectAnalysisResult analysis) {
        List<PreventionRecommendation> recommendations = new ArrayList<>();

        try {
            RootCauseAnalysisResult rootCause = analysis.getRootCauseAnalysis();
            if (rootCause != null) {
                RootCauseCategory primaryCause = rootCause.getPrimaryCause();
                double confidence = rootCause.getConfidenceLevel();

                PreventionRecommendation recommendation = generateRecommendationForRootCause(
                        primaryCause, confidence);
                if (recommendation != null) {
                    recommendations.add(recommendation);
                }
            }

            // 基于模式的建议
            List<DefectPattern> patterns = analysis.getIdentifiedPatterns();
            for (DefectPattern pattern : patterns) {
                PreventionRecommendation patternRecommendation = generateRecommendationForPattern(pattern);
                if (patternRecommendation != null) {
                    recommendations.add(patternRecommendation);
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to perform default recommendation generation", e);
        }

        return recommendations;
    }

    /**
     * 为根因生成建议
     */
    private static PreventionRecommendation generateRecommendationForRootCause(
            RootCauseCategory cause, double confidence) {

        String title, description, category;
        int priority;
        List<String> steps = new ArrayList<>();

        switch (cause) {
            case EQUIPMENT_FAILURE:
                title = "加强设备维护和监控";
                description = "针对设备故障导致的缺陷，建议加强预防性维护和实时监控";
                category = "equipment";
                priority = 5;
                steps.addAll(Arrays.asList(
                        "建立设备预防性维护计划",
                        "安装关键设备实时监控系统",
                        "培训设备操作和维护人员",
                        "定期进行设备性能评估"
                ));
                break;

            case PROCESS_VARIATION:
                title = "优化工艺参数控制";
                description = "针对工艺变异导致的缺陷，建议加强工艺参数控制和稳定性管理";
                category = "process";
                priority = 4;
                steps.addAll(Arrays.asList(
                        "实施统计过程控制(SPC)",
                        "优化关键工艺参数设置",
                        "建立工艺参数报警机制",
                        "定期进行工艺能力分析"
                ));
                break;

            case MATERIAL_QUALITY:
                title = "加强原材料质量控制";
                description = "针对材料质量问题导致的缺陷，建议强化供应商管理和来料检验";
                category = "material";
                priority = 4;
                steps.addAll(Arrays.asList(
                        "完善供应商评估体系",
                        "加强来料质量检验",
                        "建立材料质量追溯机制",
                        "定期进行供应商审核"
                ));
                break;

            case HUMAN_ERROR:
                title = "加强人员培训和作业指导";
                description = "针对人为错误导致的缺陷，建议加强培训和标准化作业管理";
                category = "human";
                priority = 3;
                steps.addAll(Arrays.asList(
                        "制定标准化作业指导书",
                        "定期开展技能培训",
                        "建立质量意识教育机制",
                        "实施作业过程监督检查"
                ));
                break;

            default:
                title = "综合质量改进措施";
                description = "建议采取综合性的质量改进措施";
                category = "general";
                priority = 2;
                steps.addAll(Arrays.asList(
                        "开展质量管理体系评审",
                        "分析历史缺陷数据趋势",
                        "制定针对性改进计划",
                        "建立持续改进机制"
                ));
                break;
        }

        double effectiveness = Math.min(1.0, confidence * 0.8 + 0.2);

        return new PreventionRecommendation(title, description, priority, effectiveness, category)
                .addImplementationStep("问题识别和分析")
                .addImplementationStep("制定改进方案")
                .addImplementationStep("实施改进措施")
                .addImplementationStep("效果跟踪和评估");
    }

    /**
     * 为模式生成建议
     */
    private static PreventionRecommendation generateRecommendationForPattern(DefectPattern pattern) {
        String title, description, category;
        int priority;
        List<String> steps = new ArrayList<>();

        switch (pattern.getPatternType()) {
            case CLUSTER:
                title = "加强生产过程监控";
                description = "针对缺陷聚集模式，建议加强生产过程的实时监控和预警";
                category = "process";
                priority = 4;
                steps.addAll(Arrays.asList(
                        "增加关键工序监控点",
                        "建立实时异常预警机制",
                        "分析聚集时间段的生产条件",
                        "制定应急响应预案"
                ));
                break;

            case TREND:
                title = "趋势分析和预防措施";
                description = "针对缺陷趋势模式，建议进行根本原因分析并采取预防措施";
                category = "analysis";
                priority = 3;
                steps.addAll(Arrays.asList(
                        "深入分析趋势产生的原因",
                        "制定针对性预防措施",
                        "建立趋势监控机制",
                        "定期评审改进效果"
                ));
                break;

            default:
                return null;
        }

        double effectiveness = pattern.getStrength() * 0.7 + 0.3;

        return new PreventionRecommendation(title, description, priority, effectiveness, category);
    }

    /**
     * 完整缺陷分析流程
     */
    public static DefectAnalysisResult performCompleteAnalysis(List<DefectRecord> defects) {
        if (defects == null || defects.isEmpty()) {
            logger.warn("Invalid defects for complete analysis");
            return null;
        }

        try {
            long startTime = System.currentTimeMillis();

            logger.info("Starting complete defect analysis for {} defects", defects.size());

            // 1. 标准化缺陷数据
            List<StandardDefectRecord> standardizedDefects = standardizeDefects(defects);

            // 2. 根因分析
            RootCauseAnalysisResult rootCauseResult = analyzeRootCause(defects);

            // 3. 模式识别
            List<DefectPattern> patterns = identifyPatterns(defects);

            long analysisDuration = System.currentTimeMillis() - startTime;

            // 4. 生成分析结果
            DefectAnalysisResult analysisResult = new DefectAnalysisResult(
                    standardizedDefects, rootCauseResult, patterns, analysisDuration);

            // 5. 生成预防建议
            List<PreventionRecommendation> recommendations = generatePreventionRecommendations(analysisResult);
            recommendations.forEach(analysisResult::addRecommendation);

            logger.info("Complete defect analysis finished in {}ms with {} recommendations",
                    analysisDuration, recommendations.size());

            return analysisResult;
        } catch (Exception e) {
            logger.error("Failed to perform complete defect analysis", e);
            return null;
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;
            defectCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastUpdateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );
            logger.debug("Cleaned up defect cache, remaining entries: {}", defectCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup defect cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setCacheSize(defectCache.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 关闭缺陷分析器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("DefectAnalyzer shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("DefectAnalyzer shutdown interrupted");
        }
    }

    // 缺陷严重程度枚举
    public enum DefectSeverity {
        CRITICAL("严重", 5),
        MAJOR("主要", 4),
        MINOR("次要", 3),
        COSMETIC("外观", 2),
        OBSERVATION("观察项", 1);

        private final String description;
        private final int priority;

        DefectSeverity(String description, int priority) {
            this.description = description;
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }
    }

    // 缺陷状态枚举
    public enum DefectStatus {
        NEW("新建"),
        ANALYZING("分析中"),
        CONFIRMED("已确认"),
        RESOLVING("解决中"),
        RESOLVED("已解决"),
        VERIFIED("已验证"),
        CLOSED("已关闭"),
        REOPENED("重新打开");

        private final String description;

        DefectStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 缺陷类型枚举
    public enum DefectType {
        PROCESS("工艺缺陷"),
        MATERIAL("材料缺陷"),
        EQUIPMENT("设备缺陷"),
        HUMAN("人为缺陷"),
        DESIGN("设计缺陷"),
        ENVIRONMENTAL("环境缺陷"),
        MEASUREMENT("测量缺陷"),
        UNKNOWN("未知缺陷");

        private final String description;

        DefectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 根因分类枚举
    public enum RootCauseCategory {
        EQUIPMENT_FAILURE("设备故障"),
        PROCESS_VARIATION("工艺变异"),
        MATERIAL_QUALITY("材料质量"),
        HUMAN_ERROR("人为错误"),
        ENVIRONMENTAL_FACTOR("环境因素"),
        DESIGN_FLAW("设计缺陷"),
        MEASUREMENT_ERROR("测量误差"),
        MAINTENANCE_ISSUE("维护问题"),
        UNKNOWN("未知原因");

        private final String description;

        RootCauseCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 缺陷模式类型枚举
    public enum PatternType {
        CLUSTER("聚集模式"),
        TREND("趋势模式"),
        PERIODIC("周期模式"),
        RANDOM("随机模式"),
        CORRELATION("关联模式");

        private final String description;

        PatternType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 缺陷分析器接口
    public interface DefectAnalyzerEngine {
        RootCauseAnalysisResult analyzeRootCause(List<StandardDefectRecord> defects) throws Exception;

        boolean supportsDefectType(DefectType defectType);

        String getAnalyzerName();
    }

    // 模式识别器接口
    public interface PatternRecognizer {
        List<DefectPattern> identifyPatterns(List<StandardDefectRecord> defects) throws Exception;

        boolean supportsPatternType(PatternType patternType);

        String getRecognizerName();
    }

    // 建议生成器接口
    public interface RecommendationGenerator {
        List<PreventionRecommendation> generateRecommendations(DefectAnalysisResult analysis) throws Exception;

        boolean supportsCategory(String category);

        String getGeneratorName();
    }

    // 原始缺陷记录类
    public static class DefectRecord {
        private final String defectId;
        private final String productId;
        private final String batchId;
        private final DefectType defectType;
        private final DefectSeverity severity;
        private final String description;
        private final LocalDateTime createTime;
        private final String location;
        private final Map<String, Object> attributes;
        private final List<String> images;
        private final String reporter;
        private final boolean isValid;

        public DefectRecord(String defectId, String productId, String batchId,
                            DefectType defectType, DefectSeverity severity,
                            String description, LocalDateTime createTime) {
            this.defectId = defectId != null ? defectId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.batchId = batchId != null ? batchId : "";
            this.defectType = defectType != null ? defectType : DefectType.UNKNOWN;
            this.severity = severity != null ? severity : DefectSeverity.OBSERVATION;
            this.description = description != null ? description : "";
            this.createTime = createTime != null ? createTime : LocalDateTime.now();
            this.location = "";
            this.attributes = new ConcurrentHashMap<>();
            this.images = new ArrayList<>();
            this.reporter = "";
            this.isValid = true;
        }

        // Getters
        public String getDefectId() {
            return defectId;
        }

        public String getProductId() {
            return productId;
        }

        public String getBatchId() {
            return batchId;
        }

        public DefectType getDefectType() {
            return defectType;
        }

        public DefectSeverity getSeverity() {
            return severity;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getLocation() {
            return location;
        }

        public DefectRecord setLocation(String location) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public DefectRecord setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public List<String> getImages() {
            return new ArrayList<>(images);
        }

        public DefectRecord addImage(String imageUrl) {
            this.images.add(imageUrl);
            return this;
        }

        public String getReporter() {
            return reporter;
        }

        public DefectRecord setReporter(String reporter) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public boolean isValid() {
            return isValid;
        }

        @Override
        public String toString() {
            return "DefectRecord{" +
                    "defectId='" + defectId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", batchId='" + batchId + '\'' +
                    ", defectType=" + defectType +
                    ", severity=" + severity +
                    ", createTime=" + createTime +
                    ", isValid=" + isValid +
                    '}';
        }
    }

    // 标准化缺陷记录类
    public static class StandardDefectRecord {
        private final String defectId;
        private final String productId;
        private final String batchId;
        private final DefectType defectType;
        private final DefectSeverity severity;
        private final String description;
        private final LocalDateTime createTime;
        private final String location;
        private final String standardizedDescription;
        private final String defectCode;
        private final Map<String, Object> standardizedAttributes;
        private final List<String> images;
        private final String reporter;
        private final DefectStatus status;
        private final LocalDateTime lastUpdateTime;

        public StandardDefectRecord(DefectRecord rawDefect) {
            this.defectId = rawDefect.getDefectId();
            this.productId = rawDefect.getProductId();
            this.batchId = rawDefect.getBatchId();
            this.defectType = rawDefect.getDefectType();
            this.severity = rawDefect.getSeverity();
            this.description = rawDefect.getDescription();
            this.createTime = rawDefect.getCreateTime();
            this.location = rawDefect.getLocation();
            this.standardizedDescription = standardizeDescription(rawDefect.getDescription());
            this.defectCode = generateDefectCode(rawDefect);
            this.standardizedAttributes = standardizeAttributes(rawDefect.getAttributes());
            this.images = new ArrayList<>(rawDefect.getImages());
            this.reporter = rawDefect.getReporter();
            this.status = DefectStatus.NEW;
            this.lastUpdateTime = LocalDateTime.now();
        }

        // 标准化描述
        private String standardizeDescription(String description) {
            if (description == null || description.isEmpty()) {
                return "未提供描述";
            }

            // 移除多余空格和特殊字符
            return description.trim().replaceAll("\\s+", " ");
        }

        // 生成缺陷代码
        private String generateDefectCode(DefectRecord rawDefect) {
            StringBuilder code = new StringBuilder();
            code.append(rawDefect.getDefectType().name().charAt(0));
            code.append(String.format("%02d", rawDefect.getSeverity().getPriority()));

            // 添加产品代码
            if (rawDefect.getProductId() != null && !rawDefect.getProductId().isEmpty()) {
                String productCode = rawDefect.getProductId().replaceAll("[^A-Z0-9]", "");
                if (productCode.length() > 3) {
                    code.append(productCode.substring(0, 3));
                } else {
                    code.append(productCode);
                }
            }

            // 添加时间戳
            code.append(rawDefect.getCreateTime().getYear() % 100);
            code.append(String.format("%03d", rawDefect.getCreateTime().getDayOfYear()));

            return code.toString().toUpperCase();
        }

        // 标准化属性
        private Map<String, Object> standardizeAttributes(Map<String, Object> rawAttributes) {
            Map<String, Object> standardized = new ConcurrentHashMap<>();

            if (rawAttributes != null) {
                for (Map.Entry<String, Object> entry : rawAttributes.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // 标准化键名
                    String standardizedKey = key.toLowerCase().replaceAll("[^a-z0-9_]", "_");

                    // 标准化值
                    Object standardizedValue = standardizeAttributeValue(value);

                    standardized.put(standardizedKey, standardizedValue);
                }
            }

            return standardized;
        }

        // 标准化属性值
        private Object standardizeAttributeValue(Object value) {
            if (value == null) {
                return "";
            }

            if (value instanceof String) {
                return ((String) value).trim();
            }

            if (value instanceof Number) {
                return value;
            }

            return value.toString().trim();
        }

        // Getters
        public String getDefectId() {
            return defectId;
        }

        public String getProductId() {
            return productId;
        }

        public String getBatchId() {
            return batchId;
        }

        public DefectType getDefectType() {
            return defectType;
        }

        public DefectSeverity getSeverity() {
            return severity;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getLocation() {
            return location;
        }

        public String getStandardizedDescription() {
            return standardizedDescription;
        }

        public String getDefectCode() {
            return defectCode;
        }

        public Map<String, Object> getStandardizedAttributes() {
            return new HashMap<>(standardizedAttributes);
        }

        public List<String> getImages() {
            return new ArrayList<>(images);
        }

        public String getReporter() {
            return reporter;
        }

        public DefectStatus getStatus() {
            return status;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        @Override
        public String toString() {
            return "StandardDefectRecord{" +
                    "defectId='" + defectId + '\'' +
                    ", defectCode='" + defectCode + '\'' +
                    ", productId='" + productId + '\'' +
                    ", defectType=" + defectType +
                    ", severity=" + severity +
                    ", status=" + status +
                    '}';
        }
    }

    // 根因分析结果类
    public static class RootCauseAnalysisResult {
        private final String analysisId;
        private final List<RootCauseFinding> findings;
        private final RootCauseCategory primaryCause;
        private final double confidenceLevel;
        private final LocalDateTime analysisTime;
        private final String methodology;
        private final Map<String, Object> metadata;
        private final List<String> recommendations;

        public RootCauseAnalysisResult(List<RootCauseFinding> findings,
                                       RootCauseCategory primaryCause,
                                       double confidenceLevel, String methodology) {
            this.analysisId = UUID.randomUUID().toString();
            this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
            this.primaryCause = primaryCause;
            this.confidenceLevel = confidenceLevel;
            this.analysisTime = LocalDateTime.now();
            this.methodology = methodology != null ? methodology : "统计分析";
            this.metadata = new ConcurrentHashMap<>();
            this.recommendations = new ArrayList<>();
        }

        // Getters
        public String getAnalysisId() {
            return analysisId;
        }

        public List<RootCauseFinding> getFindings() {
            return new ArrayList<>(findings);
        }

        public void addFinding(RootCauseFinding finding) {
            this.findings.add(finding);
        }

        public RootCauseCategory getPrimaryCause() {
            return primaryCause;
        }

        public double getConfidenceLevel() {
            return confidenceLevel;
        }

        public LocalDateTime getAnalysisTime() {
            return analysisTime;
        }

        public String getMethodology() {
            return methodology;
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

        public List<String> getRecommendations() {
            return new ArrayList<>(recommendations);
        }

        public void addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
        }

        @Override
        public String toString() {
            return "RootCauseAnalysisResult{" +
                    "analysisId='" + analysisId + '\'' +
                    ", findingCount=" + findings.size() +
                    ", primaryCause=" + primaryCause +
                    ", confidenceLevel=" + String.format("%.2f", confidenceLevel) +
                    ", analysisTime=" + analysisTime +
                    '}';
        }
    }

    // 根因发现类
    public static class RootCauseFinding {
        private final RootCauseCategory category;
        private final String description;
        private final double probability;
        private final List<String> evidence;
        private final Map<String, Object> attributes;

        public RootCauseFinding(RootCauseCategory category, String description,
                                double probability, List<String> evidence) {
            this.category = category != null ? category : RootCauseCategory.UNKNOWN;
            this.description = description != null ? description : "";
            this.probability = probability;
            this.evidence = evidence != null ? new ArrayList<>(evidence) : new ArrayList<>();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public RootCauseCategory getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public double getProbability() {
            return probability;
        }

        public List<String> getEvidence() {
            return new ArrayList<>(evidence);
        }

        public void addEvidence(String evidence) {
            this.evidence.add(evidence);
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
            return "RootCauseFinding{" +
                    "category=" + category +
                    ", description='" + description + '\'' +
                    ", probability=" + String.format("%.2f", probability) +
                    ", evidenceCount=" + evidence.size() +
                    '}';
        }
    }

    // 缺陷模式类
    public static class DefectPattern {
        private final String patternId;
        private final PatternType patternType;
        private final String description;
        private final List<StandardDefectRecord> relatedDefects;
        private final double strength;
        private final LocalDateTime detectionTime;
        private final Map<String, Object> characteristics;
        private final String significance;

        public DefectPattern(PatternType patternType, String description,
                             List<StandardDefectRecord> relatedDefects, double strength) {
            this.patternId = UUID.randomUUID().toString();
            this.patternType = patternType != null ? patternType : PatternType.RANDOM;
            this.description = description != null ? description : "";
            this.relatedDefects = relatedDefects != null ? new ArrayList<>(relatedDefects) : new ArrayList<>();
            this.strength = strength;
            this.detectionTime = LocalDateTime.now();
            this.characteristics = new ConcurrentHashMap<>();
            this.significance = determineSignificance(strength);
        }

        // 确定重要性
        private String determineSignificance(double strength) {
            if (strength >= 0.9) return "极高";
            if (strength >= 0.7) return "高";
            if (strength >= 0.5) return "中等";
            if (strength >= 0.3) return "低";
            return "极低";
        }

        // Getters
        public String getPatternId() {
            return patternId;
        }

        public PatternType getPatternType() {
            return patternType;
        }

        public String getDescription() {
            return description;
        }

        public List<StandardDefectRecord> getRelatedDefects() {
            return new ArrayList<>(relatedDefects);
        }

        public double getStrength() {
            return strength;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public Map<String, Object> getCharacteristics() {
            return new HashMap<>(characteristics);
        }

        public void setCharacteristic(String key, Object value) {
            this.characteristics.put(key, value);
        }

        public Object getCharacteristic(String key) {
            return this.characteristics.get(key);
        }

        public String getSignificance() {
            return significance;
        }

        @Override
        public String toString() {
            return "DefectPattern{" +
                    "patternId='" + patternId + '\'' +
                    ", patternType=" + patternType +
                    ", relatedDefectCount=" + relatedDefects.size() +
                    ", strength=" + String.format("%.2f", strength) +
                    ", significance='" + significance + '\'' +
                    '}';
        }
    }

    // 缺陷分析结果类
    public static class DefectAnalysisResult {
        private final String resultId;
        private final List<StandardDefectRecord> analyzedDefects;
        private final RootCauseAnalysisResult rootCauseAnalysis;
        private final List<DefectPattern> identifiedPatterns;
        private final LocalDateTime analysisTime;
        private final long analysisDuration;
        private final Map<String, Object> summary;
        private final List<PreventionRecommendation> recommendations;

        public DefectAnalysisResult(List<StandardDefectRecord> analyzedDefects,
                                    RootCauseAnalysisResult rootCauseAnalysis,
                                    List<DefectPattern> identifiedPatterns,
                                    long analysisDuration) {
            this.resultId = UUID.randomUUID().toString();
            this.analyzedDefects = analyzedDefects != null ? new ArrayList<>(analyzedDefects) : new ArrayList<>();
            this.rootCauseAnalysis = rootCauseAnalysis;
            this.identifiedPatterns = identifiedPatterns != null ? new ArrayList<>(identifiedPatterns) : new ArrayList<>();
            this.analysisTime = LocalDateTime.now();
            this.analysisDuration = analysisDuration;
            this.summary = new ConcurrentHashMap<>();
            this.recommendations = new ArrayList<>();

            // 生成摘要信息
            generateSummary();
        }

        // 生成摘要信息
        private void generateSummary() {
            summary.put("totalDefects", analyzedDefects.size());
            summary.put("analysisTime", analysisTime);
            summary.put("analysisDuration", analysisDuration + "ms");

            if (!analyzedDefects.isEmpty()) {
                // 按严重程度统计
                Map<DefectSeverity, Long> severityCount = analyzedDefects.stream()
                        .collect(Collectors.groupingBy(
                                StandardDefectRecord::getSeverity,
                                Collectors.counting()
                        ));
                summary.put("severityDistribution", severityCount);

                // 按缺陷类型统计
                Map<DefectType, Long> typeCount = analyzedDefects.stream()
                        .collect(Collectors.groupingBy(
                                StandardDefectRecord::getDefectType,
                                Collectors.counting()
                        ));
                summary.put("typeDistribution", typeCount);
            }

            if (rootCauseAnalysis != null) {
                summary.put("primaryRootCause", rootCauseAnalysis.getPrimaryCause());
                summary.put("confidenceLevel", rootCauseAnalysis.getConfidenceLevel());
            }

            summary.put("patternCount", identifiedPatterns.size());
        }

        // Getters
        public String getResultId() {
            return resultId;
        }

        public List<StandardDefectRecord> getAnalyzedDefects() {
            return new ArrayList<>(analyzedDefects);
        }

        public RootCauseAnalysisResult getRootCauseAnalysis() {
            return rootCauseAnalysis;
        }

        public List<DefectPattern> getIdentifiedPatterns() {
            return new ArrayList<>(identifiedPatterns);
        }

        public LocalDateTime getAnalysisTime() {
            return analysisTime;
        }

        public long getAnalysisDuration() {
            return analysisDuration;
        }

        public Map<String, Object> getSummary() {
            return new HashMap<>(summary);
        }

        public List<PreventionRecommendation> getRecommendations() {
            return new ArrayList<>(recommendations);
        }

        public void addRecommendation(PreventionRecommendation recommendation) {
            this.recommendations.add(recommendation);
        }

        @Override
        public String toString() {
            return "DefectAnalysisResult{" +
                    "resultId='" + resultId + '\'' +
                    ", defectCount=" + analyzedDefects.size() +
                    ", patternCount=" + identifiedPatterns.size() +
                    ", analysisTime=" + analysisTime +
                    ", analysisDuration=" + analysisDuration + "ms" +
                    '}';
        }
    }

    // 预防建议类
    public static class PreventionRecommendation {
        private final String recommendationId;
        private final String title;
        private final String description;
        private final int priority;
        private final double expectedEffectiveness;
        private final List<String> implementationSteps;
        private final LocalDateTime createTime;
        private final String category;
        private final Map<String, Object> metadata;
        private final String responsibleParty;

        public PreventionRecommendation(String title, String description,
                                        int priority, double expectedEffectiveness,
                                        String category) {
            this.recommendationId = UUID.randomUUID().toString();
            this.title = title != null ? title : "";
            this.description = description != null ? description : "";
            this.priority = priority;
            this.expectedEffectiveness = expectedEffectiveness;
            this.implementationSteps = new ArrayList<>();
            this.createTime = LocalDateTime.now();
            this.category = category != null ? category : "通用";
            this.metadata = new ConcurrentHashMap<>();
            this.responsibleParty = "";
        }

        // Getters and Setters
        public String getRecommendationId() {
            return recommendationId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public double getExpectedEffectiveness() {
            return expectedEffectiveness;
        }

        public List<String> getImplementationSteps() {
            return new ArrayList<>(implementationSteps);
        }

        public PreventionRecommendation addImplementationStep(String step) {
            this.implementationSteps.add(step);
            return this;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getCategory() {
            return category;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public PreventionRecommendation setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public String getResponsibleParty() {
            return responsibleParty;
        }

        public PreventionRecommendation setResponsibleParty(String responsibleParty) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "PreventionRecommendation{" +
                    "recommendationId='" + recommendationId + '\'' +
                    ", title='" + title + '\'' +
                    ", priority=" + priority +
                    ", expectedEffectiveness=" + String.format("%.2f", expectedEffectiveness) +
                    ", category='" + category + '\'' +
                    '}';
        }
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private int cacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        public long getCacheTimeout() {
            return cacheTimeout;
        }

        public void setCacheTimeout(long cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
        }

        @Override
        public String toString() {
            return "CacheStatistics{" +
                    "cacheSize=" + cacheSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }
}