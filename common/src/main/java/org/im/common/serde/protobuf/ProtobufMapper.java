package org.im.common.serde.protobuf;

import com.google.protobuf.Parser;
import org.im.common.exception.type.serde.ProtobufSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protobuf 序列化/反序列化工具类
 * <p>
 * 提供通用的 Protobuf 序列化和反序列化功能
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/09
 */
public class ProtobufMapper {

    private static final Logger logger = LoggerFactory.getLogger(ProtobufMapper.class);

    /**
     * 将实体对象序列化为字节数组
     *
     * @param obj       实体对象
     * @param converter 转换器
     * @param <T>       实体对象类型
     * @return 序列化后的字节数组
     * @throws ProtobufSerializationException 序列化异常
     */
    public static <T> byte[] serialize(T obj, ProtoConverter<T> converter)
            throws ProtobufSerializationException {
        try {
            com.google.protobuf.Message proto = converter.toProto(obj);
            return proto.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to serialize object to Protobuf", e);
            throw new ProtobufSerializationException("Failed to serialize object to Protobuf", e);
        }
    }

    /**
     * 将字节数组反序列化为实体对象
     *
     * @param data      字节数组
     * @param converter 转换器
     * @param parser    Protobuf 解析器
     * @param <T>       实体对象类型
     * @return 反序列化后的实体对象
     * @throws ProtobufSerializationException 反序列化异常
     */
    public static <T> T deserialize(byte[] data, ProtoConverter<T> converter,
                                    Parser<? extends com.google.protobuf.Message> parser)
            throws ProtobufSerializationException {
        try {
            com.google.protobuf.Message proto = parser.parseFrom(data);
            return converter.fromProto(proto);
        } catch (Exception e) {
            logger.error("Failed to deserialize Protobuf to object", e);
            throw new ProtobufSerializationException("Failed to deserialize Protobuf to object", e);
        }
    }
}
