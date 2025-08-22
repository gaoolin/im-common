package com.qtech.im.semiconductor.equipment.parameter.command;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:26:30
 * desc   :
 */

import java.io.Serializable;

/**
 * 参数范围类
 */
public class ParameterRange implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double min;
    private Double max;
    private Double step;
    private Boolean inclusiveMin;
    private Boolean inclusiveMax;

    public ParameterRange() {
        this.inclusiveMin = true;
        this.inclusiveMax = true;
    }

    public ParameterRange(Double min, Double max) {
        this.min = min;
        this.max = max;
        this.inclusiveMin = true;
        this.inclusiveMax = true;
    }

    // Getters and Setters
    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
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

    /**
     * 检查值是否在范围内
     */
    public boolean contains(double value) {
        if (min != null && value < min) {
            return false;
        }
        if (max != null && value > max) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ParameterRange{" +
                "min=" + min +
                ", max=" + max +
                ", step=" + step +
                ", inclusiveMin=" + inclusiveMin +
                ", inclusiveMax=" + inclusiveMax +
                '}';
    }
}
