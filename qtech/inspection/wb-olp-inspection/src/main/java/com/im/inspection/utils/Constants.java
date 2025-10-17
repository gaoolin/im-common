package com.im.inspection.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/04 14:52:53
 */

public class Constants {
    // 常量
    public static final int TIME_OFFSET_MINUTES = -15;
    public static final String JAR_FILE_PATH = "E:\\dossier\\etl\\transfer\\target\\transfer-2.1.1-pg-encrypted.jar";
    public static final String PUBLIC_KEY_PATH = "E:\\dossier\\others\\key-tool\\src\\main\\resources\\file\\publicKey.pem";
    public static final String TOKEN_FILE_PATH = "E:\\dossier\\others\\key-tool\\src\\main\\resources\\file\\token.dat";
    public static final String CLASS_NAME = "org.apache.tools.Srv";
    public static final String METHOD_NAME = "doCheck";
    // Sql
    public static final String NEED_FILTER_MC_ID_STD_MOD_SQL = "SELECT SID, MC_ID, LINE_COUNT, STATUS, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME, PROVIDER, FACTORY, REMARK FROM IMBIZ.WB_OLP_STD_MODEL_INFO  WHERE STATUS = 0";
    public static final String STD_MOD_SQL = "SELECT a.MC_ID STD_MC_ID, LINE_NO STD_LINE_NO, LEAD_X STD_LEAD_X, LEAD_Y STD_LEAD_Y, PAD_X STD_PAD_X, PAD_Y STD_PAD_Y, LEAD_DIFF STD_LEAD_DIFF, PAD_DIFF STD_PAD_DIFF, LEAD_THRESHOLD, PAD_THRESHOLD, WIRE_LEN STD_WIRE_LEN FROM IMBIZ.WB_OLP_STD_MODEL_DETAIL a LEFT JOIN IMBIZ.WB_OLP_STD_MODEL_INFO b ON a.MC_ID = b.MC_ID WHERE b.STATUS = 1";
    public static final String DRUID_RAW_DATA_SQL = "SELECT SIMID, ECount, receive_date, MachineType, __time, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Sendcount FROM t_dev_attrs WHERE __time > '%s' AND receive_date IS NOT NULL AND MachineType IS NOT NULL AND device_type = 'WB' AND B1 IS NOT NULL AND B1 NOT LIKE 'DIE%%' AND B1 NOT LIKE 'LEAD%%' AND B1 NOT LIKE '%%j%%' AND B1 NOT LIKE '.0%%' AND B1 NOT LIKE '%%ln%%'";
    public static final String DRUID_TRANSFORM_SQL = "SELECT SIMID as sim_id, MachineType_ as mc_id, __time as dt, CASE WHEN split(result_b, ',')[0] IS NOT NULL AND split(result_b, ',')[0] != '' THEN CAST(split(result_b, ',')[0] AS INTEGER) ELSE 0 END AS line_no, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[3] AS STRING) ELSE '0' END AS lead_x, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[4] AS STRING) ELSE '0' END AS lead_y, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[1] AS STRING) ELSE '0' END AS pad_x, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[2] AS STRING) ELSE '0' END AS pad_y, ECount AS check_port, Sendcount AS pieces_index FROM (SELECT SIMID, ECount, receive_date, TRIM(MachineType) AS MachineType_, __time, Sendcount, STACK(10, 'B1', B1, 'B2', B2, 'B3', B3, 'B4', B4, 'B5', B5, 'B6', B6, 'B7', B7, 'B8', B8, 'B9', B9, 'B10', B10) AS (B, result_b) FROM t_dev_attrs) t";
    public static final String NEED_FILTER_MC_ID = "select * from ttlCheckResDf where mc_id in (select mc_id from needFilterMcId)";
    public static final String EXCLUDE_NEED_FILTER_MC_ID = "select * from ttlCheckResDf where mc_id not in (select mc_id from needFilterMcId)";

    // 字段常量
    public static final String SIM_ID = "sim_id";
    public static final String STD_SIM_ID = "std_mc_id";
    public static final String PROD_TYPE = "mc_id";
    public static final String DT = "dt";
    public static final String FIRST_DRAW_TIME = "first_draw_time";
    public static final String LINE_NO = "line_no";
    public static final String STD_LINE_NO = "std_line_no";
    public static final String PIECES_INDEX = "pieces_index";
    public static final String LEAD_X = "lead_x";
    public static final String LEAD_Y = "lead_y";
    public static final String PAD_X = "pad_x";
    public static final String PAD_Y = "pad_y";
    public static final String LEAD_X_LAG = "lead_x_lag";
    public static final String LEAD_Y_LAG = "lead_y_lag";
    public static final String PAD_X_LAG = "pad_x_lag";
    public static final String PAD_Y_LAG = "pad_y_lag";
    public static final String CHECK_PORT = "check_port";
    public static final String SUB_PROD_TYPE = "sub_mc_id";
    public static final String CNT = "cnt";
    public static final String WIRE_LEN = "wire_len";
    public static final String STD_WIRE_LEN = "std_wire_len";
    public static final String CODE = "code";
    public static final String DESCRIPTION = "description";

    // fullLnMkSta 相关字段
    public static final String STD_LEAD_DIFF = "std_lead_diff";
    public static final String STD_PAD_DIFF = "std_pad_diff";
    public static final String LEAD_THRESHOLD = "lead_threshold";
    public static final String PAD_THRESHOLD = "pad_threshold";
    public static final String LEAD_OFFSET = "lead_offset";
    public static final String PAD_OFFSET = "pad_offset";

    // 其他中间字段
    public static final String LEAD_LEN = "lead_len";
    public static final String PAD_LEN = "pad_len";
    public static final String STD_MOD_LINE_CNT = "std_mod_line_cnt";
    public static final String CONFIRM_LABEL = "confirm_label";
    public static final String LINE_NO_ASC = "line_no_asc";
    public static final String LINE_NO_DESC = "line_no_desc";
    public static final String WIRE_LABEL = "wire_label";
    public static final String WIRE_LEN_TTL = "wire_len_ttl";
    public static final String WIRE_LEN_TTL_RANK = "wire_len_ttl_rank";
    public static final String LINE_NO_MOCK = "line_no_mock";
    public static final String MCS_BY_PIECES_INDEX = "mcs_by_pieces_index";

    private static final Logger logger = LoggerFactory.getLogger(Constants.class);

    private Constants() {
        logger.error("Constants类不允许实例化！");
    }
}
