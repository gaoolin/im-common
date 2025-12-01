package com.im.qtech.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ExecutorService virtualExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public CompletableFuture<SendResult<String, String>> sendMessageAsync(
            String topic, String key, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return kafkaTemplate.send(topic, key, message).get();
            } catch (Exception ex) {
                log.error("消息发送失败: topic={}, key={}", topic, key, ex);
                throw new RuntimeException(ex);
            }
        }, virtualExecutor);
    }

    @Recover
    protected void recover(Exception e, String topic, String key, String message) {
        log.error("消息发送重试失败，开始备份: topic={}, key={}", topic, key, e);
        // 实现备份逻辑
    }
}

