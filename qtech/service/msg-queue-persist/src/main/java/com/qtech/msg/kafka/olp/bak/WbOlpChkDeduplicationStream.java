package com.qtech.msg.kafka.olp.bak;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/08/24 09:22:59
 * desc   :  打线图比对结果去重
 */

// @Component
// @EnableKafkaStreams
public class WbOlpChkDeduplicationStream {
    private final RabbitTemplate rabbitTemplate;

    public WbOlpChkDeduplicationStream(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public KStream<String, String> processStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream("input-topic");

        // 使用state store来存储上一次的结果
        streamsBuilder.addStateStore(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("dedup-store"),
                        Serdes.String(),
                        Serdes.String()
                )
        );

        stream
                .transformValues(() -> new WbOlpChkDeduplicationTransformer("dedup-store"), "dedup-store")
                .filter((key, value) -> value != null) // 过滤掉去重后的null值
                .peek((key, value) -> sendToRabbitMQ(value)) // 推送去重后的消息到RabbitMQ
                .to("output-topic", Produced.with(Serdes.String(), Serdes.String())); // 将去重后的结果推送到Kafka的其他topic，或你可以忽略这一行

        return stream;
    }

    private void sendToRabbitMQ(String message) {
        rabbitTemplate.convertAndSend("qtechImExchange", "eqReverseCtrlInfoQueue", message);
    }
}
