package org.im.semiconductor.quality.metrics;


import org.im.semiconductor.quality.core.QualityAlert;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public class DropDetectionService {
    private static final double DROP_RATE_THRESHOLD = 0.05; // 5%抛料率阈值
    private static final int CONSECUTIVE_DROP_THRESHOLD = 3; // 连续抛料阈值

    public QualityAlert detectDropAnomalies(PlacementQualityStatistics stats) {
        if (stats.calculateDropRate() > DROP_RATE_THRESHOLD) {
            return new QualityAlertImpl(
                    "HIGH_DROP_RATE",
                    QualityAlert.QualityAlertLevel.WARNING,
                    "抛料率过高: " + String.format("%.2f%%", stats.calculateDropRate() * 100)
            );
        }

        if (stats.getConsecutiveDrops() >= CONSECUTIVE_DROP_THRESHOLD) {
            return new QualityAlertImpl(
                    "CONSECUTIVE_DROPS",
                    QualityAlert.QualityAlertLevel.CRITICAL,
                    "检测到连续抛料: " + stats.getConsecutiveDrops() + "次"
            );
        }

        return null;
    }
}
