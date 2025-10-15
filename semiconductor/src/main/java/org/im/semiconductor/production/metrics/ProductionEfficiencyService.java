package org.im.semiconductor.production.metrics;

/**
 * 生产效率统计服务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public class ProductionEfficiencyService {

    public UnitsPerHourMetric calculateUPH(String equipmentId, int unitsProduced, long timePeriodMillis) {
        return new UnitsPerHourMetric(equipmentId, unitsProduced, timePeriodMillis);
    }

    public MTBFMetric calculateMTBF(String equipmentId, long totalOperatingTime, int failureCount) {
        return new MTBFMetric(equipmentId, totalOperatingTime, failureCount);
    }

    public MTTRMetric calculateMTTR(String equipmentId, long totalRepairTime, int repairCount) {
        return new MTTRMetric(equipmentId, totalRepairTime, repairCount);
    }

    public EquipmentAvailabilityMetric calculateAvailability(String equipmentId, long scheduledTime, long downtime) {
        return new EquipmentAvailabilityMetric(equipmentId, scheduledTime, downtime);
    }

    public double calculateOEE(double availability, double performance, double quality) {
        return availability * performance * quality;
    }
}

