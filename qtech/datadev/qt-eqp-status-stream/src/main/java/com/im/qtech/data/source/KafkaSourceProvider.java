package com.im.qtech.data.source;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/23 13:48:48
 */
public class KafkaSourceProvider {

    public static KafkaSource<String> createSource() {
        return KafkaSource.<String>builder()
                .setBootstrapServers("10.170.6.170:9092,10.170.6.171:9092,10.170.6.172:9092")
                .setTopics("pip_executor_to_druid")
                .setGroupId("device-sync-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }
}
