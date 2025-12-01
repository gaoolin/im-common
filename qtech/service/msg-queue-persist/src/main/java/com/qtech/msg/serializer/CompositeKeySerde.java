package com.qtech.msg.serializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/08/24 19:57:50
 * desc   :
 */

public class CompositeKeySerde implements Serde<CompositeKey> {
    @Override
    public Serializer<CompositeKey> serializer() {
        return new CompositeKeySerializer();
    }

    @Override
    public Deserializer<CompositeKey> deserializer() {
        return new CompositeKeyDeserializer();
    }
}

// Similar implementation for EqReversePOJORecordSerde