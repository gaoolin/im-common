package com.im.qtech.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

@Configuration
@EnableKafka
public class KafkaConfig {
    // Kafka 配置将通过 application.yml 处理
}
