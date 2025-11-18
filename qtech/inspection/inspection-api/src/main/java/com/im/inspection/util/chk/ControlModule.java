package com.im.inspection.util.chk;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/18
 */

public enum ControlModule {
    AA_LIST("aa-list"),
    WB_OLP("wb-olp");

    private final String value;

    ControlModule(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
