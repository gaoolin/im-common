package com.im.qtech.chk.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.chk.model.EqpContextInfoImpl;
import com.im.qtech.chk.service.ParamCheckService;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka消息消费者
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */

public class KafkaMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private ConfigurationManager configManager;
    private ParamCheckService paramCheckService;
    private ExecutorService executorService;
    private AtomicBoolean running = new AtomicBoolean(false);
    private ObjectMapper objectMapper = new ObjectMapper();

    // Kafka相关组件（实际需要添加Kafka客户端依赖）
    // private KafkaConsumer<String, String> consumer;

    public KafkaMessageConsumer(ConfigurationManager configManager, ParamCheckService paramCheckService) {
        this.configManager = configManager;
        this.paramCheckService = paramCheckService;
    }

    /**
     * 开始消费Kafka消息
     */
    public void startConsuming() {
        if (running.compareAndSet(false, true)) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::consumeMessages);
            logger.info("Kafka消费者已启动");
        }
    }

    /**
     * 消费消息的主循环
     */
    private void consumeMessages() {
        try {
            // 获取Kafka配置
            String bootstrapServers = configManager.getProperty("kafka.bootstrap.servers", "localhost:9092");
            String topic = configManager.getProperty("kafka.topic", "equipment-param-topic");
            String groupId = configManager.getProperty("kafka.group.id", "aa-param-check-group");

            // 配置Kafka消费者属性
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("group.id", groupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");

            // 创建Kafka消费者
            // consumer = new KafkaConsumer<>(props);
            // consumer.subscribe(Arrays.asList(topic));

            logger.info("开始消费Kafka消息，主题: {}, 消费组: {}", topic, groupId);

            // 消费消息循环
            while (running.get()) {
                try {
                    // ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    // for (ConsumerRecord<String, String> record : records) {
                    //     processMessage(record.value());
                    // }

                    // 模拟处理过程
                    Thread.sleep(100);
                } catch (Exception e) {
                    if (running.get()) {
                        logger.error("消费Kafka消息时出错", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Kafka消费者异常", e);
        } finally {
            // if (consumer != null) {
            //     consumer.close();
            // }
            logger.info("Kafka消费者已关闭");
        }
    }

    /**
     * 处理接收到的消息
     */
    private void processMessage(String message) {
        try {
            // 解析消息
            EqpContextInfoImpl param = parseMessage(message);

            // 处理参数检查
            paramCheckService.checkEquipmentParam(param);

        } catch (Exception e) {
            logger.error("处理消息失败: {}", message, e);
        }
    }

    /**
     * 解析消息
     */
    private EqpContextInfoImpl parseMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            EqpContextInfoImpl param = new EqpContextInfoImpl();
            param.setDeviceId(jsonNode.get("deviceId").asText());

            return param;
        } catch (Exception e) {
            logger.error("解析消息失败: {}", message, e);
            throw new RuntimeException("消息解析失败", e);
        }
    }

    /**
     * 停止消费
     */
    public void stopConsuming() {
        if (running.compareAndSet(true, false)) {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            logger.info("Kafka消费者停止指令已发送");
        }
    }
}