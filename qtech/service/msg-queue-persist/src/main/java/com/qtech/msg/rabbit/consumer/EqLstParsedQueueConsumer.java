package com.qtech.msg.rabbit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtech.im.util.JsonMapperProvider;
import com.qtech.msg.entity.EqpLstParsed;
import com.qtech.msg.service.IEqpLstParsedService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * AA List 参数解析队列消费者
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/16 14:01:32
 */

@Component
public class EqLstParsedQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EqLstParsedQueueConsumer.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IEqpLstParsedService service;

    public EqLstParsedQueueConsumer(IEqpLstParsedService service) {
        this.service = service;
    }

    @RabbitListener(queues = "eqLstParsedQueue", ackMode = "MANUAL")
    public void receive(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        logger.info(">>>>> receive eqLstParsedQueue message: {}", msg);
        try {
            EqpLstParsed singleMessage = validateAndParseMessage(msg, channel, deliveryTag);
            if (singleMessage != null) {
                service.save(singleMessage);
            }

            // 检查通道状态后再执行ACK操作
            if (isChannelOpen(channel, deliveryTag)) {
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            logger.error(">>>>> receive eqLstParsedQueue message error: {}", e.getMessage(), e);
            // 安全地处理通道操作
            safeHandleChannelOperation(channel, deliveryTag, e);
        }
    }

    private EqpLstParsed validateAndParseMessage(String msg, Channel channel, long deliveryTag) throws JsonProcessingException {
        try {
            EqpLstParsed singleMessage = null;
            // 增加JSON解析异常处理，适应Fastjson2
            try {
                // 如果不是数组，则解析为单个对象
                singleMessage = objectMapper.readValue(msg, EqpLstParsed.class);
            } catch (JsonMappingException e) {
                // 尝试直接解析为列表
                objectMapper.readValue(msg, new TypeReference<List<EqpLstParsed>>() {
                });
                logger.error(">>>>> 解析结果为列表, msg: {}", msg);
            } catch (IOException e) {
                // 处理其他异常
                logger.error(">>>>> JSON解析失败, msg: {}", msg, e);
            }
            if (singleMessage == null) {
                logger.error(">>>>> 解析结果为空, msg: {}", msg);
            }
            return singleMessage;
        } catch (JsonProcessingException e) {
            logger.error(">>>>> JSON解析失败, msg: {}", msg, e);
            throw e;
        }
    }

    /**
     * 安全处理通道操作，避免通道关闭时出现异常
     */
    private void safeHandleChannelOperation(Channel channel, long deliveryTag, Exception e) {
        if (!isChannelOpen(channel, deliveryTag)) {
            return;
        }

        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException ioException) {
            logger.error("Failed to nack message with deliveryTag {}: {}", deliveryTag, ioException.getMessage());
        }
    }

    /**
     * 检查通道是否开启
     */
    private boolean isChannelOpen(Channel channel, long deliveryTag) {
        if (!channel.isOpen()) {
            logger.warn("Channel is closed, cannot perform operation on deliveryTag: {}", deliveryTag);
            return false;
        }
        return true;
    }
}
