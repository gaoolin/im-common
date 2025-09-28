package com.im.aa.inspection.consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.im.aa.inspection.comparator.EqLstComparatorV3;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.reverse.EqReverseCtrlInfo;
import com.im.aa.inspection.entity.tpl.QtechEqLstTpl;
import com.im.aa.inspection.entity.tpl.QtechEqLstTplInfo;
import com.im.aa.inspection.utils.CacheUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.im.semiconductor.common.parameter.comparator.ParameterInspection;
import org.im.util.dt.Chronos;
import org.im.util.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.im.aa.inspection.constants.EqLstChk.PROPERTIES_TO_COMPARE;
import static com.im.aa.inspection.constants.EqLstChk.PROPERTIES_TO_COMPUTE;

public class EqLstChkListener {
    private static final Logger logger = LoggerFactory.getLogger(EqLstChkListener.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.createCustomizedInstance(m -> m.setSerializationInclusion(JsonInclude.Include.NON_NULL).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

    // 静态常量实例，避免重复创建
    private static final EqLstComparatorV3 COMPARATOR = EqLstComparatorV3.getInstance();

    private static final CacheUtil cache = CacheUtil.getInstance();
    private final Producer<String, Object> kafkaProducer;
    private final String kafkaBootstrapServers;

    private Consumer<String, String> kafkaConsumer;
    private ExecutorService executorService;
    private AtomicBoolean running = new AtomicBoolean(false);

    public EqLstChkListener(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.kafkaProducer = createKafkaProducer();
    }

    private static int getStatusCode(ParameterInspection result) {
        boolean hasMissingParams = !result.getEmptyInStandard().isEmpty();
        boolean hasExtraParams = !result.getEmptyInActual().isEmpty();
        boolean hasIncorrectValues = !result.getDifferences().isEmpty();

        // 确定状态码
        int statusCode;
        if (!hasMissingParams && !hasExtraParams && !hasIncorrectValues) {
            statusCode = 0;  // 正常
        } else if (hasMissingParams && !hasExtraParams && !hasIncorrectValues) {
            statusCode = 2;  // 少参数
        } else if (!hasMissingParams && hasExtraParams && !hasIncorrectValues) {
            statusCode = 4;  // 多参数
        } else if (!hasMissingParams && !hasExtraParams && hasIncorrectValues) {
            statusCode = 3;  // 参数值异常
        } else {
            statusCode = 5;  // 复合异常
        }
        return statusCode;
    }

    private Producer<String, Object> createKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    public void startListening() {
        if (running.compareAndSet(false, true)) {
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "EqLstChkListenerThread");
                t.setDaemon(false);
                return t;
            });
            executorService.submit(this::consumeMessages);
            logger.info("EqLstChkListener started");
        }
    }

    public void stopListening() {
        if (running.compareAndSet(true, false)) {
            if (kafkaConsumer != null) {
                kafkaConsumer.wakeup();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
            logger.info("EqLstChkListener stopped");
        }
    }

    private void initializeConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBootstrapServers);
        props.put("group.id", "aaList-do-check-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");

        kafkaConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Collections.singletonList("aa-list-params-test-topic"));
    }

    private void consumeMessages() {
        initializeConsumer();
        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        processMessage(record);
                    }
                } catch (WakeupException e) {
                    if (running.get()) {
                        logger.error("Unexpected WakeupException", e);
                    } else {
                        logger.info("Consumer woken up for shutdown");
                    }
                    break;
                } catch (Exception e) {
                    logger.error("Error consuming message", e);
                }
            }
        } finally {
            if (kafkaConsumer != null) {
                kafkaConsumer.close();
            }
        }
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        try {
            String messageKey = record.key();
            EqLstParsed actualObj = objectMapper.readValue(record.value(), new TypeReference<EqLstParsed>() {
            });
            EqReverseCtrlInfo checkResult = initializeCheckResult(actualObj);

            // 获取模板信息
            QtechEqLstTplInfo modelInfoObj = getTplInfo(actualObj.getProdType());
            QtechEqLstTpl modelObj = getTpl(actualObj.getProdType());

            // 判断模板信息
            if (modelInfoObj == null) {
                handleResult(checkResult, 1, "Missing Template Information.", messageKey);
                return;
            }
            if (modelInfoObj.getStatus() == 0) {
                handleResult(checkResult, 6, "Template Offline.", messageKey);
                return;
            }
            if (modelObj == null) {
                handleResult(checkResult, 7, "Missing Template Detail.", messageKey);
                return;
            }

            // 进行参数对比
            compareAndProcessResults(modelObj, actualObj, checkResult, messageKey);
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }
    }

    private EqReverseCtrlInfo initializeCheckResult(EqLstParsed actualObj) {
        EqReverseCtrlInfo checkResult = new EqReverseCtrlInfo();
        checkResult.setSource("aa-list");
        checkResult.setSimId(actualObj.getSimId());
        checkResult.setProdType(actualObj.getProdType());
        checkResult.setChkDt(Chronos.now());
        return checkResult;
    }

    // FIXME : 当redis没有模版信息时，更严谨的逻辑是到数据库中查询 模版信息或模版详情，并更新到Redis中
    private QtechEqLstTplInfo getTplInfo(String prodType) {
        return cache.getParamsTplInfo(prodType);
    }

    private QtechEqLstTpl getTpl(String prodType) {
        return cache.getParamsTpl(prodType);
    }

    private void compareAndProcessResults(QtechEqLstTpl modelObj, EqLstParsed actualObj, EqReverseCtrlInfo checkResult, String messageKey) throws JsonProcessingException {
        ParameterInspection result = COMPARATOR.compare(modelObj, actualObj, PROPERTIES_TO_COMPARE, PROPERTIES_TO_COMPUTE);

        int statusCode = getStatusCode(result);

        checkResult.setCode(statusCode);
        checkResult.setDescription(buildDescription(result));

        sendResult(checkResult, messageKey);
    }

    private String buildDescription(ParameterInspection result) {
        StringBuilder description = new StringBuilder();

        result.getEmptyInStandard().keySet().stream().sorted().forEach(prop -> description.append(prop).append("-").append(";"));

        result.getEmptyInActual().keySet().stream().sorted().forEach(prop -> description.append(prop).append("+").append(";"));

        result.getDifferences().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String prop = entry.getKey();
            Map.Entry<Object, Object> map = entry.getValue();
            description.append(prop).append(":").append(map.getValue()).append("!=").append(map.getKey()).append(";");
        });

        return description.length() > 0 ? description.toString() : "Ok.";
    }

    private void handleResult(EqReverseCtrlInfo checkResult, int code, String description, String messageKey) throws JsonProcessingException {
        checkResult.setCode(code);
        checkResult.setDescription(description);
        sendResult(checkResult, messageKey);
    }

    private void sendResult(EqReverseCtrlInfo checkResult, String messageKey) throws JsonProcessingException {
        String jsonString = objectMapper.writeValueAsString(checkResult);
        kafkaProducer.send(new ProducerRecord<>("aa-list-params-checked-test-topic", jsonString));
        logger.info(">>>>> key: {} check message completed, result sent!", messageKey);
    }
}
