package com.im.qtech.service.msg.persist.kafka;

import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.msg.disruptor.wb.WbOlpRawDataEvent;
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

import static com.im.qtech.data.constant.QtechImBizConstant.*;
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

    private static final int MAX_PENDING_EVENTS = 512;

    @Autowired
    private MsgRedisDeduplicationUtils msgRedisDed;

    @Autowired
    @Qualifier("wbOlpRawDataDisruptor")
    private Disruptor<WbOlpRawDataEvent> disruptor;

    @Autowired
    private KafkaTemplate<Long, WbOlpRawDataRecord> kafkaTemplate;

    @KafkaListener(topics = KAFKA_WB_OLP_RAW_DATA_TOPIC,
            containerFactory = "WbOlpRawDataContainerFactory",
            groupId = "im-framework-group")
    public void consume(List<ConsumerRecord<Long, WbOlpRawDataRecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            // 直接同步处理所有记录，避免嵌套异步造成的线程池资源耗尽
            records.forEach(this::processRecordWithFallback);
            acknowledgment.acknowledge();
            log.info(">>>>> Batch of {} records processed successfully", records.size());
        } catch (Exception e) {
            log.error(">>>>> Failed to process batch of {} records", records.size(), e);
            acknowledgment.acknowledge(); // 防止Kafka重新投递造成重复处理
        }
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
            log.debug(">>>>> New data detected, processing key: {}", key);
            WbOlpRawData data = convertToWbOlpRawData(value);

            // 添加背压控制，避免 Disruptor 缓冲区溢出
            applyBackpressure();

            // 使用 tryPublishEvent 避免阻塞，提高响应性
            boolean success = disruptor.getRingBuffer().tryPublishEvent((event, sequence) -> event.setData(data));
            if (!success) {
                log.warn(">>>>> Disruptor buffer full, pushing to DLQ, key={}", key);
                kafkaTemplate.send(KAFKA_WB_OLP_RAW_DATA_TOPIC + "-dlq", record.key(), record.value());
            } else {
                log.debug(">>>>> Data published to Disruptor: {}", data.getSimId());
            }
        } else {
            log.debug(">>>>> Duplicate data skipped, key: {}", key);
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

    private void handleFailedRecord(ConsumerRecord<Long, WbOlpRawDataRecord> record, Exception ex) {
        kafkaTemplate.send(KAFKA_WB_OLP_RAW_DATA_TOPIC + "-dlq", record.key(), record.value());
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
                    safeToString(record.getModuleId()),
                    String.valueOf(record.getWireId()),
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
        data.setSimId(safeToString(r.getSimId()));
        data.setModuleId(safeToString(r.getModuleId()));
        data.setWireId(r.getWireId());

        // 对数值字段进行安全转换
        data.setLeadX(convertToString(r.getLeadX()));
        data.setLeadY(convertToString(r.getLeadY()));
        data.setPadX(convertToString(r.getPadX()));
        data.setPadY(convertToString(r.getPadY()));

        data.setCheckPort(r.getCheckPort());
        data.setPiecesIndex(r.getPiecesIndex());
        return data;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

}
