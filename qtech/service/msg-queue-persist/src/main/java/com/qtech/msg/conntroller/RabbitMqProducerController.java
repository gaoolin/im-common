package com.qtech.msg.conntroller;

import com.qtech.msg.rabbit.producer.RabbitMqProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产者控制器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/04/10 15:44:49
 */

@RestController
@RequestMapping(value = "/rabbitmq")
public class RabbitMqProducerController {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqProducerController.class);

    @Autowired
    private RabbitMqProducer rabbitMqProducer;

}