package com.im.qtech.data.serde;

import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/26 11:41:14
 */

public class WbOlpRawDataRecordSerializer implements Serializer<WbOlpRawDataRecord> {

    private static final String SCHEMA_FILE_PATH = "avro/WbOlpRawData.avsc";
    private static final Schema SCHEMA = loadSchema();
    private final DatumWriter<WbOlpRawDataRecord> datumWriter = new SpecificDatumWriter<>(SCHEMA);

    private static Schema loadSchema() {
        try {
            return new Schema.Parser().parse(WbOlpRawDataRecordSerializer.class.getClassLoader().getResourceAsStream(WbOlpRawDataRecordSerializer.SCHEMA_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Error loading Avro schema.", e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Optional configurations
    }

    @Override
    public byte[] serialize(String topic, WbOlpRawDataRecord data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(data, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Error serializing WbOlpRawDataRecord to Avro.", e);
        }
    }

    @Override
    public void close() {
        // Clean up resources if necessary
    }
}