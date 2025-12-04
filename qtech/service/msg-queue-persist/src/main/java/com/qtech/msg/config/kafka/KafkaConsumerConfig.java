package com.qtech.msg.config.kafka;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.qtech.msg.serializer.EqReversePOJORecordValueDeserializer;
import com.qtech.msg.serializer.WbOlpRawDataRecordDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
 * Kafka消费者配置类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/30 08:50:43
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${kafka.source.bootstrap-servers}")
    private String sourceBootstrapServers;

    @Autowired
    private KafkaProperties kafkaProperties;

    /**
     * 创建用于处理 EqpReversePOJORecord 的消费者工厂
     */
    @Bean
    public ConsumerFactory<Long, EqpReversePOJORecord> consumerRecordFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EqReversePOJORecordValueDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 创建用于处理 EqpReversePOJORecord 的批量消费容器工厂
     * 专用于 WbOlpConsumer 消费者
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, EqpReversePOJORecord> eqReverseInfoContainerFactory(
            ConsumerFactory<Long, EqpReversePOJORecord> consumerRecordFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, EqpReversePOJORecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> props = new HashMap<>(((DefaultKafkaConsumerFactory<Long, EqpReversePOJORecord>) consumerRecordFactory).getConfigurationProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "msg-persist-group-2"); // 设置消费组ID
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setBatchListener(true);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * 创建用于处理 WbOlpRawDataRecord 的消费者工厂
     */
    @Bean
    public ConsumerFactory<Long, WbOlpRawDataRecord> consumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WbOlpRawDataRecordDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 创建用于处理 WbOlpRawDataRecord 的批量消费容器工厂
     * 专用于 WbOlpRawConsumer 消费者
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, WbOlpRawDataRecord> wbOlpRawDataContainerFactory(
            ConsumerFactory<Long, WbOlpRawDataRecord> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, WbOlpRawDataRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> props = new HashMap<>(((DefaultKafkaConsumerFactory<Long, WbOlpRawDataRecord>) consumerFactory).getConfigurationProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "msg-persist-group-1"); // 设置消费组ID
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setBatchListener(true);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * 创建用于处理字符串类型 WbOlpRawDataRecord 的消费者工厂
     */
    @Bean
    public ConsumerFactory<String, WbOlpRawDataRecord> consumerWbOlpRawDataRecordFactory() {
        HashMap<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WbOlpRawDataRecordDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 创建用于处理字符串类型 WbOlpRawDataRecord 的容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WbOlpRawDataRecord> kafkaListenerContainerWbOlpRawDataRecordFactory() {
        ConcurrentKafkaListenerContainerFactory<String, WbOlpRawDataRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerWbOlpRawDataRecordFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    /**
     * 创建用于处理字符串类型的消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> consumerStringFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 创建用于处理字符串类型的容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerStringFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerStringFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    /**
     * 创建用于消费设备状态数据的容器工厂
     * 用于处理设备状态相关的字符串消息
     */
    @Bean
    @Qualifier("sourceKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> sourceKafkaListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, sourceBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "device-status-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 524288);
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5000);
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 200);
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setConcurrency(2);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
