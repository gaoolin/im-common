package com.im.inspection.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.inspection.entity.OcrLabelInfo;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import org.im.common.json.JsonMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/14 10:04:35
 */
@Configuration
public class RedisConfig {

    /**
     * 共享的 ObjectMapper Bean，保证全局一致性并节省资源
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        return JsonMapperProvider.getSharedInstance();
    }

    /**
     * 用于 EqpReversePOJO 类型的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, EqpReversePOJO> eqReverseInfoRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        return createJsonRedisTemplate(connectionFactory, redisObjectMapper, EqpReversePOJO.class);
    }

    /**
     * 用于 OcrLabelInfo 类型的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, OcrLabelInfo> ocrLabelInfoRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        return createJsonRedisTemplate(connectionFactory, redisObjectMapper, OcrLabelInfo.class);
    }

    /**
     * 用于 Boolean 类型的 RedisTemplate（如 simId 忽略状态）
     */
    @Bean
    public RedisTemplate<String, Boolean> booleanValueRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Boolean> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Boolean.class));
        return template;
    }

    /**
     * 创建基于 JSON 序列化的 RedisTemplate 工具方法
     */
    private <T> RedisTemplate<String, T> createJsonRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            Class<T> valueType) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(valueType);
        serializer.setObjectMapper(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}
