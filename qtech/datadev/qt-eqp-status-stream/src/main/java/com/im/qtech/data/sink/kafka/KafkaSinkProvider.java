package com.im.qtech.data.sink.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.model.EqNetworkStatus;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.im.common.json.JsonMapperProvider;

import java.nio.charset.StandardCharsets;

/**
 * KafkaSinkProvider
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/23 13:57:43
 */
public class KafkaSinkProvider {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperProvider.getSharedInstance();

    public static KafkaSink<String> createSink() {
        return KafkaSink.<String>builder()
                .setBootstrapServers("10.170.6.24:9092,10.170.6.25:9092,10.170.6.26:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic("filtered_device_data")
                        .setKeySerializationSchema((SerializationSchema<String>) element -> {
                            String key = extractDeviceId(element);
                            return key.getBytes(StandardCharsets.UTF_8);
                        })
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    private static String extractDeviceId(String json) {
        try {
            EqNetworkStatus record = OBJECT_MAPPER.readValue(json, EqNetworkStatus.class);
            return record.getDeviceId() == null ? "unknown" : record.getDeviceId();
        } catch (Exception e) {
            // 异常时提供 fallback key，避免空 key 影响分区
            return "invalid-json";
        }
    }
}