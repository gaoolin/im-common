package com.im.aa.inspection.entity.reverse;

/**
 * 检查记录标签枚举类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/11
 */


public enum LabelEum {
    /**
     * 正常记录
     */
    NORMAL("normal", "正常"),

    /**
     * 忽略记录
     */
    IGNORE("ignore", "忽略"),

    /**
     * 其他记录
     */
    OTHER("other", "其他");

    private final String code;
    private final String description;

    LabelEum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
