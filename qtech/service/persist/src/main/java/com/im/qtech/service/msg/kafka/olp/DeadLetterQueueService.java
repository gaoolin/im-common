package com.im.qtech.service.msg.kafka.olp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.im.qtech.data.dto.net.EqpNetworkStatus;
import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.im.qtech.data.constant.QtechImBizConstant.*;

/**
 * 死信队列服务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:26:38
 */

@Slf4j
@Service
public class DeadLetterQueueService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void sendWbOlpRawDataToDLQ(WbOlpRawData data) {
        try {
            kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error(">>>>> Failed to serialize message for DLQ", e);
        }
    }

    public void sendWbOlpChkToDLQ(EqpReverseInfo data) {
        try {
            kafkaTemplate.send(WB_OLP_CHECK_KAFKA_TOPIC + "-dlq", objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error(">>>>> Failed to serialize message for DLQ", e);
        }
    }

    public void sendEqNetworkStatusToDLQ(EqpNetworkStatus data) {
        try {
            kafkaTemplate.send(EQ_NETWORK_STATUS_KAFKA_TOPIC + "-dlq", objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error(">>>>> Failed to serialize message for DLQ", e);
        }
    }
}