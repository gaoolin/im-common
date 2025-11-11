package com.im.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
@ConfigurationProperties(prefix = "im.storage")
public class StorageProperties {
    private StorageType type = StorageType.CEHPS3;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String version = "v1";
}