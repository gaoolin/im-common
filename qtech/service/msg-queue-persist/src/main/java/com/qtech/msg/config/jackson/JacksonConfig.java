package com.qtech.msg.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.im.common.json.JsonMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/25 10:52:51
 * desc   :  将 ObjectMapper 配置为 Spring Bean
 */

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = JsonMapperProvider.getSharedInstance();

        // 可根据项目需要自由增减
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略 null 字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知字段
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期格式化
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为上海时区

        // 可添加模块支持，如 Java 8 时间类型、Kotlin 等
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}