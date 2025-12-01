package com.im.qtech.data.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Source 工厂类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/26 14:45:08
 */

public class KafkaSourceFactory {
    public static DataStreamSource<String> createSource(StreamExecutionEnvironment env) {
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("10.170.6.170:9092,10.170.6.171:9092,10.170.6.172:9092")
                .setTopics("pip_executor_to_druid")
                .setGroupId("flink-device-status-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "KafkaSource");
    }
}
