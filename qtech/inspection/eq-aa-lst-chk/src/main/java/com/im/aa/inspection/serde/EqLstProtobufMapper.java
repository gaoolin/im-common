package com.im.aa.inspection.serde;

import com.im.aa.inspection.proto.EqLstProto;
import org.im.common.serde.protobuf.ProtoConverter;
import org.im.common.serde.protobuf.ProtobufMapper;
import org.im.exception.type.serde.ProtobufSerializationException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/09
 */
public class EqLstProtobufMapper {

    private static final ProtoConverter<com.im.qtech.common.dto.param.EqLstPOJO> CONVERTER =
            new EqLstProtoConverter();

    public static byte[] serialize(com.im.qtech.common.dto.param.EqLstPOJO obj) {
        try {
            return ProtobufMapper.serialize(obj, CONVERTER);
        } catch (ProtobufSerializationException e) {
            throw new RuntimeException("Failed to serialize EqLstPOJO to Protobuf", e);
        }
    }

    public static com.im.qtech.common.dto.param.EqLstPOJO deserialize(byte[] data) {
        try {
            return ProtobufMapper.deserialize(data, CONVERTER, EqLstProto.EqLstPOJO.parser());
        } catch (ProtobufSerializationException e) {
            throw new RuntimeException("Failed to deserialize Protobuf to EqLstPOJO", e);
        }
    }
}
