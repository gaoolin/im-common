package com.im.storage.v1.dto;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
public class InitMultipartResponse {
    private String uploadId;
    private String bucket;
    private String objectKey;
    private Long expiresAt; // epoch millis, 可选
}