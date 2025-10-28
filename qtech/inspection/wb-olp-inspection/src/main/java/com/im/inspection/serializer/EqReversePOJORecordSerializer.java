package com.im.inspection.serializer;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
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
 * @date 2024/08/24 21:11:12
 */
public class EqReversePOJORecordSerializer implements Serializer<EqpReversePOJORecord> {

    private final DatumWriter<EqpReversePOJORecord> datumWriter =
            new SpecificDatumWriter<>(EqpReversePOJORecord.class);

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
            throw new SerializationException(">>>>> Error serializing EqReverseCtrlInfoRecord to Avro.", e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Optional logging or config check
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
