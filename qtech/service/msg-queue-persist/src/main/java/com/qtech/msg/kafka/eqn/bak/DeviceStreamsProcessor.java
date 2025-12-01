package com.qtech.msg.kafka.eqn.bak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/11/19 11:52:06
 * desc   :
 */

// @Component
public class DeviceStreamsProcessor {

    private static final String INPUT_TOPIC = "pip_executor_to_druid";
    private static final String OUTPUT_TOPIC = "filtered_device_data";

    // @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public KafkaStreams kafkaStreams(@Autowired StreamsBuilder streamsBuilder,
                                     @Autowired Properties kafkaStreamProperties) {
        // 指定 Key 和 Value 的 Serde
        KStream<String, String> stream = streamsBuilder.stream(
                INPUT_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        stream.filter((key, value) -> {
            try {
                JsonNode rootNode = objectMapper.readTree(value);
                String deviceType = rootNode.get("device_type").asText();
                return "WB".equals(deviceType) || "XYZ".equals(deviceType);
            } catch (Exception e) {
                System.err.println("Error filtering message: " + e.getMessage());
                return false;
            }
        }).to(OUTPUT_TOPIC);

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), kafkaStreamProperties);
        streams.start();
        return streams;
    }
}