package com.qtech.msg.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.qtech.im.util.JsonMapperProvider;
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
    public RedisTemplate<String, EqpReversePOJO> eqReverseCtrlInfoRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, EqpReversePOJO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<EqpReversePOJO> serializer = new Jackson2JsonRedisSerializer<>(EqpReversePOJO.class);
        ObjectMapper objectMapper = JsonMapperProvider.createCustomizedInstance(m -> m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false));

        serializer.setObjectMapper(objectMapper);

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