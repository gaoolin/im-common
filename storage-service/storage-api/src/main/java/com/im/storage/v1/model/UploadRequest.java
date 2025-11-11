package com.im.storage.v1.model;

import lombok.Data;

import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
public class UploadRequest {
    private String bucket;
    private String key;
    private byte[] bytes;      // 简单上传时用
    private long size;
    private String contentType;
    private Map<String,String> metadata;
}
