package org.im.semiconductor.production.metrics;

/**
 * OEE计算服务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class OEEMetricService {

    /**
     * 计算设备综合效率
     *
     * @param equipmentId           设备ID
     * @param plannedProductionTime 计划生产时间(分钟)
     * @param actualOperatingTime   实际运行时间(分钟)
     * @param idealCycleTime        理论循环时间(秒)
     * @param totalProduction       总生产数量
     * @param goodProduction        合格品数量
     * @return OEE计算结果
     */
    public OEECalculationResult calculateOEE(String equipmentId,
                                             double plannedProductionTime,
                                             double actualOperatingTime,
                                             double idealCycleTime,
                                             int totalProduction,
                                             int goodProduction) {

        // 1. 计算可用率
        double availability = plannedProductionTime > 0 ?
                actualOperatingTime / plannedProductionTime : 0;

        // 2. 计算性能效率
        double performance = (actualOperatingTime * 60) > 0 && idealCycleTime > 0 ?
                (totalProduction * idealCycleTime) / (actualOperatingTime * 60) : 0;

        // 3. 计算质量指数
        double quality = totalProduction > 0 ?
                (double) goodProduction / totalProduction : 0;

        return new OEECalculationResult(availability, performance, quality, equipmentId);
    }

    /**
     * 从生产统计数据计算OEE
     */
    public OEECalculationResult calculateOEEFromStats(String equipmentId,
                                                      ProductionStatistics stats) {
        return calculateOEE(
                equipmentId,
                stats.getPlannedTime(),
                stats.getActualOperatingTime(),
                stats.getIdealCycleTime(),
                stats.getTotalProduction(),
                stats.getGoodProduction()
        );
    }
}

