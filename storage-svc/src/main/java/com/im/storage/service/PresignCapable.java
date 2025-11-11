package com.im.storage.service;

import java.time.Duration;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/11
 */

public interface PresignCapable {
    /**
     * 生成预签名上传URL
     */
    String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration);

    /**
     * 生成预签名下载URL
     */
    String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration);
}

