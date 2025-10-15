package org.im.semiconductor.quality.core;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface QualityMetric {
    String getMetricName();

    double getValue();

    QualityStatus getStatus();

    LocalDateTime getMeasurementTime();
}