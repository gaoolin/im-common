package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * 1. OEE指标归类
 * OEE (Overall Equipment Effectiveness) 应该归类为设备综合效率指标，属于生产管理模块中的核心KPI指标。
 * 2. OEE核心组成
 * OEE由三个核心要素组成：
 * Availability (可用率): 设备实际运行时间与计划运行时间的比率
 * Performance (性能效率): 实际生产速度与理论最大速度的比率
 * Quality (质量指数): 合格产品数量与总生产数量的比率
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class OEECalculationResult {
    private final double availability;     // 可用率
    private final double performance;      // 性能效率
    private final double quality;          // 质量指数
    private final double oee;              // 设备综合效率
    private final String equipmentId;
    private final LocalDateTime calculationTime;

    public OEECalculationResult(double availability, double performance, double quality,
                                String equipmentId) {
        this.availability = availability;
        this.performance = performance;
        this.quality = quality;
        this.oee = availability * performance * quality;
        this.equipmentId = equipmentId;
        this.calculationTime = LocalDateTime.now();
    }

    public double getOEE() {
        return oee;
    }

    public OEEStatus getOEEStatus() {
        if (oee >= 0.85) return OEEStatus.EXCELLENT;
        if (oee >= 0.70) return OEEStatus.GOOD;
        if (oee >= 0.60) return OEEStatus.ACCEPTABLE;
        return OEEStatus.POOR;
    }

    // Getters
    public double getAvailability() {
        return availability;
    }

    public double getPerformance() {
        return performance;
    }

    public double getQuality() {
        return quality;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public LocalDateTime getCalculationTime() {
        return calculationTime;
    }
}
