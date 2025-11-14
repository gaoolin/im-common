package com.im.inspection.dpp.data;

/**
 * 字段名称枚举类，用于统一管理 DataFrame 列名。
 * <p>
 * 一个更优雅、类型安全的方式。相比常量字符串类，枚举可以提供更好的封装性、可读性
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/15 11:42:13
 */

public enum FieldEnum {
    // 通用字段
    SIM_ID("sim_id"),
    MODULE_ID("module_id"),
    DT("dt"),
    FIRST_DRAW_TIME("first_draw_time"),
    WIRE_ID("wire_id"),
    PIECES_INDEX("pieces_index"),
    LEAD_X("lead_x"),
    LEAD_Y("lead_y"),
    PAD_X("pad_x"),
    PAD_Y("pad_y"),
    CHECK_PORT("check_port"),
    NORM_MODULE_ID("norm_module_id"),
    CNT("cnt"),
    WIRE_LEN("wire_len"),
    CODE("code"),
    DESCRIPTION("description"),

    // fullLnMkSta 相关字段
    TPL_LEAD_DIFF("tpl_lead_diff"),
    TPL_PAD_DIFF("tpl_pad_diff"),
    LEAD_THRESHOLD("lead_th"),
    PAD_THRESHOLD("pad_th"),
    LEAD_OFFSET("lead_offset"),
    PAD_OFFSET("pad_offset"),

    // 其他中间字段
    LEAD_LEN("lead_len"),
    PAD_LEN("pad_len"),
    TPL_WIRE_CNT("tpl_wire_cnt"),
    CONFIRM_LABEL("confirm_label"),
    WIRE_ID_ASC("wire_id_asc"),
    WIRE_ID_DESC("wire_id_desc"),
    WIRE_LABEL("wire_label"),
    WIRE_LEN_TTL("wire_len_ttl"),
    WIRE_LEN_TTL_RANK("wire_len_ttl_rank"),
    WIRE_ID_MOCK("wire_id_mock");

    private final String fieldName;

    FieldEnum(String fieldName) {
        this.fieldName = fieldName;
    }

    public String get() {
        return fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}

