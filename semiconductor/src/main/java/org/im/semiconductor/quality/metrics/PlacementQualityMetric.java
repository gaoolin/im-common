package org.im.semiconductor.quality.metrics;

import org.im.semiconductor.quality.core.QualityMetric;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface PlacementQualityMetric extends QualityMetric {
    /**
     * 抛料率计算
     *
     * @return 抛料率 (抛料数量/总贴装尝试数量)
     */
    double getDropRate();

    /**
     * 吸料失败率
     *
     * @return 吸料失败率 (吸料失败次数/总吸料尝试次数)
     */
    double getPickupFailureRate();

    /**
     * 贴装准确率
     *
     * @return 贴装准确率 (准确贴装数量/总贴装数量)
     */
    double getPlacementAccuracyRate();

    /**
     * 连续抛料次数
     *
     * @return 连续抛料次数，用于异常检测
     */
    int getConsecutiveDropCount();
}
