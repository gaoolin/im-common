package com.im.qtech.service.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import org.im.common.json.JsonMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/24 08:47:46
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, EqpReverseInfo> eqReverseCtrlInfoRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, EqpReverseInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 JsonMapperProvider 创建 ObjectMapper 实例
        ObjectMapper objectMapper = JsonMapperProvider.createCustomizedInstance(m ->
                m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false));

        // 在构造函数中直接传递 ObjectMapper，避免使用已弃用方法
        Jackson2JsonRedisSerializer<EqpReverseInfo> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, EqpReverseInfo.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
