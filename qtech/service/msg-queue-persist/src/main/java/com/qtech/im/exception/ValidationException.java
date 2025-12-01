package com.qtech.im.exception;

import java.util.List;

/**
 * 参数校验异常类
 * <p>
 * 用于表示参数校验失败的情况
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19 13:38:57
 */
public class ValidationException extends BusinessException {
    private static final long serialVersionUID = 1L;

    private final List<ValidationError> validationErrors;

    public ValidationException(String errorCode, String errorMessage, List<ValidationError> validationErrors) {
        super(errorCode, errorMessage);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String errorCode, String errorMessage, List<ValidationError> validationErrors, Throwable cause) {
        super(errorCode, errorMessage, cause);
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    /**
     * 校验错误信息类
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ValidationError(String field, String message) {
            this(field, message, null);
        }

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        // Getters
        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
}
