package com.im.storage.v1.model;

import lombok.Data;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
public class FileInfo {
    private String bucket;
    private String key;
    private long size;
    private String eTag;
    private String contentType;
    private long lastModified;
}
