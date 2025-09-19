package com.im.exception;

/**
 * 系统异常类
 * <p>
 * 用于表示系统级别或技术层面的异常情况
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 13:38:18
 */
public class SystemException extends BaseException {
    private static final long serialVersionUID = 1L;

    public SystemException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public SystemException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public SystemException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
