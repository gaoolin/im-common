package org.im.exception.type.orm;

/**
 * ORM框架基础异常类
 * 所有ORM相关异常的基类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public class ORMException extends RuntimeException {
    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public ORMException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public ORMException(String message, Throwable cause) {
        super(message, cause);
    }
}