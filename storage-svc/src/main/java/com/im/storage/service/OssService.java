package com.im.storage.service;

import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;
import com.im.storage.service.storage.StorageType;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * OSS服务接口
 * 提供统一的OSS操作接口，支持多版本和多存储类型
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
public interface OssService {

    /**
     * 上传对象 - 默认存储类型
     */
    ObjectMetadata putObject(String bucketName, String objectKey, InputStream content, String contentType);

    /**
     * 上传对象 - 指定存储类型
     */
    ObjectMetadata putObject(String bucketName, String objectKey, InputStream content,
                             String contentType, StorageType storageType);

    /**
     * 下载对象 - 默认存储类型
     */
    InputStream getObject(String bucketName, String objectKey);

    /**
     * 下载对象 - 指定存储类型
     */
    InputStream getObject(String bucketName, String objectKey, StorageType storageType);

    /**
     * 删除对象 - 默认存储类型
     */
    void deleteObject(String bucketName, String objectKey);

    /**
     * 删除对象 - 指定存储类型
     */
    void deleteObject(String bucketName, String objectKey, StorageType storageType);

    /**
     * 列出对象 - 默认存储类型
     */
    List<ObjectInfo> listObjects(String bucketName, String prefix);

    /**
     * 列出对象 - 指定存储类型
     */
    List<ObjectInfo> listObjects(String bucketName, String prefix, StorageType storageType);

    /**
     * 获取对象元数据 - 默认存储类型
     */
    ObjectMetadata getObjectMetadata(String bucketName, String objectKey);

    /**
     * 获取对象元数据 - 指定存储类型
     */
    ObjectMetadata getObjectMetadata(String bucketName, String objectKey, StorageType storageType);

    /**
     * 检查存储桶是否存在 - 默认存储类型
     */
    boolean bucketExists(String bucketName);

    /**
     * 检查存储桶是否存在 - 指定存储类型
     */
    boolean bucketExists(String bucketName, StorageType storageType);

    /**
     * 创建存储桶 - 默认存储类型
     */
    void createBucket(String bucketName);

    /**
     * 创建存储桶 - 指定存储类型
     */
    void createBucket(String bucketName, StorageType storageType);

    /**
     * 删除存储桶 - 默认存储类型
     */
    void deleteBucket(String bucketName);

    /**
     * 删除存储桶 - 指定存储类型
     */
    void deleteBucket(String bucketName, StorageType storageType);

    /**
     * 生成预签名上传URL - 默认存储类型
     */
    String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration);

    /**
     * 生成预签名上传URL - 指定存储类型
     */
    String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration, StorageType storageType);

    /**
     * 生成预签名下载URL - 默认存储类型
     */
    String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration);

    /**
     * 生成预签名下载URL - 指定存储类型
     */
    String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration, StorageType storageType);

}