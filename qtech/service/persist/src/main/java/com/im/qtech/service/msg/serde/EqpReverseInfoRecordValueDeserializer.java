package com.im.qtech.service.msg.serde;

import com.im.qtech.service.msg.avro.EqpReverseInfoRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 21:16:04
 */

public class EqpReverseInfoRecordValueDeserializer implements Deserializer<EqpReverseInfoRecord> {
    private static final Logger logger = LoggerFactory.getLogger(EqpReverseInfoRecordValueDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // 可选配置
    }

    @Override
    public EqpReverseInfoRecord deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
            SpecificDatumReader<EqpReverseInfoRecord> datumReader = new SpecificDatumReader<>(EqpReverseInfoRecord.class);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            logger.error("Error while deserializing Avro to EqReverseCtrlInfoRecord.", e);
            throw new SerializationException("Error while deserializing Avro to EqReverseCtrlInfoRecord.", e);
        }
    }

    @Override
    public void close() {
        // 可选资源释放
    }
}