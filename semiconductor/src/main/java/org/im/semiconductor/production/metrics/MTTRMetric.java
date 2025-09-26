package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * MTTR (Mean Time To Repair) 指标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class MTTRMetric implements ProductionEfficiencyMetric {
    private final String equipmentId;
    private final long totalRepairTime;
    private final int repairCount;
    private final LocalDateTime measurementTime;

    public MTTRMetric(String equipmentId, long totalRepairTime, int repairCount) {
        this.equipmentId = equipmentId;
        this.totalRepairTime = totalRepairTime;
        this.repairCount = repairCount;
        this.measurementTime = LocalDateTime.now();
    }

    @Override
    public double getValue() {
        // 计算平均修复时间(小时)
        return repairCount > 0 ? (double) totalRepairTime / (1000 * 60 * 60 * repairCount) : 0;
    }

    @Override
    public String getMetricId() {
        return "MTTR_" + equipmentId;
    }

    @Override
    public String getMetricName() {
        return "平均修复时间";
    }

    @Override
    public EfficiencyStatus getStatus() {
        double mttr = getValue();
        if (mttr <= 1) return EfficiencyStatus.EXCELLENT;    // 1小时以内
        if (mttr <= 3) return EfficiencyStatus.GOOD;         // 1-3小时
        if (mttr <= 6) return EfficiencyStatus.ACCEPTABLE;    // 3-6小时
        return EfficiencyStatus.POOR;                        // 6小时以上
    }

    @Override
    public LocalDateTime getMeasurementTime() {
        return null;
    }

    public long getTotalRepairTime() {
        return totalRepairTime;
    }

    public int getRepairCount() {
        return repairCount;
    }
}

