package com.im.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 通用数据质量检查工具类
 * <p>
 * 特性：
 * - 通用化：支持任意类型数据的质量检查
 * - 规范化：统一的质量检查标准和报告格式
 * - 灵活性：支持自定义检查规则和阈值
 * - 可靠性：基于统计学的质量评估方法
 * - 容错性：部分失败不影响整体检查过程
 * - 专业性：提供详细的质量分析报告
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class DataQualityChecker {

    // 默认完整性阈值
    public static final double DEFAULT_COMPLETENESS_THRESHOLD = 0.95;
    // 默认一致性阈值
    public static final double DEFAULT_CONSISTENCY_THRESHOLD = 0.98;
    // 默认准确性阈值
    public static final double DEFAULT_ACCURACY_THRESHOLD = 0.99;
    private static final Logger logger = LoggerFactory.getLogger(DataQualityChecker.class);

    /**
     * 检查数据完整性
     *
     * @param data      待检查数据
     * @param validator 数据有效性验证器
     * @param <T>       数据类型
     * @return 完整性检查结果
     */
    public static <T> CompletenessResult checkCompleteness(List<T> data, Predicate<T> validator) {
        return checkCompleteness(data, validator, DEFAULT_COMPLETENESS_THRESHOLD);
    }

    /**
     * 检查数据完整性（指定阈值）
     *
     * @param data      待检查数据
     * @param validator 数据有效性验证器
     * @param threshold 完整性阈值
     * @param <T>       数据类型
     * @return 完整性检查结果
     */
    public static <T> CompletenessResult checkCompleteness(List<T> data, Predicate<T> validator, double threshold) {
        if (data == null) {
            return new CompletenessResult(0, 0, 0, false, "Data is null");
        }

        if (validator == null) {
            return new CompletenessResult(data.size(), data.size(), 1.0, true, "No validator provided");
        }

        int total = data.size();
        int valid = 0;

        for (T item : data) {
            if (item != null && validator.test(item)) {
                valid++;
            }
        }

        double completeness = total > 0 ? (double) valid / total : 0;
        boolean pass = completeness >= threshold;
        String message = String.format("Completeness: %.2f%% (%d/%d)", completeness * 100, valid, total);

        return new CompletenessResult(total, valid, completeness, pass, message);
    }

    /**
     * 检查数据一致性
     *
     * @param dataLists  多个数据列表
     * @param comparator 数据比较器
     * @param <T>        数据类型
     * @return 一致性检查结果
     */
    public static <T> ConsistencyResult checkConsistency(List<List<T>> dataLists, Comparator<T> comparator) {
        return checkConsistency(dataLists, comparator, DEFAULT_CONSISTENCY_THRESHOLD);
    }

    /**
     * 检查数据一致性（指定阈值）
     *
     * @param dataLists  多个数据列表
     * @param comparator 数据比较器
     * @param threshold  一致性阈值
     * @param <T>        数据类型
     * @return 一致性检查结果
     */
    public static <T> ConsistencyResult checkConsistency(List<List<T>> dataLists, Comparator<T> comparator, double threshold) {
        if (dataLists == null || dataLists.isEmpty()) {
            return new ConsistencyResult(0, 0, 0, false, "No data lists provided");
        }

        if (dataLists.size() < 2) {
            return new ConsistencyResult(1, 1, 1.0, true, "Only one data entity provided");
        }

        if (comparator == null) {
            return new ConsistencyResult(0, 0, 0, false, "No comparator provided");
        }

        int totalComparisons = 0;
        int consistentComparisons = 0;

        // 两两比较
        for (int i = 0; i < dataLists.size(); i++) {
            for (int j = i + 1; j < dataLists.size(); j++) {
                List<T> list1 = dataLists.get(i);
                List<T> list2 = dataLists.get(j);

                int maxSize = Math.max(list1.size(), list2.size());
                totalComparisons += maxSize;

                for (int k = 0; k < maxSize; k++) {
                    T item1 = k < list1.size() ? list1.get(k) : null;
                    T item2 = k < list2.size() ? list2.get(k) : null;

                    if (item1 == null && item2 == null) {
                        consistentComparisons++;
                    } else if (item1 != null && item2 != null && comparator.compare(item1, item2) == 0) {
                        consistentComparisons++;
                    }
                }
            }
        }

        double consistency = totalComparisons > 0 ? (double) consistentComparisons / totalComparisons : 0;
        boolean pass = consistency >= threshold;
        String message = String.format("Consistency: %.2f%% (%d/%d)", consistency * 100, consistentComparisons, totalComparisons);

        return new ConsistencyResult(totalComparisons, consistentComparisons, consistency, pass, message);
    }

    /**
     * 生成数据质量报告
     *
     * @param data       待检查数据
     * @param validators 多个验证器
     * @param <T>        数据类型
     * @return 数据质量报告
     */
    public static <T> DataQualityReport generateQualityReport(List<T> data, List<Predicate<T>> validators) {
        if (data == null || validators == null || validators.isEmpty()) {
            return new DataQualityReport(new ArrayList<>());
        }

        List<QualityDimensionResult> results = new ArrayList<>();

        for (int i = 0; i < validators.size(); i++) {
            Predicate<T> validator = validators.get(i);
            String dimensionName = "Dimension-" + (i + 1);

            try {
                CompletenessResult result = checkCompleteness(data, validator);
                results.add(new QualityDimensionResult(dimensionName, result.getCompleteness(), result.isPass()));
            } catch (Exception e) {
                logger.warn("Failed to check quality dimension: {}", dimensionName, e);
                results.add(new QualityDimensionResult(dimensionName, 0.0, false));
            }
        }

        return new DataQualityReport(results);
    }

    /**
     * 完整性检查结果类
     */
    public static class CompletenessResult {
        private final int total;
        private final int valid;
        private final double completeness;
        private final boolean pass;
        private final String message;

        public CompletenessResult(int total, int valid, double completeness, boolean pass, String message) {
            this.total = total;
            this.valid = valid;
            this.completeness = completeness;
            this.pass = pass;
            this.message = message;
        }

        // Getters
        public int getTotal() {
            return total;
        }

        public int getValid() {
            return valid;
        }

        public double getCompleteness() {
            return completeness;
        }

        public boolean isPass() {
            return pass;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 一致性检查结果类
     */
    public static class ConsistencyResult {
        private final int totalComparisons;
        private final int consistentComparisons;
        private final double consistency;
        private final boolean pass;
        private final String message;

        public ConsistencyResult(int totalComparisons, int consistentComparisons, double consistency, boolean pass, String message) {
            this.totalComparisons = totalComparisons;
            this.consistentComparisons = consistentComparisons;
            this.consistency = consistency;
            this.pass = pass;
            this.message = message;
        }

        // Getters
        public int getTotalComparisons() {
            return totalComparisons;
        }

        public int getConsistentComparisons() {
            return consistentComparisons;
        }

        public double getConsistency() {
            return consistency;
        }

        public boolean isPass() {
            return pass;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 质量维度结果类
     */
    public static class QualityDimensionResult {
        private final String dimensionName;
        private final double score;
        private final boolean pass;

        public QualityDimensionResult(String dimensionName, double score, boolean pass) {
            this.dimensionName = dimensionName;
            this.score = score;
            this.pass = pass;
        }

        // Getters
        public String getDimensionName() {
            return dimensionName;
        }

        public double getScore() {
            return score;
        }

        public boolean isPass() {
            return pass;
        }
    }

    /**
     * 数据质量报告类
     */
    public static class DataQualityReport {
        private final List<QualityDimensionResult> dimensionResults;
        private final double overallScore;
        private final boolean overallPass;

        public DataQualityReport(List<QualityDimensionResult> dimensionResults) {
            this.dimensionResults = dimensionResults != null ? new ArrayList<>(dimensionResults) : new ArrayList<>();

            // 计算总体得分
            double totalScore = 0;
            int passCount = 0;
            for (QualityDimensionResult result : this.dimensionResults) {
                totalScore += result.getScore();
                if (result.isPass()) {
                    passCount++;
                }
            }

            this.overallScore = this.dimensionResults.isEmpty() ? 0 : totalScore / this.dimensionResults.size();
            this.overallPass = !this.dimensionResults.isEmpty() && passCount == this.dimensionResults.size();
        }

        // Getters
        public List<QualityDimensionResult> getDimensionResults() {
            return new ArrayList<>(dimensionResults);
        }

        public double getOverallScore() {
            return overallScore;
        }

        public boolean isOverallPass() {
            return overallPass;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DataQualityReport{\n");
            sb.append("  overallScore=").append(String.format("%.2f", overallScore * 100)).append("%\n");
            sb.append("  overallPass=").append(overallPass).append("\n");
            sb.append("  dimensions=[\n");
            for (QualityDimensionResult result : dimensionResults) {
                sb.append("    ").append(result.getDimensionName())
                        .append(": ").append(String.format("%.2f", result.getScore() * 100)).append("% ")
                        .append(result.isPass() ? "PASS" : "FAIL").append("\n");
            }
            sb.append("  ]\n");
            sb.append("}");
            return sb.toString();
        }
    }
}
