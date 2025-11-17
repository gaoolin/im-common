package com.im.qtech.service.msg.persist.rabbit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.service.msg.entity.EqpLstParsedForDoris;
import com.im.qtech.service.msg.entity.EqpLstParsedForOracle;
import com.im.qtech.service.msg.entity.EqpLstParsedForPG;
import com.im.qtech.service.msg.service.IEqpLstParsedForDoris;
import com.im.qtech.service.msg.service.IEqpLstParsedService;
import com.rabbitmq.client.Channel;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AA List 参数解析队列消费者
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/16 14:01:32
 */

@Component
public class EqpLstParsedQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EqpLstParsedQueueConsumer.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    private final IEqpLstParsedService service;
    private final IEqpLstParsedForDoris serviceForDoris;

    public EqpLstParsedQueueConsumer(IEqpLstParsedService service, IEqpLstParsedForDoris serviceForDoris) {
        this.service = service;
        this.serviceForDoris = serviceForDoris;
    }

    @RabbitListener(queues = "eqLstParsedQueue", ackMode = "MANUAL")
    public void receive(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
        logger.info(">>>>> receive eqLstParsedQueue message: {}", msg);
        try {
            EqpLstParsedForPG singleMessage = validateAndParseMessage(msg, channel, deliveryTag);
            service.save(singleMessage);

            // 转换为通用实体类
            EqpLstParsedForDoris dorisEntity = convertToDoris(singleMessage);

            // 并行执行 Doris 写入
            CompletableFuture<Boolean> dorisFuture = serviceForDoris.saveAsync(dorisEntity).exceptionally(ex -> {
                logger.error(">>>>> Doris upsert 异常: ", ex);
                return false;
            });

            // 等待所有异步任务完成
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(dorisFuture);

            try {
                allFuture.join(); // 等待异步任务完成

                // 只在这里进行一次确认
                if (channel.isOpen()) {
                    channel.basicAck(deliveryTag, false);
                } else {
                    logger.warn("Channel is closed, cannot ack message with deliveryTag: {}", deliveryTag);
                }
            } catch (Exception ex) {
                logger.error(">>>>> 异步任务执行失败: ", ex);
                if (channel.isOpen()) {
                    channel.basicNack(deliveryTag, false, true);
                }
            }
        } catch (Exception e) {
            logger.error(">>>>> receive eqLstParsedQueue message error: {}", e.getMessage(), e);
            if (channel.isOpen()) {
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    private EqpLstParsedForOracle convertToOracle(EqpLstParsedForPG source) {
        return new EqpLstParsedForOracle(
                source.getSimId(),
                source.getReceivedTime()
        );
    }

    private EqpLstParsedForDoris convertToDoris(EqpLstParsedForPG source) {
        EqpLstParsedForDoris target = new EqpLstParsedForDoris();
        // 使用 Spring BeanUtils 拷贝相同属性
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    private EqpLstParsedForPG validateAndParseMessage(String msg, Channel channel, long deliveryTag) throws JsonProcessingException {
        try {
            EqpLstParsedForPG singleMessage = null;
            // 增加JSON解析异常处理，适应Fastjson2
            try {
                // 如果不是数组，则解析为单个对象
                singleMessage = objectMapper.readValue(msg, EqpLstParsedForPG.class);
            } catch (JsonMappingException e) {
                // 尝试直接解析为列表
                objectMapper.readValue(msg, new TypeReference<List<EqpLstParsedForPG>>() {
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

    private void handleException(Channel channel, long deliveryTag, Exception e) throws IOException {
        // 根据异常类型和业务需求决定是否重新入队或采取其他措施
        channel.basicReject(deliveryTag, true); // 示例中选择重新入队
    }
}
