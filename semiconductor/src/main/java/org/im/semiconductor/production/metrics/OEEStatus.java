package org.im.semiconductor.production.metrics;

/**
 * OEE状态枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public enum OEEStatus {
    EXCELLENT("优秀", "OEE ≥ 85%"),
    GOOD("良好", "70% ≤ OEE < 85%"),
    ACCEPTABLE("可接受", "60% ≤ OEE < 70%"),
    POOR("较差", "OEE < 60%");

    private final String description;
    private final String criteria;

    OEEStatus(String description, String criteria) {
        this.description = description;
        this.criteria = criteria;
    }

    public String getDescription() {
        return description;
    }

    public String getCriteria() {
        return criteria;
    }
}