package com.im.qtech.service.msg.conntroller;

import com.im.qtech.service.msg.persist.rabbit.producer.RabbitMqProducer;
import com.im.qtech.service.msg.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/04/10 15:44:49
 * desc   :  生产者控制器
 */

@RestController
@RequestMapping(value = "/rabbitmq")
public class RabbitMqProducerController {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqProducerController.class);

    @Autowired
    private RabbitMqProducer rabbitMqProducer;

    @RequestMapping(value = "/chk/result", method = RequestMethod.POST)
    public ApiResponse<String> sendMsg(@RequestBody String msg) {
        // 3个参数：交换机，路由键（队列名），消息。
        try {
            rabbitMqProducer.send("eqReverseInfoExchange", "eqReverseInfoQueue", msg);
            return ApiResponse.success();
        } catch (Exception e) {
            logger.error("send msg error: {}", e.getMessage());
        }
        return ApiResponse.badRequest("send msg failed");
    }
}