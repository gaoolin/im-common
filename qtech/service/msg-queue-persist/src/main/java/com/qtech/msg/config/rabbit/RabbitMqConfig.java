package com.qtech.msg.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/04/10 15:34:45
 */
@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("10.170.6.40");
        factory.setPort(31131);
        factory.setUsername("qtech");
        factory.setPassword("Ee786549");
        factory.setVirtualHost("/");

        // 增强连接稳定性配置
        factory.setRequestedHeartBeat(60); // 心跳检测60秒
        factory.setConnectionTimeout(30000); // 连接超时30秒
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED); // 启用发布确认
        factory.setPublisherReturns(true); // 启用发布返回

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3); // 初始消费者数量
        factory.setMaxConcurrentConsumers(10); // 最大消费者数量
        factory.setPrefetchCount(1); // 每次从队列预取的消息数
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL); // 手动确认
        factory.setDefaultRequeueRejected(false); // 拒绝消息时不重新入队
        factory.setFailedDeclarationRetryInterval(5000L); // 声明失败重试间隔
        factory.setRecoveryInterval(5000L); // 连接恢复间隔
        return factory;
    }

    @Bean
    public DirectExchange qtechImExchange() {
        return new DirectExchange("qtechImExchange", true, false);
    }

    @Bean
    public Queue eqReverseInfoQueue() {
        return new Queue("eqReverseInfoQueue", true); // 持久化队列
    }

    @Bean
    public Queue eqLstParsedQueue() {
        return new Queue("eqLstParsedQueue", true); // 持久化队列
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
