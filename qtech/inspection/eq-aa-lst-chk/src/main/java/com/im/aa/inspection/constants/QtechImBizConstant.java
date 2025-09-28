package com.im.aa.inspection.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/22 09:39:59
 */

public class QtechImBizConstant {
    // COMMON_CONSTANT

    // KAFKA_CONSTANT
    public static final String KAFKA_TOPIC = "qtech_im_wb_olp_chk_topic";
    public static final String WB_OLP_CHECK_KAFKA_TOPIC = "qtech_im_wb_olp_chk_topic";
    public static final String WB_OLP_RAW_DATA_KAFKA_TOPIC = "qtech_im_wb_olp_raw_data_topic";
    public static final String EQ_ONLINE_STATUS_KAFKA_TOPIC = "filtered_device_data";
    public static final String KAFKA_WB_OLP_CHK_RES_TOPIC = "qtech_im_wb_olp_chk_topic";
    public static final String KAFKA_WB_OLP_RAW_DATA_TOPIC = "qtech_im_wb_olp_raw_data_topic";
    public static final String KAFKA_BOOTSTRAP_SERVERS = "10.170.6.24:9092,10.170.6.25:9092,10.170.6.26:9092";


    // REDIS_CONSTANT
    public static final String REDIS_OLP_CHECK_DUPLICATION_KEY_PREFIX = "entity:im:olp_chk:";
    public static final String REDIS_OLP_RAW_DUPLICATION_KEY_PREFIX = "entity:im:olp_raw:";
    public static final String EQ_REVERSE_CTRL_INFO_REDIS_KEY_PREFIX = "entity:im:chk:reverse:";
    public static final String REDIS_COMPARISON_MODEL_KEY_PREFIX = "qtech:im:aa:list:xxx:";
    public static final String REDIS_COMPARISON_MODEL_INFO_KEY_SUFFIX = "qtech:im:aa:list:xxx:info:";
    public static final String EQ_REVERSE_IGNORE_SIM_PREFIX = "qtech:im:chk:ignored:";
    public static final String REDIS_JOB_RUN_DT_KEY_PREFIX = "entity:datadev:job:run:dt:";
    public static final String REDIS_JOB_RUN_STAT_KEY_PREFIX = "entity:datadev:job:run:stat:";
    public static final String WB_OLP_CHECK_REDIS_KEY_PREFIX = "entity:chk:olp:check:";
    public static final String WB_COMPARISON_REDIS_JOB_STAT_KEY_PREFIX = "chk:comparison:job:";
    public static final String DEVICE_ONLINE_STATUS_REDIS_KEY_PREFIX = "entity:im:eqp:online:";
    public static final int DEVICE_ONLINE_STATUS_REDIS_EXPIRE_SECONDS = 60;
    public static final Long MSG_WB_OLP_REDIS_EXPIRE_SECONDS = 900L; // 15分钟
    public static final String MSG_WB_OLP_KEY_PREFIX = "entity:im:chk:wb:olp:";

    // AA_LIST_CONSTANT
    // 聚合mtfCheck 命令，需和实体类属性的命名一致
    public static final Set<String> AGG_MTF_CHECK_ITEMS_FILTER_PREFIX = new HashSet<>(Arrays.asList("MTF_Check", "MTFOffAxisCheck"));
    public static final String AGG_MTF_CHECK_ITEMS_RESULT_SUFFIX = "F";
    public static final String EPOXY_INSPECTION_AUTO_INTERVAL_MIN = "5";
    public static final String EPOXY_INSPECTION_AUTO_INTERVAL_MAX = "30";
    public static final String AA_LIST_PARAM_RAW_DATA_SIMID_FILED = "OpCode";
    public static final String AA_LIST_PARAM_RAW_DATA_PROD_TYPE_FILED = "WoCode";
    public static final String AA_LIST_PARAM_RAW_DATA_HEX_FILED = "FactoryName";

    // SERVICE_API 常量
    // REVERSE_CONSTANT
    public static final int EQ_REVERSE_CTRL_INFO_RESPONSE_MSG_LENGTH = 100;
    // CEPH_CONSTANT
    public static final String CEPH_HTTP_URL = "http://im-s3-ceph-svc.qtech-im-api:8080/s3/files/upload/json?bucketName=%s&fileName=%s";
    public static final String CEPH_HTTP_URL_TEST = "http://localhost:8080/s3/files/upload/json?bucketName=%s&fileName=%s";
    public static final String CEPH_HTTP_URL_DEV = "http://10.170.6.40:31555/s3/files/upload/json?bucketName=%s&fileName=%s";
    public static final String CEPH_HTTP_URL_PROD = "http://im-s3-ceph-svc.qtech-im-api:8080/s3/files/upload/json?bucketName=%s&fileName=%s";
    public static final String OCR_HTTP_URL = "http://im-ocr-label-app-svc.qtech-im-api:5000/ocr/label";
    public static final String OCR_HTTP_URL_TEST = "http://127.0.0.1:5000/ocr/label";
    public static final String OCR_HTTP_URL_DEV = "http://10.170.6.40:30113/ocr/label";
    public static final String OCR_HTTP_URL_PROD = "http://im-ocr-label-app-svc.qtech-im-api:5000/ocr/label";


    private QtechImBizConstant() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }
}
