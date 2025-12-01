package com.qtech.msg.kafka.olp.bak;

import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.qtech.msg.service.IEqpReverseInfoService;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.im.qtech.data.constant.QtechImBizConstant.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/29 09:58:54
 */

// @EnableKafka
// @Service
public class WbOlpChkDeduplicationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpChkDeduplicationConsumer.class);
    private static final int BATCH_SIZE = 100; // 批处理的大小
    // 用于存储批量处理的 List
    private final List<EqpReversePOJO> messageList = new ArrayList<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, EqpReversePOJO> eqReverseCtrlInfoRedisTemplate;

    @Autowired
    private IEqpReverseInfoService eqReverseCtrlInfoService;

    @KafkaListener(topics = WB_OLP_CHECK_KAFKA_TOPIC,
            groupId = "wb-olp-chk-consumer-group",
            containerFactory = "kafkaListenerContainerRecordFactory")
    public void consume(ConsumerRecord<Long, EqpReversePOJORecord> record) throws Exception {
        String redisKey = generateRedisKey(record.value());

        // 检查 Redis 中是否存在该 key
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_OLP_CHECK_DUPLICATION_KEY_PREFIX + redisKey))) {
            // 如果 Redis 中没有该 key，先保存到 Redis
            String jsonRecord = convertRecordToJson(record.value());
            stringRedisTemplate.opsForValue().set(REDIS_OLP_CHECK_DUPLICATION_KEY_PREFIX + redisKey, jsonRecord, 30, TimeUnit.MINUTES);
            // redisTemplate.opsForValue().set(REDIS_OLP_CHECK_DUPLICATION_KEY_PREFIX + redisKey, objectMapper.writeValueAsString(record.value()), 30, TimeUnit.MINUTES);

            // 然后将数据转换为 EqReverseCtrlInfo 并添加到 List
            EqpReversePOJO pojo = convertToEqReverseCtrlInfo(record.value());

            // 过渡
            // redisTemplate.opsForValue().set(REDIS_OLP_CHECK_WB_OLP_KEY_PREFIX + eqInfo.getSimId(), eqInfo.getCode() + "*" + eqInfo.getDescription(), 30, TimeUnit.MINUTES);
            // eqReverseCtrlInfoService.upsertPostgresAsync(eqInfo);
            eqReverseCtrlInfoRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX_EQP_REVERSE_INFO + pojo.getSimId(), pojo, Duration.ofMinutes(30));
            eqReverseCtrlInfoService.upsertOracleAsync(pojo);
            eqReverseCtrlInfoService.upsertDorisAsync(pojo);
            eqReverseCtrlInfoService.addWbOlpChkDorisAsync(pojo);
            logger.info(">>>>> WbOlpChk deduplication record consumed: " + record.value());
        }
    }

    private String generateRedisKey(EqpReversePOJORecord record) {
        if (record == null) {
            return null;
        }
        return record.getSimId() +
                "|" + record.getModuleId() +
                "|" + String.valueOf(record.getChkDt()).replace(" ", "").replace(":", "") +
                "|" + record.getCode() +
                "|" + record.getDescription();
    }

    private EqpReversePOJO convertToEqReverseCtrlInfo(EqpReversePOJORecord record) {
        EqpReversePOJO eqInfo = new EqpReversePOJO();
        eqInfo.setSimId(String.valueOf(record.getSimId()));
        eqInfo.setSource("wb-olp");
        eqInfo.setModuleId(String.valueOf(record.getModuleId()));
        eqInfo.setChkDt(LocalDateTime.now());
        eqInfo.setCode(Integer.valueOf(String.valueOf(record.getCode())));
        eqInfo.setDescription(String.valueOf(record.getDescription()));
        return eqInfo;
    }

    private String convertRecordToJson(EqpReversePOJORecord record) throws IOException {
        DatumWriter<EqpReversePOJORecord> writer = new SpecificDatumWriter<>(EqpReversePOJORecord.getClassSchema());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().jsonEncoder(EqpReversePOJORecord.getClassSchema(), outputStream);
        writer.write(record, encoder);
        encoder.flush();
        return outputStream.toString("UTF-8");
    }
}