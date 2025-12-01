package com.qtech.msg.config.kafka;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.qtech.msg.serializer.EqReversePOJORecordValueSerializer;
import com.qtech.msg.serializer.WbOlpRawDataRecordSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/11/19 15:51:53
 */

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.target.bootstrap-servers}")
    private String targetBootstrapServers;

    @Bean
    @Qualifier("targetKafkaTemplate")
    public KafkaTemplate<String, String> targetKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, targetBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("wbOlpRawDataKafkaTemplate")
    public KafkaTemplate<Long, WbOlpRawDataRecord> wbOlpRawDataKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, targetBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WbOlpRawDataRecordSerializer.class);
        DefaultKafkaProducerFactory<Long, WbOlpRawDataRecord> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("wbOlpChkKafkaTemplate")
    public KafkaTemplate<Long, EqpReversePOJORecord> kafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, targetBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EqReversePOJORecordValueSerializer.class);
        DefaultKafkaProducerFactory<Long, EqpReversePOJORecord> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
}
