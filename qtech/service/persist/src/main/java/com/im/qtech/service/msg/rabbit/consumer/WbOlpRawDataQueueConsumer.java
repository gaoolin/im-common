package com.im.qtech.service.msg.rabbit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.service.msg.entity.WbOlpRawData;
import com.im.qtech.service.msg.service.IWbOlpRawDataService;
import com.rabbitmq.client.Channel;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * WB OLP原始数据队列消费者
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/16 08:33:27
 */

// @Component
public class WbOlpRawDataQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpRawDataQueueConsumer.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IWbOlpRawDataService service;

    public WbOlpRawDataQueueConsumer(IWbOlpRawDataService service) {
        this.service = service;
    }

    @RabbitListener(queues = "wbRawDataQueue", ackMode = "MANUAL")
    public void receive(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            List<WbOlpRawData> messages = validateAndParseMessage(msg, channel, deliveryTag);
            CompletableFuture<Integer> future = service.addWbOlpRawDataBatchAsync(messages);
            if (future.isCompletedExceptionally()) {
                logger.error(">>>>> 批量插入数据失败: {}", messages);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error(">>>>> 处理消息失败: {} - Error: {}", msg, e.getMessage(), e);
            handleException(channel, deliveryTag, e);
        }
    }

    private List<WbOlpRawData> validateAndParseMessage(String msg, Channel channel, long deliveryTag) throws JsonProcessingException {
        try {
            // 使用Jackson解析JSON字符串为List<EqpReverseInfo>
            List<WbOlpRawData> messages = null;
            // 增加JSON解析异常处理，适应Fastjson2
            try {
                // 如果不是数组，则解析为单个对象
                WbOlpRawData singleMessage = objectMapper.readValue(msg, WbOlpRawData.class);
                // 将单个对象封装成列表
                messages = Collections.singletonList(singleMessage);
            } catch (JsonMappingException e) {
                // 尝试直接解析为列表
                messages = objectMapper.readValue(msg, new TypeReference<List<WbOlpRawData>>() {
                });
            } catch (IOException e) {
                // 处理其他异常
                logger.error(">>>>> JSON解析失败, msg: {}", msg, e);
            }
            if (messages == null || messages.isEmpty()) {
                logger.error(">>>>> 解析结果为空, msg: {}", msg);
            }
            return messages;
        } catch (JsonProcessingException e) {
            logger.error(">>>>> JSON解析失败, msg: {}", msg, e);
            throw e;
        }
    }

    private void handleException(Channel channel, long deliveryTag, Exception e) throws IOException {
        // 根据异常类型和业务需求决定是否重新入队或采取其他措施
        channel.basicReject(deliveryTag, true); // 示例中选择重新入队
    }
}