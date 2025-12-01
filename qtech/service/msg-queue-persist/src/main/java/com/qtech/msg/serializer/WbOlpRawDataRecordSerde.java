package com.qtech.msg.serializer;

import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/26 11:49:21
 */

public class WbOlpRawDataRecordSerde implements Serde<WbOlpRawDataRecord> {
    @Override
    public Serializer<WbOlpRawDataRecord> serializer() {
        return new WbOlpRawDataRecordSerializer();
    }

    @Override
    public Deserializer<WbOlpRawDataRecord> deserializer() {
        return new WbOlpRawDataRecordDeserializer();
    }
}