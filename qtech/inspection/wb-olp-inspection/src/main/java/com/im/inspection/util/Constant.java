package com.im.inspection.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/04 14:52:53
 */

public class Constant {
    // 常量
    public static final int TIME_OFFSET_MINUTES = -15;
    public static final String JAR_FILE_PATH = "E:\\dossier\\etl\\transfer\\target\\transfer-2.1.1-pg-encrypted.jar";
    public static final String PUBLIC_KEY_PATH = "E:\\dossier\\others\\key-tool\\src\\main\\resources\\file\\publicKey.pem";
    public static final String TOKEN_FILE_PATH = "E:\\dossier\\others\\key-tool\\src\\main\\resources\\file\\token.dat";
    public static final String CLASS_NAME = "org.apache.tools.src";
    public static final String METHOD_NAME = "doCheck";
    // Sql
    public static final String NEED_FILTER_MODULE_TPL_SQL = "SELECT sid, module, wire_cnt, status, create_by, create_time, update_by, update_time, provider, site, workshop, remark FROM biz.eqp_wb_olp_tpl_info  WHERE status = 0";
    public static final String TPL_SQL = "SELECT a.module tpl_module, a.wire_id tpl_wire_id, a.lead_x tpl_lead_x, a.lead_y tpl_lead_y, a.pad_x tpl_pad_x, a.pad_y tpl_pad_y, a.lead_diff tpl_lead_diff, a.pad_diff tpl_pad_diff, lead_th, pad_th, a.wire_len tpl_wire_len FROM eqp_wb_olp_tpl a LEFT JOIN eqp_wb_olp_tpl_info b ON a.module = b.module WHERE b.status = 1";
    public static final String DRUID_RAW_DATA_SQL = "SELECT SIMID, ECount, receive_date, MachineType, __time, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Sendcount FROM t_dev_attrs WHERE __time > '%s' AND receive_date IS NOT NULL AND MachineType IS NOT NULL AND device_type = 'WB' AND B1 IS NOT NULL AND B1 NOT LIKE 'DIE%%' AND B1 NOT LIKE 'LEAD%%' AND B1 NOT LIKE '%%j%%' AND B1 NOT LIKE '.0%%' AND B1 NOT LIKE '%%ln%%'";
    public static final String DRUID_TRANSFORM_SQL = "SELECT SIMID as sim_id, MachineType_ as module, __time as dt, CASE WHEN split(result_b, ',')[0] IS NOT NULL AND split(result_b, ',')[0] != '' THEN CAST(split(result_b, ',')[0] AS INTEGER) ELSE 0 END AS wire_id, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[3] AS STRING) ELSE '0' END AS lead_x, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[4] AS STRING) ELSE '0' END AS lead_y, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[1] AS STRING) ELSE '0' END AS pad_x, CASE WHEN result_b IS NOT NULL AND result_b != '' THEN CAST(split(result_b, ',')[2] AS STRING) ELSE '0' END AS pad_y, ECount AS check_port, Sendcount AS pieces_index FROM (SELECT SIMID, ECount, receive_date, TRIM(MachineType) AS MachineType_, __time, Sendcount, STACK(10, 'B1', B1, 'B2', B2, 'B3', B3, 'B4', B4, 'B5', B5, 'B6', B6, 'B7', B7, 'B8', B8, 'B9', B9, 'B10', B10) AS (B, result_b) FROM t_dev_attrs) t";
    public static final String NEED_FILTER_MODULE = "select * from ttlCheckResDf where module in (select module from needFilterMcId)";
    public static final String EXCLUDE_NEED_FILTER_MODULE = "select * from ttlCheckResDf where module not in (select module from needFilterMcId)";

    // 字段常量
    public static final String SOURCE = "wb-olp";
    public static final String SIM_ID = "sim_id";
    public static final String TPL_MODULE = "tpl_module";
    public static final String MODULE = "module";
    public static final String CHK_DT = "chk_dt";
    public static final String DT = "dt";
    public static final String FIRST_DRAW_TIME = "first_draw_time";
    public static final String WIRE_ID = "wire_id";
    public static final String TPL_WIRE_ID = "tpl_wire_id";
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
    public static final String NORM_MODULE = "norm_module";
    public static final String CNT = "cnt";
    public static final String WIRE_LEN = "wire_len";
    public static final String TPL_WIRE_LEN = "tpl_wire_len";
    public static final String CODE = "code";
    public static final String DESCRIPTION = "description";

    // fullLnMkSta 相关字段
    public static final String TPL_LEAD_DIFF = "tpl_lead_diff";
    public static final String TPL_PAD_DIFF = "tpl_pad_diff";
    public static final String LEAD_THRESHOLD = "lead_th";
    public static final String PAD_THRESHOLD = "pad_th";
    public static final String LEAD_OFFSET = "lead_offset";
    public static final String PAD_OFFSET = "pad_offset";

    // 其他中间字段
    public static final String LEAD_LEN = "lead_len";
    public static final String PAD_LEN = "pad_len";
    public static final String TPL_WIRE_CNT = "tpl_wire_cnt";
    public static final String CONFIRM_LABEL = "confirm_label";
    public static final String WIRE_ID_ASC = "wire_id_asc";
    public static final String WIRE_ID_DESC = "wire_id_desc";
    public static final String WIRE_LABEL = "wire_label";
    public static final String WIRE_LEN_TTL = "wire_len_ttl";
    public static final String WIRE_LEN_TTL_RANK = "wire_len_ttl_rank";
    public static final String WIRE_ID_MOCK = "wire_id_mock";
    public static final String MODULES_BY_PIECES_INDEX = "modules_by_pieces_index";

    private static final Logger logger = LoggerFactory.getLogger(Constant.class);

    private Constant() {
        logger.error(">>>>> Constants类不允许实例化！");
    }
}
