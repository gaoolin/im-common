package org.im.exception.type.eqp;

import org.im.exception.type.BaseException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/17
 */

public class EquipmentException extends BaseException {
    public EquipmentException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public EquipmentException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public EquipmentException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
