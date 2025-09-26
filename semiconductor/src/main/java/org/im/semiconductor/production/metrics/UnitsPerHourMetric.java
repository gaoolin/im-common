package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * UPH (Units Per Hour) 指标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class UnitsPerHourMetric implements ProductionEfficiencyMetric {
    private final String equipmentId;
    private final int unitsProduced;
    private final long timePeriodMillis;
    private final LocalDateTime measurementTime;

    public UnitsPerHourMetric(String equipmentId, int unitsProduced, long timePeriodMillis) {
        this.equipmentId = equipmentId;
        this.unitsProduced = unitsProduced;
        this.timePeriodMillis = timePeriodMillis;
        this.measurementTime = LocalDateTime.now();
    }

    @Override
    public double getValue() {
        // 计算每小时生产量
        double hours = (double) timePeriodMillis / (1000 * 60 * 60);
        return hours > 0 ? unitsProduced / hours : 0;
    }

    @Override
    public String getMetricId() {
        return "UPH_" + equipmentId;
    }

    @Override
    public String getMetricName() {
        return "每小时生产量";
    }

    @Override
    public EfficiencyStatus getStatus() {
        double uph = getValue();
        if (uph >= 90) return EfficiencyStatus.EXCELLENT;
        if (uph >= 70) return EfficiencyStatus.GOOD;
        if (uph >= 50) return EfficiencyStatus.ACCEPTABLE;
        return EfficiencyStatus.POOR;
    }

    @Override
    public LocalDateTime getMeasurementTime() {
        return null;
    }

    public int getUnitsProduced() {
        return unitsProduced;
    }

    public long getTimePeriodMillis() {
        return timePeriodMillis;
    }
}
