package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * 设备可用率指标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public class EquipmentAvailabilityMetric implements ProductionEfficiencyMetric {
    private final String equipmentId;
    private final long scheduledTime;
    private final long downtime;
    private final LocalDateTime measurementTime;

    public EquipmentAvailabilityMetric(String equipmentId, long scheduledTime, long downtime) {
        this.equipmentId = equipmentId;
        this.scheduledTime = scheduledTime;
        this.downtime = downtime;
        this.measurementTime = LocalDateTime.now();
    }

    @Override
    public double getValue() {
        // 计算设备可用率(%)
        return scheduledTime > 0 ? ((double) (scheduledTime - downtime) / scheduledTime) * 100 : 0;
    }

    @Override
    public String getMetricId() {
        return "AVAILABILITY_" + equipmentId;
    }

    @Override
    public String getMetricName() {
        return "设备可用率";
    }

    @Override
    public EfficiencyStatus getStatus() {
        double availability = getValue();
        if (availability >= 95) return EfficiencyStatus.EXCELLENT;
        if (availability >= 90) return EfficiencyStatus.GOOD;
        if (availability >= 85) return EfficiencyStatus.ACCEPTABLE;
        return EfficiencyStatus.POOR;
    }

    @Override
    public LocalDateTime getMeasurementTime() {
        return null;
    }

    public double getAvailabilityPercentage() {
        return getValue();
    }
}

