package com.qtech.msg.kafka.olp;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/24 09:36:26
 * desc   :  wb打线数据消费
 * <p>
 * 安全地把对象转成字符串，避免出现null
 */

/*
布隆过滤器的去重逻辑：
	使用布隆过滤器来检查数据是否已处理过。通过 generateKey 方法生成唯一的去重键（基于多个字段组合）。
	如果数据已存在于布隆过滤器中，则跳过，不进行处理。
	如果数据不在布隆过滤器中，则进行处理并将该键加入布隆过滤器，避免重复处理。
Kafka 消费的改进：
	每次从 Kafka 拉取一批数据进行处理，通过 List<ConsumerRecord<Long, WbOlpRawDataRecord>> 迭代处理。
	每条记录的唯一标识通过 generateKey 方法生成，并用于布隆过滤器去重。
Disruptor 的异步处理：
	每条数据成功去重后，会通过 disruptor.publishEvent 将事件推送到 Disruptor 进行后续的异步处理。
日志记录：
	增加了日志输出，帮助开发人员查看每个消息的处理状态，尤其是在去重逻辑中，可以清楚知道数据是否为重复。
Kafka 消息确认：
	完成批量消息处理后，使用 acknowledgment.acknowledge() 手动确认消息，确保 Kafka 消息在消费后正确地进行确认。
*/
// @Slf4j
// @EnableKafka
// @Service
/*
public class WbOlpRawDataConsumer {

    private static final long TASK_TIMEOUT_SECONDS = 30;
    @Autowired
    private RBloomFilter<String> bloomFilter;
    @Autowired
    @Qualifier("wbOlpRawDataDisruptor")
    private Disruptor<WbOlpRawDataEvent> disruptor;
    @Autowired
    private TaskDispatcher taskDispatcher;
    @Autowired
    private KafkaTemplate<Long, WbOlpRawDataRecord> kafkaTemplate;

    @KafkaListener(topics = WB_OLP_RAW_DATA_KAFKA_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<ConsumerRecord<Long, WbOlpRawDataRecord>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(records.stream()
                .map(record -> CompletableFuture.runAsync(
                        () -> processRecordWithFallback(record),
                        taskDispatcher.getExecutor(TaskDispatcher.TaskPriority.NORMAL)
                )).toArray(CompletableFuture[]::new));

        // 提交一个异步定时任务用于检测超时（使用 normalExecutor）
        taskDispatcher.dispatch(() -> {
            try {
                allTasks.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info(">>>>> All records processed successfully. Acknowledging Kafka.");
                acknowledgment.acknowledge();
            } catch (TimeoutException e) {
                log.error(">>>>> Kafka record processing timeout after {}s. FORCIBLY acknowledging to prevent stuck.", TASK_TIMEOUT_SECONDS);
                acknowledgment.acknowledge();  // 超时也ack，避免kafka阻塞
            } catch (Exception e) {
                log.error(">>>>> Kafka record processing failed. FORCIBLY acknowledging.", e);
                acknowledgment.acknowledge();
            }
        }, TaskDispatcher.TaskPriority.NORMAL);
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
        String key = generateRedisKey(value);

        if (!bloomFilter.contains(key)) {
            log.info(">>>>> New data detected, processing key: {}", key);
            bloomFilter.add(key);

            WbOlpRawData data = convertToWbOlpRawData(value);
            try {
                disruptor.publishEvent((event, sequence) -> event.setData(data));
            } catch (Exception ex) {
                log.error(">>>>> Disruptor publish failed, pushing to DLQ, key={}", key, ex);
                kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
            }
            log.info(">>>>> Data processed and added to Disruptor: {}", data);
        } else {
            log.info(">>>>> Duplicate data skipped, key: {}", key);
        }
    }

    private void handleFailedRecord(ConsumerRecord<Long, WbOlpRawDataRecord> record, Exception ex) {
        // TODO: 可以在这里将异常数据推送到 DLQ（如 kafkaTemplate.send(...)）
        kafkaTemplate.send(WB_OLP_RAW_DATA_KAFKA_TOPIC + "-dlq", record.key(), record.value());
        log.warn(">>>>> [DLQ] Record pushed to DLQ: simId={}, reason={}",
                Optional.ofNullable(record.value()).map(WbOlpRawDataRecord::getSimId).orElse("null"),
                ex.getMessage());
    }

    private String generateRedisKey(WbOlpRawDataRecord record) {
        if (record != null && record.getDt() != null) {
            String formattedDt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH)
                    .format(record.getDt().atZone(ZoneId.systemDefault()));

            return String.join("|",
                    safeToString(record.getSimId()),
                    formattedDt,
                    safeToString(record.getMcId()),
                    String.valueOf(record.getLineNo()),
                    safeToString(record.getLeadX()),
                    safeToString(record.getLeadY()),
                    safeToString(record.getPadX()),
                    safeToString(record.getPadY()),
                    String.valueOf(record.getCheckPort()),
                    String.valueOf(record.getPiecesIndex()));
        }
        return UUID.randomUUID().toString(); // fallback 防止 null 报错
    }

    */
/**
 * 安全地把对象转成字符串，避免出现null
 *//*

    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private WbOlpRawData convertToWbOlpRawData(WbOlpRawDataRecord r) {
        WbOlpRawData data = new WbOlpRawData();
        data.setDt(new Date(r.getDt().toEpochMilli()));
        data.setSimId(String.valueOf(r.getSimId()));
        data.setMcId(String.valueOf(r.getMcId()));
        data.setLineNo(r.getLineNo());
        data.setLeadX(String.valueOf(r.getLeadX()));
        data.setLeadY(String.valueOf(r.getLeadY()));
        data.setPadX(String.valueOf(r.getPadX()));
        data.setPadY(String.valueOf(r.getPadY()));
        data.setCheckPort(r.getCheckPort());
        data.setPiecesIndex(r.getPiecesIndex());
        return data;
    }
}
*/
