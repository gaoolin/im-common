package com.qtech.msg.kafka.olp;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/24 16:16:06
 * desc   :
 * <p>
 * 安全地把对象转成字符串，避免出现null
 */
// @Slf4j
// @EnableKafka
// @Service
/*
public class WbOlpChkConsumer {
    private static final long TASK_TIMEOUT_SECONDS = 10;

    @Autowired
    private RBloomFilter<String> bloomFilter;

    @Autowired
    @Qualifier("wbOlpChkDisruptor")
    private Disruptor<WbOlpChkEvent> disruptor;

    @Autowired
    private TaskDispatcher taskDispatcher;

    @Autowired
    private KafkaTemplate<Long, EqReverseCtrlInfoRecord> kafkaTemplate;

    @KafkaListener(topics = WB_OLP_CHECK_KAFKA_TOPIC, containerFactory = "kafkaListenerContainerRecordFactory")
    public void consume(List<ConsumerRecord<Long, EqReverseCtrlInfoRecord>> records, Acknowledgment acknowledgment) {
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
                log.error(">>>>> Kafka record processing timeout after {}s. Kafka will NOT be acked.", TASK_TIMEOUT_SECONDS);
            } catch (Exception e) {
                log.error(">>>>> Kafka record processing failed. Kafka will NOT be acked.", e);
            }
        }, TaskDispatcher.TaskPriority.NORMAL);
    }

    private void processRecordWithFallback(ConsumerRecord<Long, EqReverseCtrlInfoRecord> record) {
        try {
            processRecord(record);
        } catch (Exception e) {
            log.error(">>>>> Error processing record. Entering fallback. Topic={}, Partition={}, Offset={}, Exception={}", record.topic(), record.partition(), record.offset(), e.getMessage(), e);

            handleFailedRecord(record, e);
        }
    }

    private void processRecord(ConsumerRecord<Long, EqReverseCtrlInfoRecord> record) {
        EqReverseCtrlInfoRecord value = record.value();
        String key = generateRedisKey(value);

        if (!bloomFilter.contains(key)) {
            log.info(">>>>> New data detected, processing key: {}", key);
            bloomFilter.add(key);

            EqReverseCtrlInfo data = convertToWbOlpChk(value);
            disruptor.publishEvent((event, sequence) -> event.setData(data));
            log.info(">>>>> Data processed and added to Disruptor: {}", data);
        } else {
            log.info(">>>>> Duplicate data skipped, key: {}", key);
        }
    }

    private void handleFailedRecord(ConsumerRecord<Long, EqReverseCtrlInfoRecord> record, Exception e) {
        // Send to DLQ
        kafkaTemplate.send(WB_OLP_CHECK_KAFKA_TOPIC + "-dlq", record.key(), record.value());
        log.warn(">>>>> Failed to process record. Sending to DLQ. Topic={}, Partition={}, Offset={}, Exception={}", record.topic(), record.partition(), record.offset(), e.getMessage());
    }

    private String generateRedisKey(EqReverseCtrlInfoRecord record) {
        if (record != null && record.getChkDt() != null) {
            String formattedDt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH)
                    .format(record.getChkDt().atZone(ZoneId.systemDefault()));

            return String.join("|",
                    safeToString(record.getSimId()),
                    safeToString(record.getProdType()),
                    formattedDt,
                    safeToString(record.getCode()),
                    safeToString(record.getDescription()));
        }
        return UUID.randomUUID().toString();
    }

    */
/**
 * 安全地把对象转成字符串，避免出现null
 *//*

    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private EqReverseCtrlInfo convertToWbOlpChk(EqReverseCtrlInfoRecord value) {
        EqReverseCtrlInfo data = new EqReverseCtrlInfo();
        data.setSimId(String.valueOf(value.getSimId()));
        data.setSource("wb-olp");
        data.setProdType(String.valueOf(value.getProdType()));
        data.setChkDt(new Date(value.getChkDt().toEpochMilli()));
        data.setCode(value.getCode());
        data.setDescription(String.valueOf(value.getDescription()));
        return data;
    }
}*/
