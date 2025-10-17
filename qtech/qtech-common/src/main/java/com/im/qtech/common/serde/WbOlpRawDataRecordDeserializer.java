package com.im.qtech.common.serde;

import com.im.qtech.common.avro.record.WbOlpRawDataRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/26 11:44:06
 */

public class WbOlpRawDataRecordDeserializer implements Deserializer<WbOlpRawDataRecord> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // 可选配置
    }

    @Override
    public WbOlpRawDataRecord deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
            SpecificDatumReader<WbOlpRawDataRecord> datumReader = new SpecificDatumReader<>(WbOlpRawDataRecord.class);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException("Error while deserializing Avro to WbOlpRawDataRecord.", e);
        }
    }

    @Override
    public void close() {
        // 可选资源释放
    }
}
