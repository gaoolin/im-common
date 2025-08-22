package com.qtech.im.util.http;

import java.util.Objects;

/**
 * HTTP状态码枚举类
 * <p>
 * 特性：
 * - 通用性：支持所有标准HTTP状态码
 * - 规范性：遵循RFC 7231、RFC 6585等标准
 * - 专业性：提供状态码分类和描述信息
 * - 灵活性：支持扩展自定义状态码
 * - 可靠性：提供完整的状态码验证机制
 * - 安全性：提供状态码安全级别分类
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class HttpStatus {
    // 1xx Informational（信息性状态码）
    public static final HttpStatus CONTINUE = new HttpStatus(100, "Continue", Category.INFORMATIONAL, false);
    public static final HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(101, "Switching Protocols", Category.INFORMATIONAL, false);
    public static final HttpStatus PROCESSING = new HttpStatus(102, "Processing", Category.INFORMATIONAL, false);
    public static final HttpStatus EARLY_HINTS = new HttpStatus(103, "Early Hints", Category.INFORMATIONAL, false);

    // 2xx Success（成功状态码）
    public static final HttpStatus OK = new HttpStatus(200, "OK", Category.SUCCESS, false);
    public static final HttpStatus CREATED = new HttpStatus(201, "Created", Category.SUCCESS, false);
    public static final HttpStatus ACCEPTED = new HttpStatus(202, "Accepted", Category.SUCCESS, false);
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = new HttpStatus(203, "Non-Authoritative Information", Category.SUCCESS, false);
    public static final HttpStatus NO_CONTENT = new HttpStatus(204, "No Content", Category.SUCCESS, true);
    public static final HttpStatus RESET_CONTENT = new HttpStatus(205, "Reset Content", Category.SUCCESS, true);
    public static final HttpStatus PARTIAL_CONTENT = new HttpStatus(206, "Partial Content", Category.SUCCESS, false);
    public static final HttpStatus MULTI_STATUS = new HttpStatus(207, "Multi-Status", Category.SUCCESS, false);
    public static final HttpStatus ALREADY_REPORTED = new HttpStatus(208, "Already Reported", Category.SUCCESS, false);
    public static final HttpStatus IM_USED = new HttpStatus(226, "IM Used", Category.SUCCESS, false);

    // 3xx Redirection（重定向状态码）
    public static final HttpStatus MULTIPLE_CHOICES = new HttpStatus(300, "Multiple Choices", Category.REDIRECTION, false);
    public static final HttpStatus MOVED_PERMANENTLY = new HttpStatus(301, "Moved Permanently", Category.REDIRECTION, false);
    public static final HttpStatus FOUND = new HttpStatus(302, "Found", Category.REDIRECTION, false);
    public static final HttpStatus SEE_OTHER = new HttpStatus(303, "See Other", Category.REDIRECTION, false);
    public static final HttpStatus NOT_MODIFIED = new HttpStatus(304, "Not Modified", Category.REDIRECTION, true);
    public static final HttpStatus USE_PROXY = new HttpStatus(305, "Use Proxy", Category.REDIRECTION, false);
    public static final HttpStatus TEMPORARY_REDIRECT = new HttpStatus(307, "Temporary Redirect", Category.REDIRECTION, false);
    public static final HttpStatus PERMANENT_REDIRECT = new HttpStatus(308, "Permanent Redirect", Category.REDIRECTION, false);

    // 4xx Client Error（客户端错误状态码）
    public static final HttpStatus BAD_REQUEST = new HttpStatus(400, "Bad Request", Category.CLIENT_ERROR, false);
    public static final HttpStatus UNAUTHORIZED = new HttpStatus(401, "Unauthorized", Category.CLIENT_ERROR, false);
    public static final HttpStatus PAYMENT_REQUIRED = new HttpStatus(402, "Payment Required", Category.CLIENT_ERROR, false);
    public static final HttpStatus FORBIDDEN = new HttpStatus(403, "Forbidden", Category.CLIENT_ERROR, false);
    public static final HttpStatus NOT_FOUND = new HttpStatus(404, "Not Found", Category.CLIENT_ERROR, false);
    public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(405, "Method Not Allowed", Category.CLIENT_ERROR, false);
    public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(406, "Not Acceptable", Category.CLIENT_ERROR, false);
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(407, "Proxy Authentication Required", Category.CLIENT_ERROR, false);
    public static final HttpStatus REQUEST_TIMEOUT = new HttpStatus(408, "Request Timeout", Category.CLIENT_ERROR, false);
    public static final HttpStatus CONFLICT = new HttpStatus(409, "Conflict", Category.CLIENT_ERROR, false);
    public static final HttpStatus GONE = new HttpStatus(410, "Gone", Category.CLIENT_ERROR, false);
    public static final HttpStatus LENGTH_REQUIRED = new HttpStatus(411, "Length Required", Category.CLIENT_ERROR, false);
    public static final HttpStatus PRECONDITION_FAILED = new HttpStatus(412, "Precondition Failed", Category.CLIENT_ERROR, false);
    public static final HttpStatus PAYLOAD_TOO_LARGE = new HttpStatus(413, "Payload Too Large", Category.CLIENT_ERROR, false);
    public static final HttpStatus URI_TOO_LONG = new HttpStatus(414, "URI Too Long", Category.CLIENT_ERROR, false);
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = new HttpStatus(415, "Unsupported Media Type", Category.CLIENT_ERROR, false);
    public static final HttpStatus RANGE_NOT_SATISFIABLE = new HttpStatus(416, "Range Not Satisfiable", Category.CLIENT_ERROR, false);
    public static final HttpStatus EXPECTATION_FAILED = new HttpStatus(417, "Expectation Failed", Category.CLIENT_ERROR, false);
    public static final HttpStatus IM_A_TEAPOT = new HttpStatus(418, "I'm a teapot", Category.CLIENT_ERROR, false);
    public static final HttpStatus MISDIRECTED_REQUEST = new HttpStatus(421, "Misdirected Request", Category.CLIENT_ERROR, false);
    public static final HttpStatus UNPROCESSABLE_ENTITY = new HttpStatus(422, "Unprocessable Entity", Category.CLIENT_ERROR, false);
    public static final HttpStatus LOCKED = new HttpStatus(423, "Locked", Category.CLIENT_ERROR, false);
    public static final HttpStatus FAILED_DEPENDENCY = new HttpStatus(424, "Failed Dependency", Category.CLIENT_ERROR, false);
    public static final HttpStatus TOO_EARLY = new HttpStatus(425, "Too Early", Category.CLIENT_ERROR, false);
    public static final HttpStatus UPGRADE_REQUIRED = new HttpStatus(426, "Upgrade Required", Category.CLIENT_ERROR, false);
    public static final HttpStatus PRECONDITION_REQUIRED = new HttpStatus(428, "Precondition Required", Category.CLIENT_ERROR, false);
    public static final HttpStatus TOO_MANY_REQUESTS = new HttpStatus(429, "Too Many Requests", Category.CLIENT_ERROR, false);
    public static final HttpStatus REQUEST_HEADER_FIELDS_TOO_LARGE = new HttpStatus(431, "Request Header Fields Too Large", Category.CLIENT_ERROR, false);
    public static final HttpStatus UNAVAILABLE_FOR_LEGAL_REASONS = new HttpStatus(451, "Unavailable For Legal Reasons", Category.CLIENT_ERROR, false);

    // 5xx Server Error（服务器错误状态码）
    public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(500, "Internal Server Error", Category.SERVER_ERROR, false);
    public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(501, "Not Implemented", Category.SERVER_ERROR, false);
    public static final HttpStatus BAD_GATEWAY = new HttpStatus(502, "Bad Gateway", Category.SERVER_ERROR, false);
    public static final HttpStatus SERVICE_UNAVAILABLE = new HttpStatus(503, "Service Unavailable", Category.SERVER_ERROR, false);
    public static final HttpStatus GATEWAY_TIMEOUT = new HttpStatus(504, "Gateway Timeout", Category.SERVER_ERROR, false);
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(505, "HTTP Version Not Supported", Category.SERVER_ERROR, false);
    public static final HttpStatus VARIANT_ALSO_NEGOTIATES = new HttpStatus(506, "Variant Also Negotiates", Category.SERVER_ERROR, false);
    public static final HttpStatus INSUFFICIENT_STORAGE = new HttpStatus(507, "Insufficient Storage", Category.SERVER_ERROR, false);
    public static final HttpStatus LOOP_DETECTED = new HttpStatus(508, "Loop Detected", Category.SERVER_ERROR, false);
    public static final HttpStatus NOT_EXTENDED = new HttpStatus(510, "Not Extended", Category.SERVER_ERROR, false);
    public static final HttpStatus NETWORK_AUTHENTICATION_REQUIRED = new HttpStatus(511, "Network Authentication Required", Category.SERVER_ERROR, false);

    private final int code;
    private final String reasonPhrase;
    private final Category category;
    private final boolean noResponseBody;

    /**
     * 构造HTTP状态码
     *
     * @param code           状态码
     * @param reasonPhrase   原因短语
     * @param category       状态码分类
     * @param noResponseBody 是否无响应体
     */
    private HttpStatus(int code, String reasonPhrase, Category category, boolean noResponseBody) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
        this.category = category;
        this.noResponseBody = noResponseBody;
    }

    /**
     * 根据状态码值获取HttpStatus实例
     *
     * @param code 状态码值
     * @return 对应的HttpStatus实例
     */
    public static HttpStatus valueOf(int code) {
        switch (code) {
            // 1xx
            case 100:
                return CONTINUE;
            case 101:
                return SWITCHING_PROTOCOLS;
            case 102:
                return PROCESSING;
            case 103:
                return EARLY_HINTS;

            // 2xx
            case 200:
                return OK;
            case 201:
                return CREATED;
            case 202:
                return ACCEPTED;
            case 203:
                return NON_AUTHORITATIVE_INFORMATION;
            case 204:
                return NO_CONTENT;
            case 205:
                return RESET_CONTENT;
            case 206:
                return PARTIAL_CONTENT;
            case 207:
                return MULTI_STATUS;
            case 208:
                return ALREADY_REPORTED;
            case 226:
                return IM_USED;

            // 3xx
            case 300:
                return MULTIPLE_CHOICES;
            case 301:
                return MOVED_PERMANENTLY;
            case 302:
                return FOUND;
            case 303:
                return SEE_OTHER;
            case 304:
                return NOT_MODIFIED;
            case 305:
                return USE_PROXY;
            case 307:
                return TEMPORARY_REDIRECT;
            case 308:
                return PERMANENT_REDIRECT;

            // 4xx
            case 400:
                return BAD_REQUEST;
            case 401:
                return UNAUTHORIZED;
            case 402:
                return PAYMENT_REQUIRED;
            case 403:
                return FORBIDDEN;
            case 404:
                return NOT_FOUND;
            case 405:
                return METHOD_NOT_ALLOWED;
            case 406:
                return NOT_ACCEPTABLE;
            case 407:
                return PROXY_AUTHENTICATION_REQUIRED;
            case 408:
                return REQUEST_TIMEOUT;
            case 409:
                return CONFLICT;
            case 410:
                return GONE;
            case 411:
                return LENGTH_REQUIRED;
            case 412:
                return PRECONDITION_FAILED;
            case 413:
                return PAYLOAD_TOO_LARGE;
            case 414:
                return URI_TOO_LONG;
            case 415:
                return UNSUPPORTED_MEDIA_TYPE;
            case 416:
                return RANGE_NOT_SATISFIABLE;
            case 417:
                return EXPECTATION_FAILED;
            case 418:
                return IM_A_TEAPOT;
            case 421:
                return MISDIRECTED_REQUEST;
            case 422:
                return UNPROCESSABLE_ENTITY;
            case 423:
                return LOCKED;
            case 424:
                return FAILED_DEPENDENCY;
            case 425:
                return TOO_EARLY;
            case 426:
                return UPGRADE_REQUIRED;
            case 428:
                return PRECONDITION_REQUIRED;
            case 429:
                return TOO_MANY_REQUESTS;
            case 431:
                return REQUEST_HEADER_FIELDS_TOO_LARGE;
            case 451:
                return UNAVAILABLE_FOR_LEGAL_REASONS;

            // 5xx
            case 500:
                return INTERNAL_SERVER_ERROR;
            case 501:
                return NOT_IMPLEMENTED;
            case 502:
                return BAD_GATEWAY;
            case 503:
                return SERVICE_UNAVAILABLE;
            case 504:
                return GATEWAY_TIMEOUT;
            case 505:
                return HTTP_VERSION_NOT_SUPPORTED;
            case 506:
                return VARIANT_ALSO_NEGOTIATES;
            case 507:
                return INSUFFICIENT_STORAGE;
            case 508:
                return LOOP_DETECTED;
            case 510:
                return NOT_EXTENDED;
            case 511:
                return NETWORK_AUTHENTICATION_REQUIRED;

            default:
                if (code < 100 || code >= 600) {
                    throw new IllegalArgumentException("Invalid HTTP status code: " + code);
                }
                return new HttpStatus(code, "Unknown Status", getCategory(code), false);
        }
    }

    /**
     * 根据状态码获取分类
     *
     * @param code 状态码
     * @return 状态码分类
     */
    public static Category getCategory(int code) {
        if (code >= 100 && code < 200) {
            return Category.INFORMATIONAL;
        } else if (code >= 200 && code < 300) {
            return Category.SUCCESS;
        } else if (code >= 300 && code < 400) {
            return Category.REDIRECTION;
        } else if (code >= 400 && code < 500) {
            return Category.CLIENT_ERROR;
        } else if (code >= 500 && code < 600) {
            return Category.SERVER_ERROR;
        } else {
            throw new IllegalArgumentException("Invalid HTTP status code: " + code);
        }
    }

    /**
     * 判断状态码是否表示成功
     *
     * @param code 状态码
     * @return true表示成功
     */
    public static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }

    /**
     * 判断状态码是否表示重定向
     *
     * @param code 状态码
     * @return true表示重定向
     */
    public static boolean isRedirect(int code) {
        return code >= 300 && code < 400;
    }

    /**
     * 判断状态码是否表示客户端错误
     *
     * @param code 状态码
     * @return true表示客户端错误
     */
    public static boolean isClientError(int code) {
        return code >= 400 && code < 500;
    }

    /**
     * 判断状态码是否表示服务器错误
     *
     * @param code 状态码
     * @return true表示服务器错误
     */
    public static boolean isServerError(int code) {
        return code >= 500 && code < 600;
    }

    /**
     * 判断是否为错误状态码（4xx或5xx）
     *
     * @param code 状态码
     * @return true表示错误状态码
     */
    public static boolean isError(int code) {
        return isClientError(code) || isServerError(code);
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取原因短语
     *
     * @return 原因短语
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * 获取状态码分类
     *
     * @return 状态码分类
     */
    public Category getCategory() {
        return category;
    }

    /**
     * 判断是否无响应体
     *
     * @return true表示无响应体
     */
    public boolean isNoResponseBody() {
        return noResponseBody;
    }

    /**
     * 判断是否为信息性状态码（1xx）
     *
     * @return true表示信息性状态码
     */
    public boolean isInformational() {
        return category == Category.INFORMATIONAL;
    }

    /**
     * 判断是否为成功状态码（2xx）
     *
     * @return true表示成功状态码
     */
    public boolean isSuccess() {
        return category == Category.SUCCESS;
    }

    /**
     * 判断是否为重定向状态码（3xx）
     *
     * @return true表示重定向状态码
     */
    public boolean isRedirect() {
        return category == Category.REDIRECTION;
    }

    /**
     * 判断是否为客户端错误状态码（4xx）
     *
     * @return true表示客户端错误状态码
     */
    public boolean isClientError() {
        return category == Category.CLIENT_ERROR;
    }

    /**
     * 判断是否为服务器错误状态码（5xx）
     *
     * @return true表示服务器错误状态码
     */
    public boolean isServerError() {
        return category == Category.SERVER_ERROR;
    }

    /**
     * 判断是否为错误状态码（4xx或5xx）
     *
     * @return true表示错误状态码
     */
    public boolean isError() {
        return isClientError() || isServerError();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpStatus that = (HttpStatus) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " " + reasonPhrase;
    }

    /**
     * HTTP状态码分类枚举
     */
    public enum Category {
        /**
         * 1xx Informational（信息性状态码）
         * 表示接收的请求正在处理
         */
        INFORMATIONAL(1, "Informational"),

        /**
         * 2xx Success（成功状态码）
         * 表示请求正常处理完毕
         */
        SUCCESS(2, "Success"),

        /**
         * 3xx Redirection（重定向状态码）
         * 表示需要后续操作才能完成请求
         */
        REDIRECTION(3, "Redirection"),

        /**
         * 4xx Client Error（客户端错误状态码）
         * 表示客户端发送的请求有误
         */
        CLIENT_ERROR(4, "Client Error"),

        /**
         * 5xx Server Error（服务器错误状态码）
         * 表示服务器处理请求出错
         */
        SERVER_ERROR(5, "Server Error");

        private final int value;
        private final String reasonPhrase;

        Category(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }

        public int getValue() {
            return value;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        @Override
        public String toString() {
            return value + "xx " + reasonPhrase;
        }
    }
}
