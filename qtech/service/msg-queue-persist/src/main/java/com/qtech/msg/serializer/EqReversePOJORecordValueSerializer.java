package com.qtech.msg.serializer;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
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
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 21:11:12
 */

public class EqReversePOJORecordValueSerializer implements Serializer<EqpReversePOJORecord> {
    private static final Logger logger = LoggerFactory.getLogger(EqReversePOJORecordValueSerializer.class);
    private static final String SCHEMA_FILE_PATH = "avro/EqpReversePOJORecord.avsc";
    private static final Schema SCHEMA = loadSchema();
    private final DatumWriter<EqpReversePOJORecord> datumWriter = new SpecificDatumWriter<>(SCHEMA);

    private static Schema loadSchema() {
        try {
            return new Schema.Parser().parse(EqReversePOJORecordValueSerializer.class.getClassLoader().getResourceAsStream(EqReversePOJORecordValueSerializer.SCHEMA_FILE_PATH));
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
            throw new SerializationException("Error serializing WbOlpRawDataCompositeKey to Avro.", e);
        }
    }

    @Override
    public void close() {
        // Clean up resources if necessary
    }
}
