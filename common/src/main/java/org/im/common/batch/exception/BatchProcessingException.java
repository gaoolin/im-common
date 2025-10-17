package org.im.common.batch.exception;

/**
 * 批处理异常基类
 * <p>
 * 所有批处理相关异常的基类
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */
public class BatchProcessingException extends Exception {

    public BatchProcessingException() {
        super();
    }

    public BatchProcessingException(String message) {
        super(message);
    }

    public BatchProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BatchProcessingException(Throwable cause) {
        super(cause);
    }
}

