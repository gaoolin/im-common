package com.im.qtech.chk.entity.struct;

import org.im.semiconductor.common.parameter.mgr.ParameterRange;

import static org.im.common.math.MathEvaluator.isNumeric;

/**
 * 设备参数边界加载器
 * <p>
 * 特性：
 * - 通用性：支持设备参数边界处理
 * - 规范性：遵循设备参数处理标准
 * - 专业性：提供专业的设备参数边界处理能力
 * - 灵活性：支持单值和范围模式
 * - 可靠性：确保设备参数边界处理的准确性
 * - 安全性：防止参数越界和非法操作
 * - 复用性：可被各种设备场景复用
 * - 容错性：具备良好的参数错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2024/05/20 11:41:47
 */
public final class BoundsLoader extends ParameterRange {
    private static final long serialVersionUID = 1L;

    private final String minStr;
    private final String maxStr;
    private final String singleValue;

    /**
     * 私有构造函数
     */
    private BoundsLoader(String minStr, String maxStr, String singleValue) {
        this.minStr = minStr;
        this.maxStr = maxStr;
        this.singleValue = singleValue;

        // 初始化父类数值字段
        if (singleValue == null && minStr != null && maxStr != null) {
            this.min = parseDoubleSafely(minStr);
            this.max = parseDoubleSafely(maxStr);
        }

        this.inclusiveMin = true;
        this.inclusiveMax = true;
    }

    /**
     * 工厂方法 - 创建范围模式
     */
    public static BoundsLoader of(String min, String max) {
        if (min == null || max == null) {
            throw new IllegalArgumentException("BoundsLoader boundaries cannot be null");
        }

        if (isNumeric(min) && isNumeric(max)) {
            if (Double.parseDouble(min) > Double.parseDouble(max)) {
                throw new IllegalArgumentException("Min value cannot be greater than max value");
            }
        } else if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Min value cannot be greater than max value (non-numeric)");
        }

        return new BoundsLoader(min, max, null);
    }

    /**
     * 工厂方法 - 创建单值模式
     */
    public static BoundsLoader single(String value) {
        return new BoundsLoader(null, null, value);
    }

    /**
     * 判断是否包含某个值
     */
    @Override
    public boolean contains(Object value) {
        if (value == null) {
            return false;
        }

        String valueStr = value.toString();

        if (singleValue != null) {
            // 单值模式
            return singleValue.equals(valueStr);
        } else if (minStr != null && maxStr != null) {
            // 校验边界合法性
            if (!validateBounds()) {
                throw new IllegalStateException("Invalid bounds: min must be less than or equal to max");
            }

            // 数字范围模式
            if (isNumeric(valueStr) && isNumeric(minStr) && isNumeric(maxStr)) {
                double v = parseDoubleSafely(valueStr);
                double minValue = parseDoubleSafely(minStr);
                double maxValue = parseDoubleSafely(maxStr);
                return v >= minValue && v <= maxValue;
            } else {
                // 字符串范围模式
                int cmpMin = valueStr.compareTo(minStr);
                int cmpMax = valueStr.compareTo(maxStr);
                return cmpMin >= 0 && cmpMax <= 0;
            }
        } else {
            // 如果singleValue和min/max都为null，则返回false
            return false;
        }
    }

    /**
     * 比较单值
     */
    public boolean compareSingleValue(Object value, Operator operator) {
        if (value == null || singleValue == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }

        Comparable<?> left = convertToComparable(singleValue);
        Comparable<?> right = convertToComparable(value);

        if (left == null || right == null) {
            throw new IllegalArgumentException("Unsupported value type for comparison");
        }

        @SuppressWarnings("unchecked") int comparison = ((Comparable<Object>) left).compareTo(right);

        switch (operator) {
            case GREATER_THAN:
                return comparison > 0;
            case GREATER_THAN_OR_EQUAL:
                return comparison >= 0;
            case LESS_THAN:
                return comparison < 0;
            case LESS_THAN_OR_EQUAL:
                return comparison <= 0;
            case EQUAL:
                return comparison == 0;
            case NOT_EQUAL:
                return comparison != 0;
            default:
                throw new IllegalArgumentException("Unknown comparison operator: " + operator);
        }
    }

    /**
     * 类型转换
     */
    private Comparable<?> convertToComparable(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof String) {
            String str = (String) value;
            if (isNumeric(str)) {
                return Double.parseDouble(str);
            }
            return str;
        }
        throw new IllegalArgumentException("Unsupported value type for comparison: " + value.getClass().getName());
    }

    /**
     * 判断两个范围是否有交集
     */
    @Override
    public boolean intersects(ParameterRange other) {
        if (other == null || this.minStr == null || this.maxStr == null ||
                other.getMin() == null || other.getMax() == null) {
            return false;
        }

        if (isNumeric(this.minStr) && isNumeric(other.getMin().toString())) {
            double thisMin = Double.parseDouble(this.minStr);
            double thisMax = Double.parseDouble(this.maxStr);
            double otherMin = other.getMin();
            double otherMax = other.getMax();
            return thisMin <= otherMax && thisMax >= otherMin;
        } else {
            return this.minStr.compareTo(other.getMax().toString()) <= 0 &&
                    this.maxStr.compareTo(other.getMin().toString()) >= 0;
        }
    }

    /**
     * 判断是否是子集
     */
    @Override
    public boolean isSubsetOf(ParameterRange other) {
        if (other == null || this.minStr == null || this.maxStr == null ||
                other.getMin() == null || other.getMax() == null) {
            return false;
        }

        if (isNumeric(this.minStr) && isNumeric(other.getMin().toString())) {
            double otherMin = other.getMin();
            double otherMax = other.getMax();
            return otherMin <= Double.parseDouble(this.minStr) &&
                    otherMax >= Double.parseDouble(this.maxStr);
        } else {
            return other.getMin().toString().compareTo(this.minStr) <= 0 &&
                    other.getMax().toString().compareTo(this.maxStr) >= 0;
        }
    }

    /**
     * 校验边界
     */
    private boolean validateBounds() {
        if (isNumeric(minStr) && isNumeric(maxStr)) {
            return parseDoubleSafely(minStr) <= parseDoubleSafely(maxStr);
        } else {
            return minStr.compareTo(maxStr) <= 0;
        }
    }

    /**
     * 安全解析Double
     */
    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + value, e);
        }
    }

    public boolean hasSingleValue() {
        return singleValue != null && !singleValue.isEmpty();
    }

    public String getSingleValue() {
        return singleValue;
    }

    public String getMinStr() {
        return minStr;
    }

    public String getMaxStr() {
        return maxStr;
    }

    @Override
    public String toString() {
        return singleValue != null ?
                "BoundsLoader[single=" + singleValue + "]" :
                "BoundsLoader[min=" + minStr + ", max=" + maxStr + "]";
    }

    /**
     * 操作符枚举
     */
    public enum Operator {
        GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL
    }
}