package com.im.qtech.service.config.dynamic;

import java.util.Arrays;

/**
 * 数据源枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

public enum DSName {
    FIRST("first"),
    SECOND("second"),
    THIRD("third");

    private final String name;

    DSName(String name) {
        this.name = name;
    }

    public static DSName from(String name) {
        return Arrays.stream(values())
                .filter(ds -> ds.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(FIRST);
    }

    public String getName() {
        return name;
    }
}