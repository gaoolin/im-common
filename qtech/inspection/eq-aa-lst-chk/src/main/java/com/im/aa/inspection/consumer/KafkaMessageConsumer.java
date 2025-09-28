package com.im.aa.inspection.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.service.ParamCheckService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.im.config.ConfigurationManager;
import org.im.semiconductor.common.dispatcher.MessageHandlerDispatcher;
import org.im.util.dt.Chronos;
import org.im.util.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka消息消费者
 * 负责监听Kafka消息并转发给参数检查服务处理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class KafkaMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    private final ConfigurationManager configManager;
    private final ParamCheckService paramCheckService;
    private final MessageHandlerDispatcher messageHandlerDispatcher;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int threadPoolSize;

    private Consumer<String, Object> consumer;
    private Producer<String, Object> producer;
    private ExecutorService executorService;
    private ExecutorService handlerExecutorService;

    public KafkaMessageConsumer(
            ConfigurationManager configManager,
            ParamCheckService paramCheckService,
            MessageHandlerDispatcher messageHandlerDispatcher) {
        this.configManager = configManager;
        this.paramCheckService = paramCheckService;
        this.messageHandlerDispatcher = messageHandlerDispatcher;
        this.threadPoolSize = configManager.getIntProperty("im.thread.pool.size", 3);
    }

    public void startConsuming() {
        if (running.compareAndSet(false, true)) {
            try {
                initializeKafkaComponents();
                executorService = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "EqLstConsumerThread");
                    t.setDaemon(false);
                    return t;
                });
                handlerExecutorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
                    Thread t = new Thread(r, "EqLstHandlerThread-" + r.hashCode());
                    t.setDaemon(false);
                    return t;
                });
                executorService.submit(this::pollAndProcessRecords);
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                logger.info(">>>>> Started KafkaMessageConsumer successfully");
            } catch (Exception e) {
                logger.error(">>>>> Failed to start KafkaMessageConsumer", e);
                running.set(false);
                throw e;
            }
        } else {
            logger.warn(">>>>> KafkaMessageConsumer is already running");
        }
    }

    private void initializeKafkaComponents() {
        // 初始化消费者
        String bootstrapServers = configManager.getProperty("kafka.bootstrap.servers", "localhost:9092");
        String inputTopic = configManager.getProperty("kafka.input.topic", "qtech_im_aa_list_topic");
        String groupId = configManager.getProperty("kafka.group.id", "aa-param-check-group");

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", groupId);
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("enable.auto.commit", "true");
        consumerProps.put("auto.commit.interval.ms", "1000");

        this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));
        logger.info(">>>>> Subscribing to input topic: {}", inputTopic);

        // 初始化生产者
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(producerProps);
    }

    public void stopConsuming() {
        if (running.compareAndSet(true, false)) {
            logger.info(">>>>> Stopping KafkaMessageConsumer");
            // 发送Wakeup signal来中断poll()
            if (consumer != null) {
                consumer.wakeup();
            }

            try {
                // 等待任务完成
                if (executorService != null) {
                    executorService.shutdown();
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        logger.warn(">>>>> Executor did not terminate in the specified time.");
                        executorService.shutdownNow();
                    }
                }

                // 关闭消息处理线程池
                if (handlerExecutorService != null) {
                    handlerExecutorService.shutdown();
                    if (!handlerExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        logger.warn(">>>>> Handler executor did not terminate in the specified time.");
                        handlerExecutorService.shutdownNow();
                    }
                }
            } catch (InterruptedException e) {
                logger.error(">>>>> Interrupted while waiting for executor to terminate", e);
                if (executorService != null) {
                    executorService.shutdownNow();
                }
                if (handlerExecutorService != null) {
                    handlerExecutorService.shutdownNow();
                }
                Thread.currentThread().interrupt();
            } finally {
                try {
                    if (consumer != null) {
                        consumer.close(Duration.ofSeconds(10));
                        logger.info(">>>>> Kafka consumer closed successfully");
                    }
                    if (producer != null) {
                        producer.close();
                        logger.info(">>>>> Kafka producer closed successfully");
                    }
                } catch (Exception e) {
                    logger.error(">>>>> Error closing Kafka components", e);
                }
            }
            logger.info(">>>>> Stopped KafkaMessageConsumer");
        }
    }

    private void pollAndProcessRecords() {
        logger.info(">>>>> Starting Kafka message consumption loop, running status: {}", running.get());

        if (!running.get()) {
            logger.warn(">>>>> Consumer is not running at start of pollAndProcessRecords, exiting");
            return;
        }

        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
                    logger.debug(">>>>> Polled {} records", records.count());

                    for (ConsumerRecord<String, Object> record : records) {
                        handleRecord(record);
                    }
                } catch (WakeupException e) {
                    if (running.get()) {
                        logger.error(">>>>> Unexpected WakeupException during consumption", e);
                    } else {
                        logger.info(">>>>> Consumer woken up as part of normal shutdown");
                    }
                    break;
                } catch (Exception e) {
                    logger.error(">>>>> Error consuming message", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.info(">>>>> Consumer thread interrupted");
                        break;
                    }
                }
            }
            if (!running.get()) {
                logger.info(">>>>> Kafka message consumption loop exiting due to consumer stop request");
            } else {
                logger.warn(">>>>> Kafka message consumption loop exiting unexpectedly");
            }
        } finally {
            logger.info(">>>>> Exiting Kafka message consumption loop");
        }
    }

    private void handleRecord(ConsumerRecord<String, Object> record) {
        String messageStr = (String) record.value();

        if (messageStr == null) {
            logger.warn(">>>>> Received null or empty message from topic: {}", record.topic());
            return;
        }

        // 使用共享线程池处理消息，避免为每个消息创建新线程池
        Future<?> future = handlerExecutorService.submit(() -> {
            try {
                logger.debug(">>>>> [START] Processing record, offset={}, partition={}", record.offset(), record.partition());

                // 1. 解析消息
                logger.debug(">>>>> Step1: Parsing message...");
                EqLstParsed eqLstParsed = messageHandlerDispatcher.processMessage(EqLstParsed.class, messageStr);
                if (eqLstParsed == null) {
                    logger.warn(">>>>> Step1: Parsing returned null, raw message: {}",
                            messageStr.substring(0, Math.min(messageStr.length(), 75)));
                    return;
                }
                eqLstParsed.setReceivedTime(Chronos.toDate(Chronos.now()));

                // 2. 转 JSON
                logger.debug(">>>>> Step2: Serializing message...");
                String processedMessageStr = objectMapper.writeValueAsString(eqLstParsed);

                // 3. 发到 Kafka (转发到处理后的主题)
                logger.debug(">>>>> Step3: Sending to Kafka...");
                String messageKey = (eqLstParsed.getProdType() != null ? eqLstParsed.getProdType() : "unknown")
                        + "-" + (eqLstParsed.getSimId() != null ? eqLstParsed.getSimId() : "unknown");

                String outputTopic = configManager.getProperty("kafka.output.topic", "aa-list-params-test-topic");
                producer.send(new ProducerRecord<>(outputTopic, messageKey, processedMessageStr));

                // 4. 处理参数检查
                logger.debug(">>>>> Step4: Performing parameter check...");
                paramCheckService.checkEquipmentParam(eqLstParsed);

                logger.info(">>>>> [SUCCESS] key={} processed and dispatched", messageKey);

            } catch (Exception e) {
                logger.error(">>>>> [ERROR] Exception processing message: {}",
                        messageStr.substring(0, Math.min(messageStr.length(), 75)), e);
            }
        });

        try {
            // 超时保护：5秒内必须处理完
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            logger.error(">>>>> [TIMEOUT] Processing record took too long, offset={}, partition={}",
                    record.offset(), record.partition());
            future.cancel(true); // 中断卡死的处理
        } catch (Exception e) {
            logger.error(">>>>> [ERROR] Unexpected exception while processing record, offset={}, partition={}",
                    record.offset(), record.partition(), e);
        }
    }
}
