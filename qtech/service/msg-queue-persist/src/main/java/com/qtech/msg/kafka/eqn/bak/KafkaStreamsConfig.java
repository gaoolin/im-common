package com.qtech.msg.kafka.eqn.bak;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/11/19 11:51:28
 * desc   :
 */

// @Configuration
public class KafkaStreamsConfig {

    @Bean(name = "kafkaStreamProperties")
    public Properties kafkaStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "device-status-streams-app"); // 应用 ID
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.170.6.170:9092,10.170.6.171:9092,10.170.6.172:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");

        // 设置 Key 和 Value 的 Serde
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        return props;
    }
}