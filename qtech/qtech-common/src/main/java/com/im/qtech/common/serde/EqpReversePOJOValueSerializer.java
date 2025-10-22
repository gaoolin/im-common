package com.im.qtech.common.serde;

import com.im.qtech.common.avro.record.EqpReversePOJORecord;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * avro serializer for EqpReverseInfoRecord
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 21:11:12
 */

public class EqpReversePOJOValueSerializer implements Serializer<EqpReversePOJORecord> {
    private static final Logger logger = LoggerFactory.getLogger(EqpReversePOJOValueSerializer.class);
    private static final String SCHEMA_FILE_PATH = "avro/EqpReversePOJO.avsc";
    // private static final Schema SCHEMA = loadSchema();
    // 直接使用类的Schema，与反序列化器保持一致
    private final DatumWriter<EqpReversePOJORecord> datumWriter = new SpecificDatumWriter<>(EqpReversePOJORecord.class);

    // private final DatumWriter<EqpReversePOJORecord> datumWriter = new SpecificDatumWriter<>(SCHEMA);

    private static Schema loadSchema() {
        try {
            return new Schema.Parser().parse(EqpReversePOJOValueSerializer.class.getClassLoader().getResourceAsStream(EqpReversePOJOValueSerializer.SCHEMA_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Error loading Avro schema.", e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Optional configurations
    }

    @Override
    public byte[] serialize(String topic, EqpReversePOJORecord data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(data, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error serializing EqpReversePOJORecord to Avro. Topic: {}, Data: {}", topic, data, e);
            throw new SerializationException("Error serializing EqpReversePOJORecord to Avro.", e);
        }
    }

    @Override
    public void close() {
        // Clean up resources if necessary
    }

    // 在Serializer和Deserializer中添加
    private void validateSchemaCompatibility() {
        Schema writerSchema = EqpReversePOJORecord.getClassSchema();
        // 添加兼容性检查逻辑
        logger.debug("Using schema: {}", writerSchema.toString(true));
    }
}
