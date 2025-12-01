package com.qtech.msg.serializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/08/26 11:46:19
 * desc   :
 */


public class WbOlpRawDataCompositeKeySerde implements Serde<WbOlpRawDataCompositeKey> {
    @Override
    public Serializer<WbOlpRawDataCompositeKey> serializer() {
        return new WbOlpRawDataCompositeKeySerializer();
    }

    @Override
    public Deserializer<WbOlpRawDataCompositeKey> deserializer() {
        return new WbOlpRawDataCompositeKeyDeserializer();
    }
}
