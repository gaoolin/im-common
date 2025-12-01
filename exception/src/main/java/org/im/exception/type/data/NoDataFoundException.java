package org.im.exception.type.data;

import org.im.exception.type.BaseException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class NoDataFoundException extends BaseException {
    public NoDataFoundException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public NoDataFoundException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public NoDataFoundException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
