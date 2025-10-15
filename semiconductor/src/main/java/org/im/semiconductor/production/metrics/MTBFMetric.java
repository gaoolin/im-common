package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * MTBF (Mean Time Between Failures) 指标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public class MTBFMetric implements ProductionEfficiencyMetric {
    private final String equipmentId;
    private final long totalOperatingTime;
    private final int failureCount;
    private final LocalDateTime measurementTime;

    public MTBFMetric(String equipmentId, long totalOperatingTime, int failureCount) {
        this.equipmentId = equipmentId;
        this.totalOperatingTime = totalOperatingTime;
        this.failureCount = failureCount;
        this.measurementTime = LocalDateTime.now();
    }

    @Override
    public double getValue() {
        // 计算平均故障间隔时间(小时)
        return failureCount > 0 ? (double) totalOperatingTime / (1000 * 60 * 60 * failureCount) : 0;
    }

    @Override
    public String getMetricId() {
        return "MTBF_" + equipmentId;
    }

    @Override
    public String getMetricName() {
        return "平均故障间隔时间";
    }

    @Override
    public EfficiencyStatus getStatus() {
        double mtbf = getValue();
        if (mtbf >= 100) return EfficiencyStatus.EXCELLENT;  // 100小时以上
        if (mtbf >= 50) return EfficiencyStatus.GOOD;        // 50-100小时
        if (mtbf >= 20) return EfficiencyStatus.ACCEPTABLE;  // 20-50小时
        return EfficiencyStatus.POOR;                        // 20小时以下
    }

    @Override
    public LocalDateTime getMeasurementTime() {
        return null;
    }

    public long getTotalOperatingTime() {
        return totalOperatingTime;
    }

    public int getFailureCount() {
        return failureCount;
    }
}
