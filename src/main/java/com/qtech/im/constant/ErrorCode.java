package com.qtech.im.constant;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 13:42:03
 * desc   :
 * 错误码接口
 * <p>
 * 定义系统中所有可能的错误码
 */
public interface ErrorCode {

    // 系统级错误码 (SYS_xxx)
    String SYS_UNKNOWN_ERROR = "SYS_0001";
    String SYS_CONFIG_ERROR = "SYS_0002";
    String SYS_INIT_ERROR = "SYS_0003";
    String SYS_IO_ERROR = "SYS_0004";
    String SYS_NETWORK_ERROR = "SYS_0005";

    // 业务级错误码 (BIZ_xxx)
    String BIZ_PARAM_INVALID = "BIZ_0001";
    String BIZ_DATA_NOT_FOUND = "BIZ_0002";
    String BIZ_DATA_EXISTS = "BIZ_0003";
    String BIZ_PERMISSION_DENIED = "BIZ_0004";
    String BIZ_OPERATION_NOT_ALLOWED = "BIZ_0005";

    // 认证授权错误码 (AUTH_xxx)
    String AUTH_LOGIN_FAILED = "AUTH_0001";
    String AUTH_TOKEN_EXPIRED = "AUTH_0002";
    String AUTH_TOKEN_INVALID = "AUTH_0003";
    String AUTH_ACCESS_DENIED = "AUTH_0004";

    // 数据库错误码 (DB_xxx)
    String DB_CONNECTION_FAILED = "DB_0001";
    String DB_QUERY_ERROR = "DB_0002";
    String DB_UPDATE_ERROR = "DB_0003";
    String DB_DELETE_ERROR = "DB_0004";
    String DB_INSERT_ERROR = "DB_0005";

    // 第三方服务错误码 (EXT_xxx)
    String EXT_SERVICE_UNAVAILABLE = "EXT_0001";
    String EXT_SERVICE_TIMEOUT = "EXT_0002";
    String EXT_SERVICE_ERROR = "EXT_0003";

    // 网络错误码 (NET_xxx)
    String NET_CONNECTION_FAILED = "NET_0001";
    String NET_REQUEST_TIMEOUT = "NET_0002";
    String NET_REQUEST_ERROR = "NET_0003";
    String NET_RESPONSE_ERROR = "NET_0004";
    String NET_REQUEST_NOT_FOUND = "NET_0005";
    String NET_REQUEST_METHOD_NOT_ALLOWED = "NET_0006";

    // 丘钛设备点检错误码（针对丘钛MES）
    int QT_CHECK_OK = 200;
    int QT_CHECK_NG = 302;
}