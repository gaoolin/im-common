package org.im.semiconductor.quality.core;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface QualityReport {
    String getReportId();

    LocalDateTime getReportTime();

    Quality getQualityObject();

    List<QualityMetric> getMetrics();

    QualityStatus getOverallStatus();

    List<QualityRecommendation> getRecommendations();
}