package com.qtech.msg.serializer;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 20:04:10
 */


public class EqReversePOJORecordSerde implements Serde<EqpReversePOJORecord> {
    @Override
    public Serializer<EqpReversePOJORecord> serializer() {
        return new EqReversePOJORecordValueSerializer();
    }

    @Override
    public Deserializer<EqpReversePOJORecord> deserializer() {
        return new EqReversePOJORecordValueDeserializer();
    }
}
