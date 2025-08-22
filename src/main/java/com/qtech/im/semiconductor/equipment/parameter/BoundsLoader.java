package com.qtech.im.semiconductor.equipment.parameter;


import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.qtech.im.common.math.MathEvaluator.isNumeric;
import static com.qtech.im.common.math.MathEvaluator.parseDoubleSafely;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/05/20 11:41:47
 * desc   :  解析数据装载工具
 */

public class BoundsLoader {
    private final String min;
    private final String max;
    private final String singleValue;

    private BoundsLoader(String min, String max, String singleValue) {
        this.min = min;
        this.max = max;
        this.singleValue = singleValue;
    }

    /**
     * @param min 小值
     * @param max 大值
     * @return com.qtech.share.aa.model.BoundsLoader
     * @decription 工厂方法
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

    public static BoundsLoader single(String value) {
        return new BoundsLoader(null, null, value);
    }

    /**
     * 判断是否包含某个值
     *
     * @param value 要检查的值
     * @return 如果值在范围或等于单个值，则返回 {@code true}；否则返回 {@code false}
     * @throws IllegalArgumentException 如果传入的值为 {@code null}
     * @throws IllegalStateException    如果边界范围不合法（即 {@code min} 大于 {@code max}）
     */
    public boolean contains(String value) {
        if (value == null) {
            return false;
        }

        if (singleValue != null) {
            // 单值模式
            return singleValue.equals(value);
        } else if (min != null && max != null) {
            // 校验 min 和 max 的合法性
            if (!validateBounds()) {
                throw new IllegalStateException("Invalid bounds: min must be less than or equal to max");
            }

            // 数字范围模式
            if (isNumeric(value) && isNumeric(min) && isNumeric(max)) {
                double v = parseDoubleSafely(value);
                double minValue = parseDoubleSafely(min);
                double maxValue = parseDoubleSafely(max);
                return v >= minValue && v <= maxValue;
            } else {
                // 字符串范围模式
                int cmpMin = value.compareTo(min);
                int cmpMax = value.compareTo(max);
                return cmpMin >= 0 && cmpMax <= 0;
            }
        } else {
            // 如果 singleValue 和 min/max 都为 null，则返回 false
            return false;
        }
    }

    /**
     * 比较单值
     * 只能比较数值，不能比较字符串
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

    private Comparable<?> convertToComparable(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue(); // 统一转换为 Double 进行比较
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0; // true -> 1, false -> 0
        }
        if (value instanceof String) {
            String str = (String) value;
            if (isNumeric(str)) {
                return Double.parseDouble(str); // 字符串数字转换为 Double
            }
            return str; // 作为普通字符串处理
        }
        throw new IllegalArgumentException("Unsupported value type for comparison: " + value.getClass().getName());
    }

    /**
     * 判断两个范围是否有交集
     */
    public boolean intersects(BoundsLoader other) {
        if (other == null || this.min == null || this.max == null || other.min == null || other.max == null) {
            return false;
        }

        if (isNumeric(this.min) && isNumeric(other.min)) {
            double thisMin = Double.parseDouble(this.min);
            double thisMax = Double.parseDouble(this.max);
            double otherMin = Double.parseDouble(other.min);
            double otherMax = Double.parseDouble(other.max);
            return thisMin <= otherMax && thisMax >= otherMin;
        } else {
            return this.min.compareTo(other.max) <= 0 && this.max.compareTo(other.min) >= 0;
        }
    }

    /**
     * 判断是否是子集
     */
    public boolean isSubsetOf(BoundsLoader other) {
        if (other == null || this.min == null || this.max == null || other.min == null || other.max == null) return false;

        if (isNumeric(this.min) && isNumeric(other.min)) {
            return Double.parseDouble(other.min) <= Double.parseDouble(this.min) && Double.parseDouble(other.max) >= Double.parseDouble(this.max);
        } else {
            return other.min.compareTo(this.min) <= 0 && other.max.compareTo(this.max) >= 0;
        }
    }

    /**
     * 判断两个范围是否相等
     *
     * @param other 另一个 BoundsLoader 对象
     * @return 如果两个对象的范围相等，则返回 {@code true}；否则返回 {@code false}
     */
    public boolean equalsRange(BoundsLoader other) {
        if (other == null) {
            return false;
        }

        if (this.min != null && this.max != null && other.min != null && other.max != null) {
            if (isNumeric(this.min) && isNumeric(this.max) && isNumeric(other.min) && isNumeric(other.max)) {
                double thisMin = parseDoubleSafely(this.min);
                double thisMax = parseDoubleSafely(this.max);
                double otherMin = parseDoubleSafely(other.min);
                double otherMax = parseDoubleSafely(other.max);
                return thisMin == otherMin && thisMax == otherMax;
            } else {
                return this.min.equals(other.min) && this.max.equals(other.max);
            }
        }

        return false;
    }

    /**
     * 校验 min 和 max 的合法性
     */
    private boolean validateBounds() {
        if (isNumeric(min) && isNumeric(max)) {
            return parseDoubleSafely(min) <= parseDoubleSafely(max);
        } else {
            return min.compareTo(max) <= 0;
        }
    }

    @Override
    public String toString() {
        return singleValue != null ? "SingleValue[" + singleValue + "]" : "BoundsLoader[min=" + min + ", max=" + max + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundsLoader that = (BoundsLoader) o;
        return Objects.equals(min, that.min) && Objects.equals(max, that.max) && Objects.equals(singleValue, that.singleValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, singleValue);
    }

    public boolean hasSingleValue() {
        return StringUtils.isNotBlank(singleValue);
    }

    public enum Operator {
        GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL
    }
}