package com.im.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * S3配置属性类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
@ConstructorBinding
public class S3ConfigProperties {
    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String endpoint;
    private String bucket;

    @PostConstruct
    public void validate() {
        System.out.println("Access Key: " + accessKeyId);
        System.out.println("Secret Key: " + secretAccessKey);
    }

}