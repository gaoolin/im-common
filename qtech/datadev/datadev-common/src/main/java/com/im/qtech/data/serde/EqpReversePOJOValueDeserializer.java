package com.im.qtech.data.serde;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.lang3.SerializationException;
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

public class EqpReversePOJOValueDeserializer implements Deserializer<EqpReversePOJORecord> {
    private static final Logger logger = LoggerFactory.getLogger(EqpReversePOJOValueDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // 可选配置
    }


    @Override
    public EqpReversePOJORecord deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            logger.warn("Received empty or null data from topic: {}", topic);
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Decoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
            SpecificDatumReader<EqpReversePOJORecord> datumReader = new SpecificDatumReader<>(EqpReversePOJORecord.class);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            logger.error("IO error while deserializing Avro message from topic: {}. Data length: {}, First 10 bytes: {}",
                    topic, data.length, bytesToHex(data), e);
            throw new SerializationException("IO error while deserializing Avro message", e);
        } catch (Exception e) {
            logger.error("Unexpected error while deserializing Avro message from topic: {}. Data length: {}, First 10 bytes: {}",
                    topic, data.length, bytesToHex(data), e);
            throw new SerializationException("Unexpected error while deserializing Avro message", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 10); i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    @Override
    public void close() {
        // 可选资源释放
    }

    // 在Serializer和Deserializer中添加
    private void validateSchemaCompatibility() {
        Schema writerSchema = EqpReversePOJORecord.getClassSchema();
        // 添加兼容性检查逻辑
        logger.debug("Using schema: {}", writerSchema.toString(true));
    }
}