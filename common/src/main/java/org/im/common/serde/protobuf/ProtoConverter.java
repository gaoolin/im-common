package org.im.common.serde.protobuf;

/**
 * Protobuf 转换器接口
 * <p>
 * 定义实体对象与 Protobuf 消息之间的转换规范
 * </p>
 *
 * @param <T> 实体对象类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/09
 */
public interface ProtoConverter<T> {

    /**
     * 将实体对象转换为 Protobuf 消息
     *
     * @param obj 实体对象
     * @return Protobuf 消息
     */
    com.google.protobuf.Message toProto(T obj);

    /**
     * 将 Protobuf 消息转换为实体对象
     *
     * @param proto Protobuf 消息
     * @return 实体对象
     */
    T fromProto(com.google.protobuf.Message proto);
}
