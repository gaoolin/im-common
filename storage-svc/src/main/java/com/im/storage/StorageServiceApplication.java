package com.im.storage;

import com.im.storage.config.S3ConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 存储服务启动类
 * 支持多种存储后端的统一API服务
 */
@SpringBootApplication
@EnableConfigurationProperties(S3ConfigProperties.class)
public class StorageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}