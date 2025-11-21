package com.im.storage.service.impl;

import com.im.storage.exception.StorageException;
import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;
import com.im.storage.service.OssService;
import com.im.storage.service.PresignCapable;
import com.im.storage.service.StorageService;
import com.im.storage.service.storage.StorageType;
import com.im.storage.service.storage.factory.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * OSS服务实现类
 * 提供统一的OSS操作实现，支持多版本和多存储类型的路由
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Value("${storage.default:ceph-s3}")
    private String defaultStorageType;

    // 获取默认存储服务
    private StorageService getDefaultStorageService() {
        return storageServiceFactory.getStorageService(defaultStorageType);
    }

    // 获取指定存储服务
    private StorageService getStorageService(StorageType storageType) {
        return storageServiceFactory.getStorageService(storageType);
    }

    /**
     * 初始化OSS服务
     */
    @Override
    public void init() {

    }

    /**
     * 对象是否存在 - 指定存储类型
     *
     * @param bucketName
     * @param objectKey
     */
    @Override
    public boolean objectExists(String bucketName, String objectKey) {
        try {
            return getDefaultStorageService().objectExists(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to check object existence: " + objectKey, e);
        }
    }

    /**
     * 对象是否存在 - 指定存储类型
     *
     * @param bucketName
     * @param objectKey
     * @param storageType
     */
    @Override
    public boolean objectExists(String bucketName, String objectKey, StorageType storageType) {
        try {
            return getStorageService(storageType).objectExists(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to check object existence: " + objectKey, e);
        }
    }

    @Override
    public ObjectMetadata putObject(String bucketName, String objectKey, InputStream content, String contentType) {
        try {
            return getDefaultStorageService().putObject(bucketName, objectKey, content, contentType);
        } catch (Exception e) {
            throw new StorageException("Failed to put object: " + objectKey, e);
        }
    }

    @Override
    public ObjectMetadata putObject(String bucketName, String objectKey, InputStream content,
                                    String contentType, StorageType storageType) {
        try {
            return getStorageService(storageType).putObject(bucketName, objectKey, content, contentType);
        } catch (Exception e) {
            throw new StorageException("Failed to put object: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectKey) {
        try {
            return getDefaultStorageService().getObject(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to get object: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectKey, StorageType storageType) {
        try {
            return getStorageService(storageType).getObject(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to get object: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            getDefaultStorageService().deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey, StorageType storageType) {
        try {
            getStorageService(storageType).deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + objectKey, e);
        }
    }

    @Override
    public List<ObjectInfo> listObjects(String bucketName, String prefix) {
        try {
            return getDefaultStorageService().listObjects(bucketName, prefix);
        } catch (Exception e) {
            throw new StorageException("Failed to list objects in bucket: " + bucketName, e);
        }
    }

    @Override
    public List<ObjectInfo> listObjects(String bucketName, String prefix, StorageType storageType) {
        try {
            return getStorageService(storageType).listObjects(bucketName, prefix);
        } catch (Exception e) {
            throw new StorageException("Failed to list objects in bucket: " + bucketName, e);
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String objectKey) {
        try {
            return getDefaultStorageService().getObjectMetadata(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to get object metadata: " + objectKey, e);
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String objectKey, StorageType storageType) {
        try {
            return getStorageService(storageType).getObjectMetadata(bucketName, objectKey);
        } catch (Exception e) {
            throw new StorageException("Failed to get object metadata: " + objectKey, e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return getDefaultStorageService().bucketExists(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to check bucket existence: " + bucketName, e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName, StorageType storageType) {
        try {
            return getStorageService(storageType).bucketExists(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to check bucket existence: " + bucketName, e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            getDefaultStorageService().createBucket(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to create bucket: " + bucketName, e);
        }
    }

    @Override
    public void createBucket(String bucketName, StorageType storageType) {
        try {
            getStorageService(storageType).createBucket(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to create bucket: " + bucketName, e);
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            getDefaultStorageService().deleteBucket(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to delete bucket: " + bucketName, e);
        }
    }

    @Override
    public void deleteBucket(String bucketName, StorageType storageType) {
        try {
            getStorageService(storageType).deleteBucket(bucketName);
        } catch (Exception e) {
            throw new StorageException("Failed to delete bucket: " + bucketName, e);
        }
    }

    /**
     * 生成预签名上传URL - 默认存储类型
     *
     * @param bucketName
     * @param objectKey
     * @param expiration
     */
    @Override
    public String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration) {
        try {
            StorageService storageService = getDefaultStorageService();
            if (storageService instanceof PresignCapable) {
                return ((PresignCapable) storageService).generatePresignedUploadUrl(bucketName, objectKey, expiration);
            } else {
                throw new StorageException("Default storage type does not support presigned URLs");
            }
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned upload URL for: " + objectKey, e);
        }
    }

    /**
     * 生成预签名上传URL - 指定存储类型
     *
     * @param bucketName
     * @param objectKey
     * @param expiration
     * @param storageType
     */
    @Override
    public String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration, StorageType storageType) {
        try {
            if (!storageType.isPresignSupported()) {
                throw new StorageException("Storage type " + storageType.getType() + " does not support presigned URLs");
            }

            StorageService storageService = getStorageService(storageType);
            if (storageService instanceof PresignCapable) {
                return ((PresignCapable) storageService).generatePresignedUploadUrl(bucketName, objectKey, expiration);
            } else {
                throw new StorageException("Storage type " + storageType.getType() + " does not support presigned URLs");
            }
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned upload URL for: " + objectKey, e);
        }
    }

    /**
     * 生成预签名下载URL - 默认存储类型
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键
     * @param expiration 过期时间
     */
    @Override
    public String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration) {
        try {
            StorageService storageService = getDefaultStorageService();
            if (storageService instanceof PresignCapable) {
                return ((PresignCapable) storageService).generatePresignedDownloadUrl(bucketName, objectKey, expiration);
            } else {
                throw new StorageException("Default storage type does not support presigned URLs");
            }
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned download URL for: " + objectKey, e);
        }
    }

    /**
     * 生成预签名下载URL - 指定存储类型
     *
     * @param bucketName  存储桶名称
     * @param objectKey   对象键
     * @param expiration  过期时间
     * @param storageType 存储类型
     */
    @Override
    public String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration, StorageType storageType) {
        try {
            if (!storageType.isPresignSupported()) {
                throw new StorageException("Storage type " + storageType.getType() + " does not support presigned URLs");
            }

            StorageService storageService = getStorageService(storageType);
            if (storageService instanceof PresignCapable) {
                return ((PresignCapable) storageService).generatePresignedDownloadUrl(bucketName, objectKey, expiration);
            } else {
                throw new StorageException("Storage type " + storageType.getType() + " does not support presigned URLs");
            }
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned download URL for: " + objectKey, e);
        }
    }
}