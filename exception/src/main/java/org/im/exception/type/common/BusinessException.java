package org.im.exception.type.common;

import org.im.exception.type.BaseException;

/**
 * 业务异常类
 * <p>
 * 用于表示业务逻辑层面的异常情况
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 13:37:20
 */
public class BusinessException extends BaseException {
    private static final long serialVersionUID = 1L;

    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public BusinessException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}