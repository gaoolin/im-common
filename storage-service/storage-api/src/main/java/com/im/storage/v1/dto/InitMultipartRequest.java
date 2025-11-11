package com.im.storage.v1.dto;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
public class InitMultipartRequest {
    private String tenantId;        // 可选
    private String bucket;
    private String objectKey;
    private String contentType;
    private Long totalSize;         // 可选
    private Long expiresSeconds;    // 可选，会话过期时长
}