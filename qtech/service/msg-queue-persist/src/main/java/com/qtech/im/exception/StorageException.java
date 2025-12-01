package com.qtech.im.exception;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/09
 */
public class StorageException extends BaseException {
    private static final long serialVersionUID = 1L;

    public StorageException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public StorageException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public StorageException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
