package com.qtech.im.semiconductor.equipment.parameter.mgr;

import java.io.Serializable;
import java.util.Objects;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:26:30
 */

/**
 * 参数范围抽象基类
 * <p>
 * 特性：
 * - 通用性：支持各种参数范围场景
 * - 规范性：定义标准的范围操作接口
 * - 专业性：提供专业的范围处理能力
 * - 灵活性：支持自定义范围实现
 * - 可靠性：确保范围处理的稳定性
 * - 安全性：防止范围越界和非法操作
 * - 复用性：可被各种范围场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public abstract class ParameterRange implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Double min;
    protected Double max;
    protected Double step;
    protected Boolean inclusiveMin;
    protected Boolean inclusiveMax;

    /**
     * 默认构造函数
     */
    public ParameterRange() {
        this.inclusiveMin = true;
        this.inclusiveMax = true;
    }

    /**
     * 带范围的构造函数
     */
    public ParameterRange(Double min, Double max) {
        this.min = min;
        this.max = max;
        this.inclusiveMin = true;
        this.inclusiveMax = true;
        validateRange();
    }

    /**
     * 验证范围的有效性
     */
    protected void validateRange() {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
    }

    /**
     * 检查值是否在范围内 - 抽象方法，由子类实现
     */
    public abstract boolean contains(Object value);

    /**
     * 检查值是否在数值范围内 - 提供默认实现
     */
    protected boolean containsNumeric(double value) {
        boolean result = true;

        if (min != null) {
            if (inclusiveMin != null && inclusiveMin) {
                result = result && (value >= min);
            } else {
                result = result && (value > min);
            }
        }

        if (max != null) {
            if (inclusiveMax != null && inclusiveMax) {
                result = result && (value <= max);
            } else {
                result = result && (value < max);
            }
        }

        return result;
    }

    /**
     * 判断两个范围是否有交集
     */
    public boolean intersects(ParameterRange other) {
        if (other == null || this.min == null || this.max == null ||
                other.getMin() == null || other.getMax() == null) {
            return false;
        }

        double thisMin = this.min;
        double thisMax = this.max;
        double otherMin = other.getMin();
        double otherMax = other.getMax();

        return thisMin <= otherMax && thisMax >= otherMin;
    }

    /**
     * 判断是否是子集
     */
    public boolean isSubsetOf(ParameterRange other) {
        if (other == null || this.min == null || this.max == null ||
                other.getMin() == null || other.getMax() == null) {
            return false;
        }

        double thisMin = this.min;
        double thisMax = this.max;
        double otherMin = other.getMin();
        double otherMax = other.getMax();

        return otherMin <= thisMin && otherMax >= thisMax;
    }

    /**
     * 判断两个范围是否相等
     */
    public boolean equalsRange(ParameterRange other) {
        if (other == null) {
            return false;
        }

        if (this.min == null && other.getMin() == null) {
            return this.max == null ? other.getMax() == null : this.max.equals(other.getMax());
        }

        if (this.max == null && other.getMax() == null) {
            return this.min.equals(other.getMin());
        }

        return (this.min == null ? other.getMin() == null : this.min.equals(other.getMin())) &&
                (this.max == null ? other.getMax() == null : this.max.equals(other.getMax()));
    }

    // Getters and Setters
    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
        validateRange();
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
        validateRange();
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public Boolean isInclusiveMin() {
        return inclusiveMin;
    }

    public void setInclusiveMin(Boolean inclusiveMin) {
        this.inclusiveMin = inclusiveMin;
    }

    public Boolean isInclusiveMax() {
        return inclusiveMax;
    }

    public void setInclusiveMax(Boolean inclusiveMax) {
        this.inclusiveMax = inclusiveMax;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "min=" + min +
                ", max=" + max +
                ", step=" + step +
                ", inclusiveMin=" + inclusiveMin +
                ", inclusiveMax=" + inclusiveMax +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterRange that = (ParameterRange) o;
        return Objects.equals(min, that.min) &&
                Objects.equals(max, that.max) &&
                Objects.equals(step, that.step) &&
                Objects.equals(inclusiveMin, that.inclusiveMin) &&
                Objects.equals(inclusiveMax, that.inclusiveMax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, step, inclusiveMin, inclusiveMax);
    }
}
