package com.qtech.msg.kafka.olp;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.lmax.disruptor.dsl.Disruptor;
import com.qtech.msg.config.thread.TaskDispatcher;
import com.qtech.msg.disruptor.WbOlpChkEvent;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.im.qtech.data.constant.QtechImBizConstant.*;
import static com.qtech.msg.utils.MessageKeyUtils.buildRedisKey;
import static com.qtech.msg.utils.MessageKeyUtils.safeToString;

/**
 * WbOlp检查数据消费者
 * 处理来自WB OLP设备的检查数据，进行去重和转发到Disruptor处理队列
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 */
@Slf4j
@EnableKafka
@Service
public class WbOlpConsumer {
    private static final long TASK_TIMEOUT_SECONDS = 30;

    @Autowired
    private MsgRedisDeduplicationUtils msgRedisDed;

    @Autowired
    @Qualifier("wbOlpChkDisruptor")
    private Disruptor<WbOlpChkEvent> disruptor;

    @Autowired
    private TaskDispatcher taskDispatcher;

    @Autowired
    private KafkaTemplate<Long, EqpReversePOJORecord> kafkaTemplate;

    /**
     * 批量消费WB OLP检查数据
     *
     * @param records        批量的消息记录
     * @param acknowledgment Kafka确认对象
     */
    @KafkaListener(topics = WB_OLP_CHECK_KAFKA_TOPIC, containerFactory = "EqReverseCtrlInfoContainerFactory")
    public void consume(List<ConsumerRecord<Long, EqpReversePOJORecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        List<String> redisKeys = records.stream()
                .map(record -> MsgRedisDeduplicationUtils.buildDedupKey(MSG_WB_OLP_KEY_PREFIX, generateRedisKey(record.value())))
                .collect(Collectors.toList());

        List<String> newKeys = msgRedisDed.filterNewKeys(redisKeys, MSG_WB_OLP_REDIS_EXPIRE_SECONDS);

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                IntStream.range(0, records.size())
                        .filter(i -> newKeys.contains(redisKeys.get(i)))
                        .mapToObj(i -> CompletableFuture.runAsync(
                                () -> processRecordWithFallback(records.get(i)),
                                taskDispatcher.getExecutor(TaskDispatcher.TaskPriority.NORMAL)
                        ))
                        .toArray(CompletableFuture[]::new)
        );

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
    private void processRecordWithFallback(ConsumerRecord<Long, EqpReversePOJORecord> record) {
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
    private void processRecord(ConsumerRecord<Long, EqpReversePOJORecord> record) {
        EqpReversePOJORecord value = record.value();
        EqpReversePOJO data = convertToWbOlpChk(value);
        try {
            disruptor.publishEvent((event, sequence) -> event.setData(data));
            log.info(">>>>> New data processed and added to Disruptor: {}", data);
        } catch (Exception e) {
            log.error(">>>>> Failed to publish to Disruptor: {}", data, e);
            throw e; // 抛出异常以便进入fallback处理
        }
    }

    /**
     * 处理失败的记录，将其发送到死信队列
     *
     * @param record Kafka消费者记录
     * @param e      异常信息
     */
    private void handleFailedRecord(ConsumerRecord<Long, EqpReversePOJORecord> record, Exception e) {
        try {
            kafkaTemplate.send(WB_OLP_CHECK_KAFKA_TOPIC + "-dlq", record.key(), record.value());
            log.warn(">>>>> Failed to process record. Sending to DLQ. Topic={}, Partition={}, Offset={}, Exception={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage());
        } catch (Exception ex) {
            log.error(">>>>> Failed to send record to DLQ. Topic={}, Partition={}, Offset={}",
                    record.topic(), record.partition(), record.offset(), ex);
        }
    }

    /**
     * 构建 Redis 去重 key（SHA-256 签名）
     *
     * @param record EqpReversePOJORecord对象
     * @return Redis键
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

    /**
     * 将EqpReversePOJORecord转换为EqpReversePOJO对象
     *
     * @param value EqpReversePOJORecord对象
     * @return EqpReversePOJO对象
     */
    private EqpReversePOJO convertToWbOlpChk(EqpReversePOJORecord value) {
        EqpReversePOJO data = new EqpReversePOJO();
        data.setSimId(safeToString(value.getSimId()));
        data.setSource("wb-olp");
        data.setModuleId(safeToString(value.getModuleId()));
        data.setChkDt(LocalDateTime.now());
        data.setCode(value.getCode());
        data.setDescription(safeToString(value.getDescription()));
        return data;
    }
}
