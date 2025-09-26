package org.im.semiconductor.production.metrics;

/**
 * 效率状态枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public enum EfficiencyStatus {
    EXCELLENT("优秀"),
    GOOD("良好"),
    ACCEPTABLE("可接受"),
    POOR("较差");

    private final String description;

    EfficiencyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

