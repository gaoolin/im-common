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
    CEPH_S3("ceph-s3", "Ceph S3存储", true),
    HDFS("hdfs", "HDFS存储", false),
    MINIO("minio", "MinIO存储", true),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS", true),
    AMAZON_S3("amazon-s3", "Amazon S3", true);

    private final String type;
    private final String description;
    private final boolean presignSupported;

    StorageType(String type, String description, boolean presignSupported) {
        this.type = type;
        this.description = description;
        this.presignSupported = presignSupported;
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

    public boolean isPresignSupported() {
        return presignSupported;
    }
}
