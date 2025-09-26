package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * 生产统计数据类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class ProductionStatistics {
    private final double plannedTime;          // 计划时间(分钟)
    private final double actualOperatingTime;  // 实际运行时间(分钟)
    private final double idealCycleTime;       // 理论循环时间(秒)
    private final int totalProduction;         // 总生产数量
    private final int goodProduction;          // 合格品数量
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    // 构造函数和getter方法
    public ProductionStatistics(double plannedTime, double actualOperatingTime,
                                double idealCycleTime, int totalProduction,
                                int goodProduction, LocalDateTime startTime,
                                LocalDateTime endTime) {
        this.plannedTime = plannedTime;
        this.actualOperatingTime = actualOperatingTime;
        this.idealCycleTime = idealCycleTime;
        this.totalProduction = totalProduction;
        this.goodProduction = goodProduction;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public double getPlannedTime() {
        return plannedTime;
    }

    public double getActualOperatingTime() {
        return actualOperatingTime;
    }

    public double getIdealCycleTime() {
        return idealCycleTime;
    }

    public int getTotalProduction() {
        return totalProduction;
    }

    public int getGoodProduction() {
        return goodProduction;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
