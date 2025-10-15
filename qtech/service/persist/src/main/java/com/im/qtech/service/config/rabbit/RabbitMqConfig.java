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
    public Queue eqRevserCtrlInfoQueue() {
        return new Queue("eqReverseCtrlInfoQueue", true);
    }

    @Bean
    public Queue aaListParamsParsedQueue() {
        return new Queue("aaListParamsParsedQueue", true);
    }

    /**
     * @param
     * @return org.springframework.amqp.core.Binding
     * @description 绑定队列到交换机, 并指定路由键。暂时不需要这个队列来处理数据，而是使用kafka
     */
    // @Bean
    // public Queue jobRunStatQueue() {
    //     return new Queue("wbRawDataQueue", true);
    // }
    @Bean
    public Binding wbOlpCheckResultBinding() {
        return new Binding("eqReverseCtrlInfoQueue", Binding.DestinationType.QUEUE, "qtechImExchange", "eqReverseCtrlInfoQueue", null);
    }

    @Bean
    public Binding aaListParamsParsedBinding() {
        return new Binding("aaListParamsParsedQueue", Binding.DestinationType.QUEUE, "qtechImExchange", "aaListParamsParsedQueue", null);
    }

    /**
     * @description 暂时不需要这个队列来处理数据，而是使用kafka
     * @param null
     * @return
     */
    // @Bean
    // public Binding jobRunStatBinding() {
    //     return new Binding("wbRawDataQueue", Binding.DestinationType.QUEUE, "qtechImExchange", "wbRawDataQueue", null);
    // }
}
