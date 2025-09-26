package org.im.semiconductor.production.metrics;

import java.time.LocalDateTime;

/**
 * 核心生产效率指标
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */


public interface ProductionEfficiencyMetric {
    String getMetricId();

    String getMetricName();

    double getValue();

    EfficiencyStatus getStatus();

    LocalDateTime getMeasurementTime();
}