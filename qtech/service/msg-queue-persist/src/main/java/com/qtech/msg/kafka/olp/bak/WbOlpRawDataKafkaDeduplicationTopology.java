package com.qtech.msg.kafka.olp.bak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.im.qtech.data.dto.param.WbOlpRawData;
import com.qtech.msg.serializer.WbOlpRawDataCompositeKey;
import com.qtech.msg.serializer.WbOlpRawDataCompositeKeySerde;
import com.qtech.msg.serializer.WbOlpRawDataRecordSerde;
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
import java.util.Date;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/26 11:09:45
 */

// @Component
// @EnableKafkaStreams
public class WbOlpRawDataKafkaDeduplicationTopology {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpRawDataKafkaDeduplicationTopology.class);

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
        logger.info(">>>>> Creating topology for topic qtech_im_wb_olp_raw_data_topic");
        Serde<WbOlpRawDataCompositeKey> wbOlpRawDataCompositeKeySerde = new WbOlpRawDataCompositeKeySerde();
        Serde<WbOlpRawDataRecord> wbOlpRawDataRecordSerde = new WbOlpRawDataRecordSerde();

        KStream<WbOlpRawDataCompositeKey, WbOlpRawDataRecord> inputStream = streamsBuilder.stream("qtech_im_wb_olp_raw_data_topic", Consumed.with(wbOlpRawDataCompositeKeySerde, wbOlpRawDataRecordSerde));
        inputStream.foreach((key, value) -> logger.info(">>>>> Received record: key = {}, value = {}", key, value));

        inputStream
                .groupByKey()
                .reduce((oldValue, newValue) -> oldValue != null ? oldValue : newValue,
                        Materialized.<WbOlpRawDataCompositeKey, WbOlpRawDataRecord, KeyValueStore<Bytes, byte[]>>as("wbOlpRawDataStreamStateStore")
                                .withKeySerde(wbOlpRawDataCompositeKeySerde)
                                .withValueSerde(wbOlpRawDataRecordSerde)
                                .withRetention(Duration.ofMinutes(30)))
                .toStream()
                .foreach((key, value) -> {
                    logger.info(">>>>> Received record: key = {}, value = {}", key, value);
                    sendToRabbitMQ(value);
                });
    }

    public void sendToRabbitMQ(WbOlpRawDataRecord record) {
        WbOlpRawData wbOlpRawData = new WbOlpRawData();
        wbOlpRawData.setDt(LocalDateTime.now());
        wbOlpRawData.setSimId(String.valueOf(record.getSimId()));
        wbOlpRawData.setModuleId(String.valueOf(record.getModuleId()));
        wbOlpRawData.setWireId(record.getWireId());
        wbOlpRawData.setLeadX(String.valueOf(record.getLeadX()));
        wbOlpRawData.setLeadY(String.valueOf(record.getLeadY()));
        wbOlpRawData.setPadX(String.valueOf(record.getPadX()));
        wbOlpRawData.setPadY(String.valueOf(record.getPadY()));
        wbOlpRawData.setCheckPort(record.getCheckPort());
        wbOlpRawData.setPiecesIndex(record.getPiecesIndex());

        String exchangeName = "qtechImExchange";
        String routingKey = "wbRawDataQueue"; // 此处应该是路由键，而不是队列名称
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, objectMapper.writeValueAsString(wbOlpRawData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}