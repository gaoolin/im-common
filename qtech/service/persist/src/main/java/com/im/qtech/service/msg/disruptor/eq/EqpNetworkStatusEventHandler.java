package com.im.qtech.service.msg.disruptor.eq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.dto.net.EqpNetworkStatus;
import com.im.qtech.service.msg.persist.olp.DeadLetterQueueService;
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

import static com.im.qtech.data.constant.QtechImBizConstant.DEVICE_ONLINE_STATUS_REDIS_KEY_PREFIX;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 11:03:30
 */

@Slf4j
// @Service
public class EqpNetworkStatusEventHandler implements EventHandler<EqpNetworkStatusEvent>, LifecycleAware {
    public static final long DEVICE_ONLINE_STATUS_REDIS_EXPIRE_SECONDS = 60;
    private static final int BATCH_SIZE = 100;
    private static final Set<String> SPECIAL_DEVICE_TYPES_AA = new HashSet<>(Arrays.asList("LINUXRSAA"));
    private final List<EqpNetworkStatus> buffer = new ArrayList<>(BATCH_SIZE);

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
    public void onEvent(EqpNetworkStatusEvent event, long sequence, boolean endOfBatch) {
        EqpNetworkStatus eqpNetworkStatus = event.getData();
        String message = event.getRaw();

        try {
            String deviceId = eqpNetworkStatus.getDeviceId();
            String deviceType = eqpNetworkStatus.getDeviceType().toUpperCase();
            String remoteControlEq = eqpNetworkStatus.getDeviceStatus();

            // 设备状态赋默认值
            updateDeviceStatus(eqpNetworkStatus, deviceType, remoteControlEq);

            // Redis + Kafka
            handleKafka(eqpNetworkStatus, deviceId);

        } catch (Exception e) {
            log.error(">>>>> Error processing message in EventHandler: " + message, e);
        }
    }

    private void updateDeviceStatus(EqpNetworkStatus eqpNetworkStatus, String deviceType, String remoteControlEq) {
        if (SPECIAL_DEVICE_TYPES_AA.contains(deviceType) && remoteControlEq == null) {
            log.debug(">>>>> Device policy " + deviceType + " does not support remote control.");
            eqpNetworkStatus.setDeviceStatus("2");
        } else if (remoteControlEq == null) {
            eqpNetworkStatus.setDeviceStatus("999");
        } else if ("WB".equals(deviceType)) {
            if (!remoteControlEq.matches("\\d+")) {
                eqpNetworkStatus.setDeviceStatus("999");
            }
        }

        // 防止 null status 写入（status 有自定义序列化器）
        if (StringUtils.isEmpty(eqpNetworkStatus.getNetStatus())) {
            eqpNetworkStatus.setNetStatus("999");
        }

        eqpNetworkStatus.setLastUpdated(LocalDateTime.now());
    }

    private void handleRedisAndKafka(EqpNetworkStatus eqpNetworkStatus, String deviceId) {
        String redisKey = DEVICE_ONLINE_STATUS_REDIS_KEY_PREFIX + deviceId;
        String redisVal = stringRedisTemplate.opsForValue().get(redisKey);

        try {
            JsonNode redisJson = objectMapper.readTree(redisVal);
            // 注意：Remote_control 是 JSON 中的字段名
            String remoteControlInRedis = redisJson.get("Remote_control").asText();
            if (!remoteControlInRedis.equals(eqpNetworkStatus.getDeviceStatus())) {
                forwardToDeviceCluster(eqpNetworkStatus, deviceId, redisKey);
            } else {
                log.debug(">>>>> Device already exists in Redis, skipping...");
            }
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error parsing Redis value: " + redisVal, e);
        }
    }

    private void handleKafka(EqpNetworkStatus eqpNetworkStatus, String deviceId) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpNetworkStatus);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpNetworkStatus to JSON: " + eqpNetworkStatus, e);
        }
    }

    private void forwardToDeviceCluster(EqpNetworkStatus eqpNetworkStatus, String deviceId, String redisKey) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpNetworkStatus);
            stringRedisTemplate.opsForValue().set(redisKey, deviceDataJson, DEVICE_ONLINE_STATUS_REDIS_EXPIRE_SECONDS, TimeUnit.SECONDS);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpNetworkStatus to JSON: " + eqpNetworkStatus, e);
        }
    }

    private void forwardToKafkaCluster(EqpNetworkStatus eqpNetworkStatus, String deviceId, String redisKey) {
        try {
            String deviceDataJson = objectMapper.writeValueAsString(eqpNetworkStatus);
            targetKafkaTemplate.send(targetTopic, deviceId, deviceDataJson);
            log.debug(">>>>> Forwarded message to target cluster: {}", deviceDataJson);
        } catch (JsonProcessingException e) {
            log.error(">>>>> Error converting EqpNetworkStatus to JSON: " + eqpNetworkStatus, e);
        }
    }

    private void flush() {
        try {
            if (!buffer.isEmpty()) {
                buffer.forEach(deadLetterQueueService::sendEqNetworkStatusToDLQ);
                buffer.clear();
            }
        } catch (Exception e) {
            log.error(">>>>> Error saving batch to EqOnlineStatusToDLQ", e);
            buffer.clear();
        }
    }

    @Override
    public void onStart() {
        log.info(">>>>> EqpNetworkStatusEventHandler started");
    }

    @Override
    public void onShutdown() {
        flush();
        log.info(">>>>> EqpNetworkStatusEventHandler shutdown");
    }
}
