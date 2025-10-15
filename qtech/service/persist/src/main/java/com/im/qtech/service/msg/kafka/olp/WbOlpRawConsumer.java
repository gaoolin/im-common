package com.im.qtech.service.msg.kafka.olp;

import com.im.qtech.service.config.thread.TaskDispatcher;
import com.im.qtech.service.msg.avro.WbOlpRawDataRecord;
import com.im.qtech.service.msg.disruptor.wb.WbOlpRawDataEvent;
import com.im.qtech.service.msg.entity.WbOlpRawData;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.im.qtech.common.constant.QtechImBizConstant.*;
import static com.im.qtech.service.msg.util.MessageKeyUtils.safeToString;
import static com.im.qtech.service.msg.util.MessageKeyUtils.sha256;

/**
 * WbOlp原始数据消费者 - 基于Spring Boot 3和Java 21优化
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 17:06:06
 */
@Slf4j
@EnableKafka
@Service
public class WbOlpRawConsumer {

    private static final long TASK_TIMEOUT_SECONDS = 30;

    @Autowired
    private MsgRedisDeduplicationUtils msgRedisDed;

    @Autowired
    @Qualifier("wbOlpRawDataDisruptor")
    private Disruptor<WbOlpRawDataEvent> disruptor;

    @Autowired
    private TaskDispatcher taskDispatcher;

    @Autowired
    private KafkaTemplate<Long, WbOlpRawDataRecord> kafkaTemplate;

    @KafkaListener(topics = WB_OLP_RAW_DATA_KAFKA_TOPIC,
            containerFactory = "WbOlpRawDataContainerFactory",
            groupId = "im-framework-group")
    public void consume(List<ConsumerRecord<Long, WbOlpRawDataRecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(records.stream()
                .map(record -> CompletableFuture.runAsync(
                        () -> processRecordWithFallback(record),
                        taskDispatcher.getExecutor(TaskDispatcher.TaskPriority.VIRTUAL) // 使用虚拟线程
                )).toArray(CompletableFuture[]::new));

        taskDispatcher.dispatch(() -> {
            try {
                allTasks.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info(">>>>> All records processed successfully. Acknowledging Kafka.");
                acknowledgment.acknowledge();
            } catch (TimeoutException e) {
                log.error(">>>>> Kafka record processing timeout after {}s. FORCIBLY acknowledging to prevent stuck.", TASK_TIMEOUT_SECONDS);
                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.error(">>>>> Kafka record processing failed. FORCIBLY acknowledging.", e);
                acknowledgment.acknowledge();
            }
        }, TaskDispatcher.TaskPriority.NORMAL);
    }

    private void processRecordWithFallback(ConsumerRecord<Long, WbOlpRawDataRecord> record) {
        try {
            processRecord(record);
        } catch (Exception e) {
            log.error(">>>>> Error processing record. Entering fallback. Topic={}, Partition={}, Offset={}, Exception={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            handleFailedRecord(record, e);
        }
    }

    private void processRecord(ConsumerRecord<Long, WbOlpRawDataRecord> record) {
        WbOlpRawDataRecord value = record.value();
        String key = MsgRedisDeduplicationUtils.buildDedupKey(MSG_WB_OLP_KEY_PREFIX, generateRedisKey(value));

        List<String> keys = Collections.singletonList(key);
        List<String> newKeys = msgRedisDed.filterNewKeys(keys, MSG_WB_OLP_REDIS_EXPIRE_SECONDS); // 1小时去重

        if (!newKeys.isEmpty()) {
            log.info(">>>>> New data detected, processing key: {}", key);
            WbOlpRawData data = convertToWbOlpRawData(value);

            try {
                disruptor.publishEvent((event, sequence) -> event.setData(data));
            } catch (Exception ex) {
                log.error(">>>>> Disruptor publish failed, pushing to DLQ, key={}", key, ex);
                kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
            }

            log.info(">>>>> Data processed and added to Disruptor: {}", data);
        } else {
            log.info(">>>>> Duplicate data skipped, key: {}", key);
        }
    }

    private void handleFailedRecord(ConsumerRecord<Long, WbOlpRawDataRecord> record, Exception ex) {
        kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
        log.warn(">>>>> [DLQ] Record pushed to DLQ: simId={}, reason={}",
                Optional.ofNullable(record.value()).map(WbOlpRawDataRecord::getSimId).orElse("null"),
                ex.getMessage());
    }

    private String generateRedisKey(WbOlpRawDataRecord record) {
        if (record != null && record.getDt() != null) {
            String formattedDt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH)
                    .format(record.getDt().atZone(ZoneId.systemDefault()));

            String rawKey = String.join("|",
                    safeToString(record.getSimId()),
                    formattedDt,
                    safeToString(record.getModule()),
                    String.valueOf(record.getLineNo()),
                    String.valueOf(record.getCheckPort()),
                    String.valueOf(record.getPiecesIndex())
            );

            return sha256(rawKey);
        }
        return UUID.randomUUID().toString(); // fallback
    }

    private WbOlpRawData convertToWbOlpRawData(WbOlpRawDataRecord r) {
        WbOlpRawData data = new WbOlpRawData();
        data.setDt(Chronos.now());
        data.setSimId(String.valueOf(r.getSimId()));
        data.setModule(String.valueOf(r.getModule()));
        data.setLineNo(r.getLineNo());
        data.setLeadX(String.valueOf(r.getLeadX()));
        data.setLeadY(String.valueOf(r.getLeadY()));
        data.setPadX(String.valueOf(r.getPadX()));
        data.setPadY(String.valueOf(r.getPadY()));
        data.setCheckPort(r.getCheckPort());
        data.setPiecesIndex(r.getPiecesIndex());
        return data;
    }
}
