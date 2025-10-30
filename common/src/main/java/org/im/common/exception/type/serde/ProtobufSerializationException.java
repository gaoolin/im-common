package org.im.common.exception.type.serde;

/**
 * Protobuf 序列化异常
 * <p>
 * 用于封装 Protobuf 序列化/反序列化过程中的异常
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/09
 */
public class ProtobufSerializationException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public ProtobufSerializationException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public ProtobufSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}