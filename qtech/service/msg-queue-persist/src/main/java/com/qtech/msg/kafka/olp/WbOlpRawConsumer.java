package com.qtech.msg.kafka.olp;

import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.im.qtech.data.dto.param.WbOlpRawData;
import com.lmax.disruptor.dsl.Disruptor;
import com.qtech.msg.config.thread.TaskDispatcher;
import com.qtech.msg.disruptor.WbOlpRawDataEvent;
import com.qtech.msg.utils.MsgRedisDeduplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.im.qtech.data.constant.QtechImBizConstant.*;
import static com.qtech.msg.utils.MessageKeyUtils.safeToString;
import static com.qtech.msg.utils.MessageKeyUtils.sha256;

/**
 * WbOlp原始数据消费者
 * 处理来自WB OLP设备的原始数据，进行去重和转发到Disruptor处理队列
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

    /**
     * 批量消费WB OLP原始数据
     *
     * @param records        批量的消息记录
     * @param acknowledgment Kafka确认对象
     */
    @KafkaListener(topics = WB_OLP_RAW_DATA_KAFKA_TOPIC, containerFactory = "WbOlpRawDataContainerFactory")
    public void consume(List<ConsumerRecord<Long, WbOlpRawDataRecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(records.stream()
                .map(record -> CompletableFuture.runAsync(
                        () -> processRecordWithFallback(record),
                        taskDispatcher.getExecutor(TaskDispatcher.TaskPriority.NORMAL)
                )).toArray(CompletableFuture[]::new));

        taskDispatcher.dispatch(() -> {
            try {
                allTasks.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info(">>>>> All records processed successfully. Acknowledging Kafka.");
                acknowledgment.acknowledge();
            } catch (TimeoutException e) {
                log.error(">>>>> Kafka record processing timeout after {}s. NOT acknowledging to retry.", TASK_TIMEOUT_SECONDS);
                // 不调用 acknowledgment.acknowledge() 以允许重试
            } catch (Exception e) {
                log.error(">>>>> Kafka record processing failed. NOT acknowledging to retry.", e);
                // 不调用 acknowledgment.acknowledge() 以允许重试
            }
        }, TaskDispatcher.TaskPriority.NORMAL);
    }

    /**
     * 带降级处理的记录处理方法
     *
     * @param record Kafka消费者记录
     */
    private void processRecordWithFallback(ConsumerRecord<Long, WbOlpRawDataRecord> record) {
        try {
            processRecord(record);
        } catch (Exception e) {
            log.error(">>>>> Error processing record. Entering fallback. Topic={}, Partition={}, Offset={}, Exception={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            handleFailedRecord(record, e);
        }
    }

    /**
     * 处理单条记录
     *
     * @param record Kafka消费者记录
     */
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
                log.info(">>>>> Data processed and added to Disruptor: {}", data);
            } catch (Exception ex) {
                log.error(">>>>> Disruptor publish failed, pushing to DLQ, key={}", key, ex);
                kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
            }
        } else {
            log.info(">>>>> Duplicate data skipped, key: {}", key);
        }
    }

    /**
     * 处理失败的记录，将其发送到死信队列
     *
     * @param record Kafka消费者记录
     * @param ex     异常信息
     */
    private void handleFailedRecord(ConsumerRecord<Long, WbOlpRawDataRecord> record, Exception ex) {
        try {
            kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
            log.warn(">>>>> [DLQ] Record pushed to DLQ: simId={}, reason={}",
                    Optional.ofNullable(record.value()).map(WbOlpRawDataRecord::getSimId).orElse("null"),
                    ex.getMessage());
        } catch (Exception e) {
            log.error(">>>>> Failed to push record to DLQ: simId={}",
                    Optional.ofNullable(record.value()).map(WbOlpRawDataRecord::getSimId).orElse("null"), e);
        }
    }

    /**
     * 生成Redis去重键
     *
     * @param record WbOlpRawDataRecord对象
     * @return Redis键
     */
    private String generateRedisKey(WbOlpRawDataRecord record) {
        if (record != null && record.getDt() != null) {
            String formattedDt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH)
                    .format(record.getDt().atZone(ZoneId.systemDefault()));

            String rawKey = String.join("|",
                    safeToString(record.getSimId()),
                    formattedDt,
                    safeToString(record.getModuleId()),
                    String.valueOf(record.getWireId()),
                    String.valueOf(record.getCheckPort()),
                    String.valueOf(record.getPiecesIndex())
            );

            return sha256(rawKey);
        }
        return UUID.randomUUID().toString(); // fallback
    }

    /**
     * 将WbOlpRawDataRecord转换为WbOlpRawData对象
     *
     * @param r WbOlpRawDataRecord对象
     * @return WbOlpRawData对象
     */
    private WbOlpRawData convertToWbOlpRawData(WbOlpRawDataRecord r) {
        WbOlpRawData data = new WbOlpRawData();
        data.setDt(LocalDateTime.now());
        data.setSimId(String.valueOf(r.getSimId()));
        data.setModuleId(String.valueOf(r.getModuleId()));
        data.setWireId(r.getWireId());
        data.setLeadX(String.valueOf(r.getLeadX()));
        data.setLeadY(String.valueOf(r.getLeadY()));
        data.setPadX(String.valueOf(r.getPadX()));
        data.setPadY(String.valueOf(r.getPadY()));
        data.setCheckPort(r.getCheckPort());
        data.setPiecesIndex(r.getPiecesIndex());
        return data;
    }
}
