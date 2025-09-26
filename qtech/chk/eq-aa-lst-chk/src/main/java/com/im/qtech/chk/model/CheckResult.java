package com.im.qtech.chk.model;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */

import org.im.semiconductor.equipment.parameter.comparator.ComparisonResult;

/**
 * 检查结果模型
 */
public class CheckResult extends ComparisonResult {
    private String equipmentId;
    private String paramType;
    private String paramValue;
    private boolean passed;
    private String reason;
    private long checkTime;
    private String standardMin;
    private String standardMax;

    // Getters and Setters
    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public String getStandardMin() {
        return standardMin;
    }

    public void setStandardMin(String standardMin) {
        this.standardMin = standardMin;
    }

    public String getStandardMax() {
        return standardMax;
    }

    public void setStandardMax(String standardMax) {
        this.standardMax = standardMax;
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "equipmentId='" + equipmentId + '\'' +
                ", paramType='" + paramType + '\'' +
                ", paramValue='" + paramValue + '\'' +
                ", passed=" + passed +
                ", reason='" + reason + '\'' +
                ", checkTime=" + checkTime +
                ", standardMin='" + standardMin + '\'' +
                ", standardMax='" + standardMax + '\'' +
                '}';
    }
}