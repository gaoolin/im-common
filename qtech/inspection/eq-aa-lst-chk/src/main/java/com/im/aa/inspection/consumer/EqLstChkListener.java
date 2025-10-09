package com.im.aa.inspection.consumer;

/*

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
        kafkaConsumer.subscribe(Collections.singletonList("dto-list-params-test-topic"));
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
            EqpReverseRecord checkResult = initializeCheckResult(actualObj);

            // 获取模板信息
            EqLstTplInfoPO modelInfoObj = getTplInfo(actualObj.getModule());
            EqLstTplDO modelObj = getTpl(actualObj.getModule());

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

    private EqpReverseRecord initializeCheckResult(EqLstParsed actualObj) {
        EqpReverseRecord checkResult = new EqpReverseRecord();
        checkResult.setSource("dto-list");
        checkResult.setSimId(actualObj.getSimId());
        checkResult.setModule(actualObj.getModule());
        checkResult.setChkDt(Chronos.now());
        return checkResult;
    }

    // FIXME : 当redis没有模版信息时，更严谨的逻辑是到数据库中查询 模版信息或模版详情，并更新到Redis中
    private EqLstTplInfoPO getTplInfo(String module) {
        return cache.getParamsTplInfo(module);
    }

    private EqLstTplDO getTpl(String module) {
        return cache.getParamsTpl(module);
    }

    private void compareAndProcessResults(EqLstTplDO modelObj, EqLstParsed actualObj, EqpReverseRecord checkResult, String messageKey) throws JsonProcessingException {
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

    private void handleResult(EqpReverseRecord checkResult, int code, String description, String messageKey) throws JsonProcessingException {
        checkResult.setCode(code);
        checkResult.setDescription(description);
        sendResult(checkResult, messageKey);
    }

    private void sendResult(EqpReverseRecord checkResult, String messageKey) throws JsonProcessingException {
        String jsonString = objectMapper.writeValueAsString(checkResult);
        kafkaProducer.send(new ProducerRecord<>("dto-list-params-checked-test-topic", jsonString));
        logger.info(">>>>> key: {} check message completed, result sent!", messageKey);
    }
}
*/
