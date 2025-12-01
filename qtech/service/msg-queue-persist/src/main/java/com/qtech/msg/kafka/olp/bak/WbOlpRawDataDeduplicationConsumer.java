package com.qtech.msg.kafka.olp.bak;

import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.im.qtech.data.dto.param.WbOlpRawData;
import com.qtech.msg.service.IWbOlpRawDataService;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.im.qtech.data.constant.QtechImBizConstant.REDIS_OLP_RAW_DUPLICATION_KEY_PREFIX;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/29 15:56:10
 */

// @EnableKafka
// @Service
public class WbOlpRawDataDeduplicationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpRawDataDeduplicationConsumer.class);
    private static final int BATCH_SIZE = 1000;
    private final List<WbOlpRawData> messageList = new CopyOnWriteArrayList<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private IWbOlpRawDataService wbOlpRawDataService;

    @KafkaListener(topics = "qtech_im_wb_olp_raw_data_topic",
            groupId = "wb-olp-raw-data-consumer-group",
            containerFactory = "kafkaListenerContainerWbOlpRawDataRecordFactory")
    public void consume(ConsumerRecord<Long, WbOlpRawDataRecord> record) throws Exception {
        String redisKey = generateRedisKey(record.value());

        // 检查 Redis 中是否存在该 key
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_OLP_RAW_DUPLICATION_KEY_PREFIX + redisKey))) {
            String jsonRecord = convertWbOlpRawDataRecordToJson(record.value());
            redisTemplate.opsForValue().set(REDIS_OLP_RAW_DUPLICATION_KEY_PREFIX + redisKey, jsonRecord, 15, TimeUnit.MINUTES);
            WbOlpRawData wbOlpRawData = convertToWbOlpRawData(record.value());

            synchronized (this) {  // 加锁
                messageList.add(wbOlpRawData);
                if (messageList.size() >= BATCH_SIZE) {
                    persistData();
                }
            }
        }
    }

    private synchronized void persistData() {
        wbOlpRawDataService.addWbOlpRawDataBatch(messageList);
        logger.info(">>>>> Persisted {} messages.", 1000);
        // 清空 List
        messageList.clear();
    }

    private String generateRedisKey(WbOlpRawDataRecord record) {
        if (record != null) {
            return record.getSimId() +
                    "|" + String.valueOf(record.getDt()).replace(" ", "").replace(":", "") +
                    "|" + record.getModuleId() +
                    "|" + record.getWireId() +
                    "|" + record.getLeadX() +
                    "|" + record.getLeadY() +
                    "|" + record.getPadX() +
                    "|" + record.getPadY() +
                    "|" + record.getCheckPort() +
                    "|" + record.getPiecesIndex();
        }
        return null;
    }

    private WbOlpRawData convertToWbOlpRawData(WbOlpRawDataRecord wbOlpRawDataRecord) {
        WbOlpRawData wbOlpRawData = new WbOlpRawData();
        wbOlpRawData.setDt(LocalDateTime.now());
        wbOlpRawData.setSimId(String.valueOf(wbOlpRawDataRecord.getSimId()));
        wbOlpRawData.setModuleId(String.valueOf(wbOlpRawDataRecord.getModuleId()));
        wbOlpRawData.setWireId(wbOlpRawDataRecord.getWireId());
        wbOlpRawData.setLeadX(String.valueOf(wbOlpRawDataRecord.getLeadX()));
        wbOlpRawData.setLeadY(String.valueOf(wbOlpRawDataRecord.getLeadY()));
        wbOlpRawData.setPadX(String.valueOf(wbOlpRawDataRecord.getPadX()));
        wbOlpRawData.setPadY(String.valueOf(wbOlpRawDataRecord.getPadY()));
        wbOlpRawData.setCheckPort(wbOlpRawDataRecord.getCheckPort());
        wbOlpRawData.setPiecesIndex(wbOlpRawDataRecord.getPiecesIndex());
        return wbOlpRawData;
    }

    private String convertWbOlpRawDataRecordToJson(WbOlpRawDataRecord record) throws IOException {
        DatumWriter<WbOlpRawDataRecord> writer = new SpecificDatumWriter<>(WbOlpRawDataRecord.getClassSchema());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().jsonEncoder(WbOlpRawDataRecord.getClassSchema(), outputStream);
        writer.write(record, encoder);
        encoder.flush();
        return outputStream.toString("UTF-8");
    }
}