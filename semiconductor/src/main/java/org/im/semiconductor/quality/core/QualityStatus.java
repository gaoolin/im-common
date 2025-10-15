package org.im.semiconductor.quality.core;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public enum QualityStatus {
    EXCELLENT("优秀"),
    GOOD("良好"),
    ACCEPTABLE("可接受"),
    WARNING("警告"),
    CRITICAL("严重");

    private final String description;

    QualityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
