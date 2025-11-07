package com.im.storage.service.storage;

/**
 * 存储类型枚举
 * 支持多存储后端扩展
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

public enum StorageType {
    CEPH_S3("ceph-s3", "Ceph S3存储"),
    HDFS("hdfs", "HDFS存储"),
    MINIO("minio", "MinIO存储"),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS"),
    AMAZON_S3("amazon-s3", "Amazon S3");

    private final String type;
    private final String description;

    StorageType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public static StorageType fromString(String type) {
        for (StorageType storageType : StorageType.values()) {
            if (storageType.getType().equals(type)) {
                return storageType;
            }
        }
        throw new IllegalArgumentException("Unsupported storage type: " + type);
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}