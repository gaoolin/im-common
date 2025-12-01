package com.qtech.msg.rabbit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.qtech.im.util.JsonMapperProvider;
import com.qtech.msg.service.IEqpReverseInfoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.im.qtech.data.constant.QtechImBizConstant.REDIS_KEY_PREFIX_EQP_REVERSE_INFO;

/**
 * 设备反控信息消费消费者
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/16 08:33:27
 */
@Component
public class EqReverseInfoQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EqReverseInfoQueueConsumer.class);
    // 获取共享objectMapper
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IEqpReverseInfoService service;
    private final RedisTemplate<String, EqpReversePOJO> template;

    public EqReverseInfoQueueConsumer(IEqpReverseInfoService service, RedisTemplate<String, EqpReversePOJO> template) {
        this.service = service;
        this.template = template;
    }

    @RabbitListener(queues = "eqReverseInfoQueue", ackMode = "MANUAL")
    public void receive(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (msg == null || msg.isEmpty()) {
            logger.error(">>>>> 消息为空, eqReverseCtrlInfoQueue队列, msg: {}", msg);
            safeAck(channel, deliveryTag);
            return;
        }

        try {
            List<EqpReversePOJO> messages = validateAndParseMessage(msg);

            if (messages.isEmpty()) {
                logger.error(">>>>> 反控结果为空, msg: {}", msg);
                safeAck(channel, deliveryTag);
                return;
            }

            AtomicBoolean hasError = new AtomicBoolean(false);
            for (EqpReversePOJO message : messages) {
                logger.info(">>>>> 反控结果: simId={}", message.getSimId());
                processMessage(message, hasError);
            }

            if (!hasError.get()) {
                safeAck(channel, deliveryTag);
            } else {
                safeReject(channel, deliveryTag, true); // 重新入队
            }
        } catch (Exception e) {
            logger.error(">>>>> 处理消息失败: {} - Error: {}", msg, e.getMessage(), e);
            handleException(channel, deliveryTag, e);
        }
    }

    private void processMessage(EqpReversePOJO message, AtomicBoolean hasError) {
        String simId = message.getSimId();
        String redisKey = REDIS_KEY_PREFIX_EQP_REVERSE_INFO + simId;

        try {
            // 原子性删除Redis缓存
            Boolean deleted = template.delete(redisKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.debug(">>>>> Redis key 已删除: {}", redisKey);
            } else {
                logger.warn(">>>>> Redis key 不存在或删除失败: {}", redisKey);
            }
        } catch (Exception e) {
            logger.error(">>>>> Redis 删除失败: {}", redisKey, e);
            hasError.set(true);
        }

        CompletableFuture<Integer> future = service.upsertOracleAsync(message).exceptionally(ex -> {
            logger.error(">>>>> Oracle upsert 异常: ", ex);
            hasError.set(true);
            return -1;
        });

        CompletableFuture<Integer> dorisAsync = service.upsertDorisAsync(message).exceptionally(ex -> {
            logger.error(">>>>> Doris upsert 异常: ", ex);
            hasError.set(true);
            return -1;
        });

        CompletableFuture<Integer> addAaListDorisAsync = service.addEqpLstDorisAsync(message).exceptionally(ex -> {
            logger.error(">>>>> Doris insert 异常: ", ex);
            hasError.set(true);
            return -1;
        });

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future, dorisAsync, addAaListDorisAsync);
        try {
            allFuture.join(); // 等待所有异步任务完成
        } catch (CompletionException e) {
            logger.error(">>>>> 异步任务执行失败: ", e.getCause());
            hasError.set(true);
        }
    }

    private List<EqpReversePOJO> validateAndParseMessage(String msg) throws JsonProcessingException {
        try {
            // 尝试解析为单个对象
            EqpReversePOJO singleMessage = objectMapper.readValue(msg, EqpReversePOJO.class);
            return Collections.singletonList(singleMessage);
        } catch (JsonProcessingException e1) {
            try {
                // 尝试解析为列表
                return objectMapper.readValue(msg, new TypeReference<List<EqpReversePOJO>>() {
                });
            } catch (JsonProcessingException e2) {
                logger.error(">>>>> JSON解析失败, msg: {}", msg, e2);
                throw e2;
            }
        }
    }

    private void handleException(Channel channel, long deliveryTag, Exception e) {
        if (!isChannelOpen(channel, deliveryTag)) {
            return;
        }

        try {
            if (e instanceof JsonProcessingException) {
                // JSON解析错误，可以考虑重新入队
                channel.basicNack(deliveryTag, false, true);
            } else {
                // 其他类型的异常，根据具体业务需求处理
                channel.basicReject(deliveryTag, false);
            }
        } catch (IOException ioException) {
            logger.error("Failed to handle exception for deliveryTag {}: {}", deliveryTag, ioException.getMessage());
        }
    }

    /**
     * 安全执行ACK操作
     */
    private void safeAck(Channel channel, long deliveryTag) {
        if (!isChannelOpen(channel, deliveryTag)) {
            return;
        }

        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            logger.error("Failed to ack message with deliveryTag {}: {}", deliveryTag, e.getMessage());
        }
    }

    /**
     * 安全执行Reject操作
     */
    private void safeReject(Channel channel, long deliveryTag, boolean requeue) {
        if (!isChannelOpen(channel, deliveryTag)) {
            return;
        }

        try {
            channel.basicReject(deliveryTag, requeue);
        } catch (IOException e) {
            logger.error("Failed to reject message with deliveryTag {}: {}", deliveryTag, e.getMessage());
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
