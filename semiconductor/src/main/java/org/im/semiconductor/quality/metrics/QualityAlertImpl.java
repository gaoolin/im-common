package org.im.semiconductor.quality.metrics;

import org.im.semiconductor.quality.core.Quality;
import org.im.semiconductor.quality.core.QualityAlert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public class QualityAlertImpl implements QualityAlert {
    private final String alertId;
    private final String alertType;
    private final QualityAlertLevel level;
    private final String message;
    private final LocalDateTime alertTime;
    private final Map<String, Object> details;

    public QualityAlertImpl(String alertType, QualityAlertLevel level, String message) {
        this.alertId = UUID.randomUUID().toString();
        this.alertType = alertType;
        this.level = level;
        this.message = message;
        this.alertTime = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    @Override
    public String getAlertId() {
        return alertId;
    }

    @Override
    public QualityAlertLevel getLevel() {
        return level;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public LocalDateTime getAlertTime() {
        return alertTime;
    }

    @Override
    public Quality getSourceQuality() {
        return null;
    }
}
