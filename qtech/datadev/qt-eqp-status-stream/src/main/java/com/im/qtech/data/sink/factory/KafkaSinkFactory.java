package com.im.qtech.data.sink.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.model.EqNetworkStatus;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;

/**
 * Sink 工厂类，使用新的 KafkaSink API
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/26 15:06:31
 */
public class KafkaSinkFactory {
    public static void createSink(DataStream<EqNetworkStatus> stream) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        KafkaSink<String> sink = KafkaSink.<String>builder().setBootstrapServers("10.170.6.24:9092,10.170.6.25:9092,10.170.6.26:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder().setTopic("filtered_device_data_test")
                        .setValueSerializationSchema(new SimpleStringSchema()).build()).build();

        stream.map(mapper::writeValueAsString).sinkTo(sink);
    }
}
