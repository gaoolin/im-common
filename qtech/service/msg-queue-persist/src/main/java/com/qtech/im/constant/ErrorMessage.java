package com.qtech.im.constant;

/**
 * 错误消息常量类
 * <p>
 * 定义系统中所有错误码对应的默认消息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 13:42:40
 */
public interface ErrorMessage {

    // 系统级错误消息
    String SYS_UNKNOWN_ERROR = "系统未知错误";
    String SYS_CONFIG_ERROR = "系统配置错误";
    String SYS_INIT_ERROR = "系统初始化失败";
    String SYS_IO_ERROR = "系统IO异常";
    String SYS_NETWORK_ERROR = "网络连接异常";

    // 业务级错误消息
    String BIZ_PARAM_INVALID = "参数校验失败";
    String BIZ_DATA_NOT_FOUND = "数据不存在";
    String BIZ_DATA_EXISTS = "数据已存在";
    String BIZ_PERMISSION_DENIED = "权限不足";
    String BIZ_OPERATION_NOT_ALLOWED = "操作不被允许";

    // 认证授权错误消息
    String AUTH_LOGIN_FAILED = "登录失败";
    String AUTH_TOKEN_EXPIRED = "认证令牌已过期";
    String AUTH_TOKEN_INVALID = "认证令牌无效";
    String AUTH_ACCESS_DENIED = "访问被拒绝";

    // 数据库错误消息
    String DB_CONNECTION_FAILED = "数据库连接失败";
    String DB_QUERY_ERROR = "数据库查询异常";
    String DB_UPDATE_ERROR = "数据库更新异常";
    String DB_DELETE_ERROR = "数据库删除异常";
    String DB_INSERT_ERROR = "数据库插入异常";

    // 网络错误消息
    String NET_CONNECTION_FAILED = "网络连接失败";
    String NET_REQUEST_TIMEOUT = "网络请求超时";
    String NET_REQUEST_ERROR = "网络请求异常";
    String NET_RESPONSE_ERROR = "网络响应异常";
    String NET_REQUEST_NOT_FOUND = "网络请求未找到";
    String NET_REQUEST_METHOD_NOT_ALLOWED = "网络请求方法不允许";

    // 第三方服务错误消息
    String EXT_SERVICE_UNAVAILABLE = "第三方服务不可用";
    String EXT_SERVICE_TIMEOUT = "第三方服务调用超时";
    String EXT_SERVICE_ERROR = "第三方服务调用异常";

    // 丘钛设备点检错误消息
    String QT_CHECK_OK = "OK";
    String QT_CHECK_NG = "NG";
}