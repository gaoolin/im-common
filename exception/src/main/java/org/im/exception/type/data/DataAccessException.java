package org.im.exception.type.data;

import org.im.exception.type.BaseException;

/**
 * 数据访问异常类
 * <p>
 * 用于封装数据访问异常
 *
 * @author :  gaozhilin
 * @email :  gaoolin@gmail.com
 * @date :  2024/11/19 08:51:04
 */

public class DataAccessException extends BaseException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public DataAccessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public DataAccessException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}

