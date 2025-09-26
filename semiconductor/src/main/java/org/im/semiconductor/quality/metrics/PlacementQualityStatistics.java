package org.im.semiconductor.quality.metrics;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public class PlacementQualityStatistics {
    private int totalAttempts;           // 总贴装尝试次数
    private int successfulPlacements;    // 成功贴装次数
    private int dropCount;               // 抛料次数
    private int pickupFailures;          // 吸料失败次数
    private int consecutiveDrops;        // 连续抛料次数
    private List<PlacementError> errors; // 错误记录

    // 计算抛料率
    public double calculateDropRate() {
        return totalAttempts > 0 ? (double) dropCount / totalAttempts : 0.0;
    }

    // 计算吸料失败率
    public double calculatePickupFailureRate() {
        return totalAttempts > 0 ? (double) pickupFailures / totalAttempts : 0.0;
    }

    // 计算贴装准确率
    public double calculatePlacementAccuracyRate() {
        return totalAttempts > 0 ? (double) successfulPlacements / totalAttempts : 0.0;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getSuccessfulPlacements() {
        return successfulPlacements;
    }

    public void setSuccessfulPlacements(int successfulPlacements) {
        this.successfulPlacements = successfulPlacements;
    }

    public int getDropCount() {
        return dropCount;
    }

    public void setDropCount(int dropCount) {
        this.dropCount = dropCount;
    }

    public int getPickupFailures() {
        return pickupFailures;
    }

    public void setPickupFailures(int pickupFailures) {
        this.pickupFailures = pickupFailures;
    }

    public int getConsecutiveDrops() {
        return consecutiveDrops;
    }

    public void setConsecutiveDrops(int consecutiveDrops) {
        this.consecutiveDrops = consecutiveDrops;
    }

    public List<PlacementError> getErrors() {
        return errors;
    }

    public void setErrors(List<PlacementError> errors) {
        this.errors = errors;
    }
}
