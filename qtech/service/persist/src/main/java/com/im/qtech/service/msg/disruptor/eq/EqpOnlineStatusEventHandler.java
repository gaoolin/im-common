package com.im.qtech.service.msg.disruptor.eq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.service.msg.entity.EqpOnlineStatus;
import com.im.qtech.service.msg.kafka.olp.DeadLetterQueueService;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.im.qtech.common.constant.QtechImBizConstant.DEVICE_ONLINE_STATUS_REDIS_KEY_PREFIX;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 11:03:30
 */

@Slf4j
// @Service
public class EqpOnlineStatusEventHandler implements EventHandler<EqpOnlineStatusEvent>, LifecycleAware {
    public static final long DEVICE_ONLINE_STATUS_REDIS_EXPIRE_SECONDS = 60;
    private static final int BATCH_SIZE = 100;
    private static final Set<String> SPECIAL_DEVICE_TYPES_AA = new HashSet<>(Arrays.asList("LINUXRSAA"));
    private final List<EqpOnlineStatus> buffer = new ArrayList<>(BATCH_SIZE);

    @Autowired
    private DeadLetterQueueService deadLetterQueueService;
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> targetKafkaTemplate;
    @Value("${kafka.consumer.topic:}")
    private String targetTopic;

    // 业务核心代码搬过来
    @Override
    public void onEvent(EqpOnlineStatusEvent event, long sequence, boolean endOfBatch) {
        EqpOnlineStatus eqpOnlineStatus = event.getData();
        String message = event.getRaw();

        try {
            String deviceId = eqpOnlineStatus.getDeviceId();
            String deviceType = eqpOnlineStatus.getDeviceType().toUpperCase();
            String remoteControlEq = eqpOnlineStatus.getRemoteControl();

            // 设备状态赋默认值
            updateDeviceStatus(eqpOnlineStatus, deviceType, remoteControlEq);

            // Redis + Kafka
            handleKafka(eqpOnlineStatus, deviceId);

        } catch (Exception e) {
            log.error(">>>>> Error processing message in EventHandler: " + message, e);
        }
    }

    private void updateDeviceStatus(EqpOnlineStatus eqpOnlineStatus, String deviceType, String remoteControlEq) {
        if (SPECIAL_DEVICE_TYPES_AA.contains(deviceType) && remoteControlEq == null) {
            log.debug(">>>>> Device type " + deviceType + " does not support remote control.");
            eqpOnlineStatus.setRemoteControl("2");
        } else if (remoteControlEq == null) {
            eqpOnlineStatus.setRemoteControl("999");
        } else if ("WB".equals(deviceType)) {
            if (!remoteControlEq.matches("\\d+")) {
                eqpOnlineStatus.setRemoteControl("999");
            }
        }

        // 防止 null status 写入（status 有自定义序列化器）
        if (StringUtils.isEmpty(eqpOnlineStatus.getStatus())) {
            eqpOnlineStatus.setStatus("999");
        }

        eqpOnlineStatus.setLastUpdated(LocalDateTime.now());
    }

    private void handleRedisAndKafka(EqpOnlineStatus eqpOnlineStatus, String deviceId) {
        String redisKey = DEVICE_ONLINE_STATUS_REDIS_KEY_PREFIX + deviceId;
        String redisVal = stringRedisTemplate.opsForValue().get(redisKey);

        try {
            JsonNode redisJson = objectMapper.readTree(redisVal);
            // 注意：Remote_control 是 JSON 中的字段名
            String remoteControlInRedis = redisJson.get("Remote_control").asText();
            if (!remoteControlInRedis.equals(eqpOnlineStatus.getRemoteControl())) {
                forwardToDeviceCluster(eqpOnlineStatus, deviceId, redisKey);
            } else {
                log.debug(">>>>> Device already exists in Redis, skipping...");
            }
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error parsing Redis value: " + redisVal, e);
        }
    }

    private void handleKafka(EqpOnlineStatus eqpOnlineStatus, String deviceId) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpOnlineStatus);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpOnlineStatus to JSON: " + eqpOnlineStatus, e);
        }
    }

    private void forwardToDeviceCluster(EqpOnlineStatus eqpOnlineStatus, String deviceId, String redisKey) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpOnlineStatus);
            stringRedisTemplate.opsForValue().set(redisKey, deviceDataJson, DEVICE_ONLINE_STATUS_REDIS_EXPIRE_SECONDS, TimeUnit.SECONDS);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpOnlineStatus to JSON: " + eqpOnlineStatus, e);
        }
    }

    private void forwardToKafkaCluster(EqpOnlineStatus eqpOnlineStatus, String deviceId, String redisKey) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpOnlineStatus);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpOnlineStatus to JSON: " + eqpOnlineStatus, e);
        }
    }

    private void flush() {
        try {
            if (!buffer.isEmpty()) {
                buffer.forEach(deadLetterQueueService::sendEqOnlineStatusToDLQ);
                buffer.clear();
            }
        } catch (Exception e) {
            log.error(">>>>> Error saving batch to EqOnlineStatusToDLQ", e);
            buffer.clear();
        }
    }

    @Override
    public void onStart() {
        log.info(">>>>> EqpOnlineStatusEventHandler started");
    }

    @Override
    public void onShutdown() {
        flush();
        log.info(">>>>> EqpOnlineStatusEventHandler shutdown");
    }
}
