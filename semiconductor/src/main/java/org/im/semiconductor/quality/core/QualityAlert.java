package org.im.semiconductor.quality.core;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface QualityAlert {
    String getAlertId();

    QualityAlertLevel getLevel();

    String getMessage();

    LocalDateTime getAlertTime();

    Quality getSourceQuality();

    enum QualityAlertLevel {
        INFO, WARNING, CRITICAL, FATAL
    }
}