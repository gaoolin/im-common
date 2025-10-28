package com.im.qtech.service.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/04/10 15:34:45
 */

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange qtechImExchange() {
        return new DirectExchange("qtechImExchange", true, false);
    }

    @Bean
    public Queue eqRevserInfoQueue() {
        return new Queue("eqReverseInfoQueue", true);
    }

    @Bean
    public Queue eqLstParsedQueue() {
        return new Queue("eqLstParsedQueue", true);
    }

    @Bean
    public Binding eqReverseInfoBinding() {
        return new Binding("eqReverseInfoQueue", Binding.DestinationType.QUEUE, "qtechImExchange", "eqReverseInfoQueue", null);
    }

    @Bean
    public Binding eqLstParsedBinding() {
        return new Binding("eqLstParsedQueue", Binding.DestinationType.QUEUE, "qtechImExchange", "eqLstParsedQueue", null);
    }
}