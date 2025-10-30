package com.im.qtech.service.msg.persist.rabbit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.service.IEqpReverseInfoService;
import com.rabbitmq.client.Channel;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.DisposableBean;
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
public class EqpReverseInfoQueueConsumer implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(EqpReverseInfoQueueConsumer.class);
    // 获取共享objectMapper
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IEqpReverseInfoService service;
    private final RedisTemplate<String, EqpReverseInfo> template;

    public EqpReverseInfoQueueConsumer(IEqpReverseInfoService service, RedisTemplate<String, EqpReverseInfo> template) {
        this.service = service;
        this.template = template;
    }

    @Override
    public void destroy() throws Exception {
        // connection is not initialized, so nothing to close here
    }

    @RabbitListener(queues = "eqReverseInfoQueue", ackMode = "MANUAL")
    public void receive(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        if (msg == null || msg.isEmpty()) {
            logger.error(">>>>> 消息为空, eqReverseInfoQueue队列, msg: {}", msg);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            List<EqpReverseInfo> messages = validateAndParseMessage(msg);

            if (messages.isEmpty()) {
                logger.error(">>>>> 解析结果为空, msg: {}", msg);
                channel.basicAck(deliveryTag, false);
                return;
            }

            AtomicBoolean hasError = new AtomicBoolean(false);
            for (EqpReverseInfo message : messages) {
                logger.info(">>>>> 解析结果: simId={}", message.getSimId());
                processMessage(message, hasError);
            }

            if (!hasError.get()) {
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicReject(deliveryTag, true); // 重新入队
            }
        } catch (Exception e) {
            logger.error(">>>>> 处理消息失败: {} - Error: {}", msg, e.getMessage(), e);
            handleException(channel, deliveryTag, e);
        }
    }

    private void processMessage(EqpReverseInfo message, AtomicBoolean hasError) {
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

        CompletableFuture<Integer> exceptionally = service.upsertPGAsync(message).exceptionally(ex -> {
            logger.error(">>>>> Postgres upsert 异常: ", ex);
            hasError.set(true);
            return -1;
        });

        // CompletableFuture<Void> allFuture = CompletableFuture.allOf(future, dorisAsync, addAaListDorisAsync);
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(exceptionally);
        try {
            allFuture.join(); // 等待所有异步任务完成
        } catch (CompletionException e) {
            logger.error(">>>>> 异步任务执行失败: ", e.getCause());
            hasError.set(true);
        }
    }

    private List<EqpReverseInfo> validateAndParseMessage(String msg) throws JsonProcessingException {
        try {
            // 尝试解析为单个对象
            EqpReverseInfo singleMessage = objectMapper.readValue(msg, EqpReverseInfo.class);
            return Collections.singletonList(singleMessage);
        } catch (JsonProcessingException e1) {
            try {
                // 尝试解析为列表
                return objectMapper.readValue(msg, new TypeReference<List<EqpReverseInfo>>() {
                });
            } catch (JsonProcessingException e2) {
                logger.error(">>>>> JSON解析失败, msg: {}", msg, e2);
                throw e2;
            }
        }
    }

    private void handleException(Channel channel, long deliveryTag, Exception e) throws IOException {
        if (e instanceof JsonProcessingException) {
            // JSON解析错误，可以考虑重新入队
            channel.basicNack(deliveryTag, false, true);
        } else {
            // 其他类型的异常，根据具体业务需求处理
            channel.basicReject(deliveryTag, false);
        }
    }
}
