package org.im.semiconductor.quality.core;

import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface Quality {
    String getId();

    String getName();

    QualityType getType();

    LocalDateTime getCreateTime();
}
