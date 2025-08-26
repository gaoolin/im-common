package com.qtech.im.exception;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/20 14:39:59
 */
public class HttpException extends BaseException {
    private static final long serialVersionUID = 1L;

    public HttpException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public HttpException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public HttpException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
