package com.im.qtech.service.msg.persist.kafka;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.service.msg.disruptor.wb.WbOlpChkEvent;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.util.MsgRedisDeduplicationUtils;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.im.common.dt.Chronos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.im.qtech.data.constant.QtechImBizConstant.*;
import static com.im.qtech.service.msg.util.MessageKeyUtils.buildRedisKey;
import static com.im.qtech.service.msg.util.MessageKeyUtils.safeToString;

/**
 * WbOlp消费者 - 基于Spring Boot 3和Java 21优化
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:26:38
 */
@Slf4j
@EnableKafka
@Service
public class WbOlpChkConsumer {

    private static final int MAX_PENDING_EVENTS = 512;

    @Autowired
    private MsgRedisDeduplicationUtils msgRedisDed;

    @Autowired
    @Qualifier("wbOlpChkDisruptor")
    private Disruptor<WbOlpChkEvent> disruptor;

    @Autowired
    private KafkaTemplate<Long, EqpReversePOJORecord> kafkaTemplate;

    @KafkaListener(topics = KAFKA_WB_OLP_CHK_RES_TOPIC,
            containerFactory = "EqReverseCtrlInfoContainerFactory",
            groupId = "im-framework-group")
    public void consume(List<ConsumerRecord<Long, EqpReversePOJORecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            // 过滤出新数据并直接处理
            List<String> redisKeys = records.stream()
                    .map(record -> MsgRedisDeduplicationUtils.buildDedupKey(MSG_WB_OLP_KEY_PREFIX, generateRedisKey(record.value())))
                    .collect(Collectors.toList());

            List<String> newKeys = msgRedisDed.filterNewKeys(redisKeys, MSG_WB_OLP_REDIS_EXPIRE_SECONDS);

            // 直接同步处理新数据，避免异步嵌套
            IntStream.range(0, records.size())
                    .filter(i -> newKeys.contains(redisKeys.get(i)))
                    .forEach(i -> processRecordWithFallback(records.get(i)));

            acknowledgment.acknowledge();
            log.info(">>>>> Batch of {} new records processed successfully", newKeys.size());
        } catch (Exception e) {
            log.error(">>>>> Failed to process batch of {} records", records.size(), e);
            acknowledgment.acknowledge(); // 防止Kafka重新投递
        }
    }

    private void processRecordWithFallback(ConsumerRecord<Long, EqpReversePOJORecord> record) {
        try {
            processRecord(record);
        } catch (Exception e) {
            log.error(">>>>> Error processing record. Entering fallback. Topic={}, Partition={}, Offset={}, Exception={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            handleFailedRecord(record, e);
        }
    }

    private void processRecord(ConsumerRecord<Long, EqpReversePOJORecord> record) {
        EqpReversePOJORecord value = record.value();
        EqpReverseInfo data = convertToWbOlpChk(value);

        // 添加背压控制
        applyBackpressure();

        // 使用 tryPublishEvent 避免阻塞
        boolean success = disruptor.getRingBuffer().tryPublishEvent((event, sequence) -> event.setData(data));
        if (!success) {
            log.warn(">>>>> Disruptor buffer full, sending to DLQ");
            handleFailedRecord(record, new RuntimeException("Disruptor buffer full"));
        } else {
            log.info(">>>>> New data processed and added to Disruptor: {}", data.getSimId());
        }
    }

    private void applyBackpressure() {
        // 当 Disruptor 缓冲区接近满载时，短暂等待释放资源
        while (disruptor.getRingBuffer().remainingCapacity() < 10) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void handleFailedRecord(ConsumerRecord<Long, EqpReversePOJORecord> record, Exception e) {
        kafkaTemplate.send(KAFKA_WB_OLP_CHK_RES_TOPIC + "-dlq", record.key(), record.value());
        log.warn(">>>>> Failed to process record. Sending to DLQ. Topic={}, Partition={}, Offset={}, Exception={}",
                record.topic(), record.partition(), record.offset(), e.getMessage());
    }

    /**
     * 构建 Redis 去重 key（SHA-256 签名）
     */
    private String generateRedisKey(EqpReversePOJORecord record) {
        if (record != null && record.getChkDt() != null) {
            String formattedDt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH)
                    .format(record.getChkDt().atZone(ZoneId.systemDefault()));

            return MSG_WB_OLP_KEY_PREFIX + buildRedisKey(
                    record.getSimId(),
                    record.getModuleId(),
                    formattedDt,
                    record.getCode(),
                    record.getDescription()
            );
        }
        return MSG_WB_OLP_KEY_PREFIX + UUID.randomUUID(); // fallback key
    }

    private EqpReverseInfo convertToWbOlpChk(EqpReversePOJORecord value) {
        EqpReverseInfo data = new EqpReverseInfo();
        data.setSimId(safeToString(value.getSimId()));
        data.setSource("wb-olp");
        data.setModuleId(safeToString(value.getModuleId()));
        data.setChkDt(Chronos.now());
        data.setCode(value.getCode());
        data.setPassed(value.getCode() == 0);
        data.setReason("kafka");
        data.setDescription(safeToString(value.getDescription()));
        return data;
    }
}
