package com.im.qtech.service.config.kafka;

import com.im.qtech.common.avro.record.WbOlpRawDataRecord;
import com.im.qtech.common.serde.EqpReversePOJOValueDeserializer;
import com.im.qtech.common.serde.WbOlpRawDataRecordDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka消费者配置 - 基于Spring Boot 3优化
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/30 08:50:43
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<Long, EqpReverseInfoRecord> consumerRecordFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EqpReversePOJOValueDeserializer.class);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 确保设置了group.id
        if (!props.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "im-framework-group");
        }
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 定义 kafkaListenerContainerRecordFactory Bean，以支持批量消费
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EqpReverseInfoRecord> EqReverseCtrlInfoContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, EqpReverseInfoRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerRecordFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(3); // 增加并发度
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000); // 设置poll超时
        return factory;
    }

    @Bean
    public ConsumerFactory<Long, WbOlpRawDataRecord> consumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WbOlpRawDataRecordDeserializer.class);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 确保设置了group.id
        if (!props.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "im-framework-group");
        }
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 定义 KafkaListenerContainerFactory Bean，以支持批量消费
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, WbOlpRawDataRecord> WbOlpRawDataContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, WbOlpRawDataRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(3); // 增加并发度
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000); // 设置poll超时
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerStringFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 确保设置了group.id
        if (!props.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "device-status-group");
        }
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerStringFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerStringFactory());
        factory.setConcurrency(2); // 增加并发度
        factory.getContainerProperties().setPollTimeout(3000); // 设置poll超时
        return factory;
    }
}
