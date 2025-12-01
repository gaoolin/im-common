package com.qtech.msg.kafka.olp.bak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.qtech.msg.serializer.EqReversePOJORecordSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 19:56:11
 */
// @Component
// @EnableKafkaStreams
public class WbOlpChkKafkaDeduplicationTopology {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpChkKafkaDeduplicationTopology.class);

    // @Autowired
    private RabbitTemplate rabbitTemplate;

    // @Autowired
    private StreamsBuilder streamsBuilder;

    // @Autowired
    private ObjectMapper objectMapper;

    // @PostConstruct
    public void init() {
        logger.info(">>>>> Initializing Kafka Streams topology...");
        createTopology(streamsBuilder);
    }

    public void createTopology(StreamsBuilder streamsBuilder) {
        logger.info(">>>>> Creating topology for topic qtech_im_wb_olp_chk_topic");
        Serde<EqpReversePOJORecord> valueSerde = new EqReversePOJORecordSerde();
        KStream<EqpReversePOJORecord, EqpReversePOJORecord> inputStream = streamsBuilder.stream("qtech_im_wb_olp_chk_topic", Consumed.with(valueSerde, valueSerde));

        inputStream.foreach((key, value) -> logger.info(">>>>> Received record: key = {}, value = {}", key, value));

        inputStream.groupByKey().reduce((oldValue, newValue) -> {
                    if (oldValue != null) {
                        // 如果旧值存在，返回旧值，丢弃新值
                        logger.info(">>>>> Duplicate record detected, ignoring new record: oldValue = {}, newValue = {}", oldValue, newValue);
                        return oldValue;
                    } else {
                        // 如果旧值不存在，保留新值
                        // sendToRabbitMQ(newValue);
                        return newValue;
                    }
                }, Materialized.<EqpReversePOJORecord, EqpReversePOJORecord, KeyValueStore<Bytes, byte[]>>as("wbOlpChkStreamStateStore")
                        .withKeySerde(valueSerde)
                        .withValueSerde(valueSerde)
                        .withRetention(Duration.ofMinutes(30)))
                .toStream();
    }


    public void sendToRabbitMQ(EqpReversePOJORecord record) {
        if (record == null) {
            logger.warn(">>>>> Record is null, skipping sending to RabbitMQ");
            return;
        }
        try {
            EqpReversePOJO pojo = new EqpReversePOJO();

            pojo.setSimId(String.valueOf(record.getSimId()));
            pojo.setModuleId(String.valueOf(record.getModuleId()));
            pojo.setChkDt(LocalDateTime.now());
            pojo.setCode(Integer.parseInt(String.valueOf(record.getCode())));
            pojo.setDescription(String.valueOf(record.getDescription()));

            // 手动处理 code 属性的类型转换

            pojo.setSource("wb-olp");

            // 使用配置化的交换机和队列名称
            String exchangeName = "qtechImExchange";
            String routingKey = "eqReverseCtrlInfoQueue"; // 此处应该是路由键，而不是队列名称
            rabbitTemplate.convertAndSend(exchangeName, routingKey, objectMapper.writeValueAsString(pojo));

            // 记录关键字段，避免敏感信息泄漏
            logger.info(">>>>> Record sent to RabbitMQ: id={}, source={}", pojo.getSimId(), pojo.getSource());
        } catch (JsonProcessingException | NumberFormatException e) {
            logger.error("Failed to convert Record to JSON or copy properties", e);
        }
    }
}