package com.im.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */


/**
 * S3配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3ConfigProperties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String endpoint;
    private String bucket;
}