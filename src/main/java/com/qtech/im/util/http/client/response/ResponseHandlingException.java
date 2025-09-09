package com.qtech.im.util.http.client.response;

/**
 * 响应处理异常类
 * <p>
 * 特性：
 * - 通用性：适用于所有响应处理场景
 * - 规范性：遵循异常处理最佳实践
 * - 专业性：提供详细的错误信息
 * - 灵活性：支持多种错误类型
 * - 可靠性：确保异常信息的完整性
 * - 安全性：不暴露敏感信息
 * - 复用性：可被各种处理场景复用
 * - 容错性：提供丰富的错误分类
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class ResponseHandlingException extends Exception {
    private final ResponseKit response;
    private final ErrorType errorType;

    public ResponseHandlingException(String message, ResponseKit response, ErrorType errorType) {
        super(message);
        this.response = response;
        this.errorType = errorType;
    }

    public ResponseHandlingException(String message, Throwable cause, ResponseKit response, ErrorType errorType) {
        super(message, cause);
        this.response = response;
        this.errorType = errorType;
    }

    public ResponseKit getResponse() {
        return response;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        /**
         * 响应体过大
         */
        RESPONSE_TOO_LARGE,

        /**
         * 响应体损坏
         */
        RESPONSE_CORRUPTED,

        /**
         * 内容类型不支持
         */
        UNSUPPORTED_CONTENT_TYPE,

        /**
         * 编码错误
         */
        ENCODING_ERROR,

        /**
         * 解析错误
         */
        PARSING_ERROR,

        /**
         * 安全违规
         */
        SECURITY_VIOLATION,

        /**
         * 超时
         */
        TIMEOUT,

        /**
         * 其他错误
         */
        OTHER
    }
}
