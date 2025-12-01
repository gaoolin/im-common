package com.im.qtech.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.api.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.im.common.json.JsonMapperProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static com.im.qtech.api.util.MessageKey.generateLstMsgKey;

/**
 * Kafka生产者接口，支持高并发、高可靠性
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

@Slf4j
@RestController
@RequestMapping("/api/list")
public class EqpLstMsgController {

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.eqp-list-message:qtech_im_aa_list_topic}")
    private String topicName;

    public EqpLstMsgController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = JsonMapperProvider.getSharedInstance();
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        if (StringUtils.isBlank(message)) {
            log.warn(">>>>> Received blank message, skip sending to Kafka.");
            return ResponseEntity.badRequest().body("Message is empty or blank");
        }

        try {
            // 生成唯一key
            String key = generateLstMsgKey(message);

            // 正确调用sendMessageAsync方法
            CompletableFuture<SendResult<String, String>> future =
                    kafkaProducerService.sendMessageAsync(topicName, key, message);
            future.join();

            log.info(">>>>> Message sent to Kafka with key: {}", key);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error(">>>>> Failed to send message to Kafka", e);
            return ResponseEntity.status(500).body(">>>>> Failed to send message");
        }
    }
}