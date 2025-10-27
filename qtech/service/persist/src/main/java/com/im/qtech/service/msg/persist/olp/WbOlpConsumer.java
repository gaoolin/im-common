package com.im.qtech.service.msg.persist.olp;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.service.msg.disruptor.wb.WbOlpChkEvent;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.util.MsgRedisDeduplicationUtils;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.im.common.dt.Chronos;
import org.im.common.thread.core.ThreadPoolSingleton;
import org.im.common.thread.task.TaskPriority;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
public class WbOlpConsumer {
    private static final long TASK_TIMEOUT_SECONDS = 30;
    private static final ThreadPoolSingleton singleton = ThreadPoolSingleton.getInstance();

    @Autowired
    private MsgRedisDeduplicationUtils msgRedisDed;

    @Autowired
    @Qualifier("wbOlpChkDisruptor")
    private Disruptor<WbOlpChkEvent> disruptor;

    @Autowired
    private KafkaTemplate<Long, EqpReversePOJORecord> kafkaTemplate;

    @KafkaListener(topics = WB_OLP_CHECK_KAFKA_TOPIC,
            containerFactory = "EqReverseCtrlInfoContainerFactory",
            groupId = "im-framework-group")
    public void consume(List<ConsumerRecord<Long, EqpReversePOJORecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        List<String> redisKeys = records.stream()
                .map(record -> MsgRedisDeduplicationUtils.buildDedupKey(MSG_WB_OLP_KEY_PREFIX, generateRedisKey(record.value())))
                .collect(Collectors.toList());

        List<String> newKeys = msgRedisDed.filterNewKeys(redisKeys, MSG_WB_OLP_REDIS_EXPIRE_SECONDS);

        // 修正1: 通过 HybridTaskDispatcher 获取执行器
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                IntStream.range(0, records.size())
                        .filter(i -> newKeys.contains(redisKeys.get(i)))
                        .mapToObj(i -> CompletableFuture.runAsync(
                                () -> processRecordWithFallback(records.get(i)),
                                singleton.getTaskDispatcher().getExecutor(TaskPriority.IMPORTANT) // 核心业务使用 VIRTUAL
                        ))
                        .toArray(CompletableFuture[]::new)
        );

        // 修正2: 通过 HybridTaskDispatcher 调度任务
        singleton.getTaskDispatcher().dispatch(() -> {
            try {
                allTasks.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info(">>>>> All records processed successfully. Acknowledging Kafka.");
                acknowledgment.acknowledge();
            } catch (TimeoutException e) {
                log.error(">>>>> Kafka record processing timeout after {}s. Kafka will NOT be acked.", TASK_TIMEOUT_SECONDS);
            } catch (Exception e) {
                log.error(">>>>> Kafka record processing failed. Kafka will NOT be acked.", e);
            }
        }, TaskPriority.NORMAL);
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
        disruptor.publishEvent((event, sequence) -> event.setData(data));
        log.info(">>>>> New data processed and added to Disruptor: {}", data);
    }

    private void handleFailedRecord(ConsumerRecord<Long, EqpReversePOJORecord> record, Exception e) {
        kafkaTemplate.send(WB_OLP_CHECK_KAFKA_TOPIC + "-dlq", record.key(), record.value());
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
                    record.getModule(),
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
        data.setModule(safeToString(value.getModule()));
        data.setChkDt(Chronos.now());
        data.setCode(value.getCode());
        data.setPassed(value.getCode() == 0);
        data.setDescription(safeToString(value.getDescription()));
        return data;
    }
}
