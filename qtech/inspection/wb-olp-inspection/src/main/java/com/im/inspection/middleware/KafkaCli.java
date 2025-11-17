package com.im.inspection.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.im.qtech.data.avro.record.EqpReversePOJORecord;
import com.im.qtech.data.avro.record.WbOlpRawDataRecord;
import com.im.qtech.data.serde.EqpReversePOJOValueSerializer;
import com.im.qtech.data.serde.WbOlpRawDataRecordSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.im.common.dt.Chronos;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.im.inspection.util.Constant.*;
import static com.im.qtech.data.constant.QtechImBizConstant.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/29 09:01:34
 */

public class KafkaCli {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCli.class);
    private static final ObjectMapper mapper = JsonMapperProvider.getSharedInstance();
    private static final KafkaProducer<Long, EqpReversePOJORecord> producer = createEqReverseCtrlInfoProducer();
    private static final KafkaProducer<Long, WbOlpRawDataRecord> wbOlpRawDataProducer = createWbOlpRawDataProducer();

    public static void sendReverseCtrlInfoToKafka(Dataset<Row> reverseInfoDf) {
        reverseInfoDf.foreachPartition((ForeachPartitionFunction<Row>) partition -> {
            List<ObjectNode> batch = new ArrayList<>();
            while (partition.hasNext()) {
                try {
                    Row row = partition.next();
                    ObjectNode jsonObject = mapper.createObjectNode();

                    // 添加空值检查
                    jsonObject.put("simId", row.getAs(SIM_ID) != null ? row.getAs(SIM_ID).toString() : null);
                    jsonObject.put("source", SOURCE);
                    jsonObject.put("moduleId", row.getAs(MODULE_ID) != null ? row.getAs(MODULE_ID).toString() : null);
                    jsonObject.put("chkDt", row.getAs(CHK_DT) != null ? row.getAs(CHK_DT).toString() : null);
                    jsonObject.put("code", row.getAs(CODE) != null ? (int) row.getAs(CODE) : 0);
                    jsonObject.put("description", row.getAs(DESCRIPTION) != null ? row.getAs(DESCRIPTION).toString() : null);
                    batch.add(jsonObject);

                    if (batch.size() >= 100) {
                        sendReverseCtrlInfoBatchToKafka(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    logger.error(">>>>> Error processing row in partition", e);
                }
            }
            if (!batch.isEmpty()) {
                sendReverseCtrlInfoBatchToKafka(batch);
            }
        });

        // 所有消息发送完成后关闭 `KafkaProducer`
        try {
            closeProducer();
        } catch (Exception e) {
            logger.error(">>>>> Error closing Kafka producer", e);
        }
    }

    private static void sendReverseCtrlInfoBatchToKafka(List<ObjectNode> batch) {
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        for (ObjectNode jsonObject : batch) {
            try {
                String simId = jsonObject.get("simId").asText();
                String source = jsonObject.get("source").asText() == null ? "wb-olp" : jsonObject.get("source").asText();
                String moduleId = jsonObject.get("moduleId").asText();
                int code = jsonObject.get("code").asInt();
                Boolean passed = jsonObject.has("passed") && !jsonObject.get("passed").isNull() ?
                        jsonObject.get("passed").asBoolean() :
                        (code == 0);
                String description = jsonObject.get("description").asText();

                // 如果需要转换为 Instant，可以直接解析格式化后的字符串
                Instant chkDtInstant = null;
                if (jsonObject.get("chkDt").asText() != null && !jsonObject.get("chkDt").isNull()) {
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(jsonObject.get("chkDt").asText(), formatter);
                        chkDtInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    } catch (DateTimeParseException e) {
                        logger.error(">>>>> Failed to parse datetime: {}", jsonObject.get("chkDt").asText(), e);
                        continue; // 跳过这条记录而不是中断整个批次
                    }
                }

                EqpReversePOJORecord value = null;
                if (chkDtInstant != null) {
                    value = new EqpReversePOJORecord(simId, source, moduleId, chkDtInstant, code, passed, null, null, description);
                }

                Long key = Chronos.currentTimestamp();

                ProducerRecord<Long, EqpReversePOJORecord> record = new ProducerRecord<>(KAFKA_WB_OLP_CHK_RES_TOPIC, key, value);

                if (value != null) {
                    producer.send(record);
                } else {
                    logger.warn(">>>>> Skipping null record for simId: {}", simId);
                }
            } catch (Exception e) {
                logger.error(">>>>> Error sending reverse control info to Kafka: {}", jsonObject, e);
            }
        }
    }

    private static KafkaProducer<Long, EqpReversePOJORecord> createEqReverseCtrlInfoProducer() {
        // Properties props = new Properties();
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EqpReversePOJOValueSerializer.class);

        return new KafkaProducer<>(props);
    }

    private static KafkaProducer<Long, WbOlpRawDataRecord> createWbOlpRawDataProducer() {
        // Properties props = new Properties();
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WbOlpRawDataRecordSerializer.class);

        return new KafkaProducer<>(props);
    }

    private static void closeProducer() {
        producer.close();
    }

    private static void closeWbOlpRawDataProducer() {
        wbOlpRawDataProducer.close();
    }

    public static void sendWbOlpRawDataToKafka(Dataset<Row> coordinateDetailDf) {
        coordinateDetailDf.foreachPartition((ForeachPartitionFunction<Row>) partition -> {
            List<ObjectNode> batch = new ArrayList<>();
            while (partition.hasNext()) {
                try {
                    Row row = partition.next();
                    ObjectNode jsonObject = mapper.createObjectNode();
                    jsonObject.put("dt", row.getAs("dt") != null ? row.getAs("dt").toString() : null);
                    jsonObject.put("simId", row.getAs("sim_id") != null ? row.getAs("sim_id").toString() : null);
                    jsonObject.put("moduleId", row.getAs("module_id") != null ? row.getAs("module_id").toString() : null);
                    jsonObject.put("wireId", row.getAs("wire_id") != null ? (int) row.getAs("wire_id") : 0);
                    jsonObject.put("leadX", row.getAs("lead_x") != null ? row.getAs("lead_x").toString() : null);
                    jsonObject.put("leadY", row.getAs("lead_y") != null ? row.getAs("lead_y").toString() : null);
                    jsonObject.put("padX", row.getAs("pad_x") != null ? row.getAs("pad_x").toString() : null);
                    jsonObject.put("padY", row.getAs("pad_y") != null ? row.getAs("pad_y").toString() : null);
                    jsonObject.put("checkPort", row.getAs("check_port") != null ? (int) row.getAs("check_port") : 0);
                    jsonObject.put("piecesIndex", row.getAs("pieces_index") != null ? (int) row.getAs("pieces_index") : 0);
                    batch.add(jsonObject);
                    if (batch.size() >= 100) {
                        sendWbOlpRawDataBatchToKafka(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    logger.error(">>>>> Error processing row in partition", e);
                }
            }
            if (!batch.isEmpty()) {
                sendWbOlpRawDataBatchToKafka(batch);
            }
        });

        // 所有消息发送完成后关闭 `KafkaProducer`
        try {
            closeWbOlpRawDataProducer();
        } catch (Exception e) {
            logger.error(">>>>> Error closing Kafka producer", e);
        }
    }

    private static void sendWbOlpRawDataBatchToKafka(List<ObjectNode> batch) {
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.n");

        for (ObjectNode jsonObject : batch) {
            try {
                Instant dt = null;
                if (jsonObject.get("dt") != null && !jsonObject.get("dt").isNull()) {
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(jsonObject.get("dt").asText(), formatter);
                        dt = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    } catch (DateTimeParseException e) {
                        logger.error(">>>>> Failed to parse datetime: {}", jsonObject.get("dt").asText(), e);
                        continue; // 跳过这条记录而不是中断整个批次
                    }
                }

                // 检查必要字段是否为空
                if (dt == null ||
                        jsonObject.get("simId") == null || jsonObject.get("simId").isNull() ||
                        jsonObject.get("moduleId") == null || jsonObject.get("moduleId").isNull()) {
                    logger.warn(">>>>> Skipping record due to missing required fields: dt={}, simId={}, moduleId={}",
                            dt,
                            jsonObject.get("simId") != null ? jsonObject.get("simId").asText() : "null",
                            jsonObject.get("moduleId") != null ? jsonObject.get("moduleId").asText() : "null");
                    continue;
                }

                WbOlpRawDataRecord value = new WbOlpRawDataRecord(
                        dt,
                        jsonObject.get("simId").asText(),
                        jsonObject.get("moduleId").asText(),
                        jsonObject.get("wireId").asInt(),
                        jsonObject.get("leadX").asText(),
                        jsonObject.get("leadY").asText(),
                        jsonObject.get("padX").asText(),
                        jsonObject.get("padY").asText(),
                        jsonObject.get("checkPort").asInt(),
                        jsonObject.get("piecesIndex").asInt()
                );
                Long key = Instant.now().toEpochMilli();

                ProducerRecord<Long, WbOlpRawDataRecord> record = new ProducerRecord<>(KAFKA_WB_OLP_RAW_DATA_TOPIC, key, value);
                wbOlpRawDataProducer.send(record);
            } catch (Exception e) {
                logger.error(">>>>> Error sending record to Kafka: {}", jsonObject, e);
            }
        }
    }
}