package com.im.qtech.service.config.redis;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 使用 @ConfigurationProperties 来代替 @Value 注解。@ConfigurationProperties 是一种更强大的配置方式，特别是在处理复杂的嵌套属性时
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 13:30:00
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis.cluster")
public class RedisClusterConfig {

    private List<String> nodes;
    private int maxRedirects;

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }
}
