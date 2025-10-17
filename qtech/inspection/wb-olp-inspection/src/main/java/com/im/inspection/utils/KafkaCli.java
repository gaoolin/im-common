package com.im.inspection.utils;

import com.alibaba.fastjson2.JSONObject;
import com.im.qtech.common.avro.record.EqpReversePOJORecord;
import com.im.qtech.common.avro.record.WbOlpRawDataRecord;
import com.im.qtech.common.serde.EqpReverseInfoRecordValueSerializer;
import com.im.qtech.common.serde.WbOlpRawDataRecordSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.im.common.dt.Chronos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.im.qtech.common.constant.QtechImBizConstant.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/29 09:01:34
 */

public class KafkaCli {
    private static final Logger logger = LoggerFactory.getLogger(KafkaCli.class);
    private static final KafkaProducer<Long, EqpReversePOJORecord> producer = createEqReverseCtrlInfoProducer();
    private static final KafkaProducer<Long, WbOlpRawDataRecord> wbOlpRawDataProducer = createWbOlpRawDataProducer();

    public static void sendReverseCtrlInfoToKafka(Dataset<Row> ctrlInfoDf) {
        ctrlInfoDf.foreachPartition((ForeachPartitionFunction<Row>) partition -> {
            List<JSONObject> batch = new ArrayList<>();
            while (partition.hasNext()) {
                Row row = partition.next();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("simId", row.getAs("sim_id"));
                jsonObject.put("source", "wb-olp");
                jsonObject.put("module", row.getAs("module"));
                jsonObject.put("chkDt", row.getAs("chk_dt"));
                jsonObject.put("code", row.getAs("code"));
                jsonObject.put("description", row.getAs("description"));
                batch.add(jsonObject);

                if (batch.size() >= 100) {
                    sendReverseCtrlInfoBatchToKafka(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                sendReverseCtrlInfoBatchToKafka(batch);
            }
        });
        // 所有消息发送完成后关闭 `KafkaProducer`
        closeProducer();
    }

    private static void sendReverseCtrlInfoBatchToKafka(List<JSONObject> batch) {
        for (JSONObject jsonObject : batch) {
            String simId = jsonObject.getString("simId");
            String source = jsonObject.getString("source") == null ? "wb-olp" : jsonObject.getString("source");
            String module = jsonObject.getString("module");
            String chkDt = jsonObject.getString("chkDt"); // 直接获取格式化后的字符串
            int code = jsonObject.getIntValue("code");
            Boolean passed = jsonObject.getBoolean("passed") != null ?
                    jsonObject.getBoolean("passed") :
                    (code == 0);
            String label = jsonObject.getString("label");
            String reason = jsonObject.getString("reason");
            String description = jsonObject.getString("description");

            // 如果需要转换为 Instant，可以直接解析格式化后的字符串
            Instant chkDtInstant = null;
            if (chkDt != null) {
                try {
                    chkDtInstant = Objects.requireNonNull(Chronos.parseZonedDateTime(chkDt, Chronos.ISO_DATETIME_FORMAT, "Asia/Shanghai")).toInstant();
                } catch (Exception e) {
                    logger.warn("Failed to parse chkDt: {}", chkDt, e);
                }
            }

            EqpReversePOJORecord value = null;
            if (chkDtInstant != null) {
                value = new EqpReversePOJORecord(simId, source, module, chkDtInstant, code, passed, label, reason, description);
            }

            Long key = Chronos.currentTimestamp();

            ProducerRecord<Long, EqpReversePOJORecord> record = new ProducerRecord<>(KAFKA_WB_OLP_CHK_RES_TOPIC, key, value);
            producer.send(record);
        }
    }

    private static KafkaProducer<Long, EqpReversePOJORecord> createEqReverseCtrlInfoProducer() {
        // Properties props = new Properties();
        HashMap<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EqpReverseInfoRecordValueSerializer.class);

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
            List<JSONObject> batch = new ArrayList<>();
            while (partition.hasNext()) {
                Row row = partition.next();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dt", row.getAs("dt"));
                jsonObject.put("simId", row.getAs("sim_id"));
                jsonObject.put("mcId", row.getAs("mc_id"));
                jsonObject.put("lineNo", row.getAs("line_no"));
                jsonObject.put("leadX", row.getAs("lead_x"));
                jsonObject.put("leadY", row.getAs("lead_y"));
                jsonObject.put("padX", row.getAs("pad_x"));
                jsonObject.put("padY", row.getAs("pad_y"));
                jsonObject.put("checkPort", row.getAs("check_port"));
                jsonObject.put("piecesIndex", row.getAs("pieces_index"));
                batch.add(jsonObject);
                if (batch.size() >= 100) {
                    sendWbOlpRawDataBatchToKafka(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                sendWbOlpRawDataBatchToKafka(batch);
            }
        });

        // 所有消息发送完成后关闭 `KafkaProducer`
        closeWbOlpRawDataProducer();
    }

    private static void sendWbOlpRawDataBatchToKafka(List<JSONObject> batch) {
        for (JSONObject jsonObject : batch) {
            WbOlpRawDataRecord value = new WbOlpRawDataRecord(jsonObject.getInstant("dt"), jsonObject.getString("simId"), jsonObject.getString("mcId"), jsonObject.getInteger("lineNo"), jsonObject.getString("leadX"), jsonObject.getString("leadY"), jsonObject.getString("padX"), jsonObject.getString("padY"), jsonObject.getInteger("checkPort"), jsonObject.getInteger("piecesIndex"));
            Long key = Instant.now().toEpochMilli(); // 使用当前时间戳作为 key

            // logger.info("Sending message to Kafka: key = {}, value = {}", key, value);

            ProducerRecord<Long, WbOlpRawDataRecord> record = new ProducerRecord<>(KAFKA_WB_OLP_RAW_DATA_TOPIC, key, value);
            wbOlpRawDataProducer.send(record);
        }
    }
}