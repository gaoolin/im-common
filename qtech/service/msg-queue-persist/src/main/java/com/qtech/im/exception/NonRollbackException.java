package com.qtech.im.exception;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/20 13:31:59
 */


public class NonRollbackException extends BaseException {
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数，创建一个带有错误码和错误信息的非回滚异常
     *
     * @param errorCode    错误码，用于标识异常类型
     * @param errorMessage 错误信息，描述异常的具体内容
     */
    public NonRollbackException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * 构造函数，创建一个带有错误码、错误信息和原因的非回滚异常
     *
     * @param errorCode    错误码，用于标识异常类型
     * @param errorMessage 错误信息，描述异常的具体内容
     * @param cause        异常原因，导致此异常的原始异常
     */
    public NonRollbackException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    /**
     * 构造函数，创建一个带有错误码、错误信息、错误详情和原因的非回滚异常
     *
     * @param errorCode    错误码，用于标识异常类型
     * @param errorMessage 错误信息，描述异常的具体内容
     * @param errorDetails 错误详情，包含异常相关的详细数据
     * @param cause        异常原因，导致此异常的原始异常
     */
    public NonRollbackException(String errorCode, String errorMessage, Object errorDetails, Throwable cause) {
        super(errorCode, errorMessage, errorDetails, cause);
    }
}
