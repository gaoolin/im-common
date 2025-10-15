package org.im.semiconductor.quality.core;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public enum QualityType {
    PROCESS_CONTROL("过程控制"),      // SPC相关
    DEFECT_ANALYSIS("缺陷分析"),      // 缺陷相关
    WAFER_QUALITY("晶圆质量"),       // 晶圆图相关
    RELIABILITY("可靠性分析"),       // 可靠性相关
    YIELD_ANALYSIS("良率分析");      // 良率相关

    private final String description;

    QualityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}