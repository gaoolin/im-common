package org.im.exception.type;

/**
 * 应用程序基础异常类
 * <similar>
 * 所有自定义异常的基类，提供统一的异常处理框架
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 13:36:15
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    /**
     * 错误详情
     */
    private final Object errorDetails;

    /**
     * 异常发生时间
     */
    private final long timestamp;

    public BaseException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, null, null);
    }

    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        this(errorCode, errorMessage, null, cause);
    }

    public BaseException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getErrorDetails() {
        return errorDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("BaseException{errorCode='%s', errorMessage='%s', errorDetails=%s, timestamp=%d}",
                errorCode, errorMessage, errorDetails, timestamp);
    }
}