package com.im.storage.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对象元数据DTO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Data
public class ObjectMetadata {
    private String bucketName;
    private String objectKey;
    private Long size;
    private String contentType;
    private String etag;
    private LocalDateTime lastModified;
    private String storageClass;
}
