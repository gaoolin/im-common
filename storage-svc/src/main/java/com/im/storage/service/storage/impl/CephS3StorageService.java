package com.im.storage.service.storage.impl;

import com.im.storage.config.S3ConfigProperties;
import com.im.storage.exception.StorageException;
import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;
import com.im.storage.service.PresignCapable;
import com.im.storage.service.StorageService;
import org.im.common.dt.Chronos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Ceph S3存储服务实现
 * 基于AWS SDK实现Ceph对象存储操作
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Service("ceph-s3StorageService")
public class CephS3StorageService implements StorageService, PresignCapable {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3ConfigProperties s3ConfigProperties;

    /**
     * 判断对象是否存在
     *
     * @param bucketName
     * @param objectKey
     * @return boolean
     */
    @Override
    public boolean objectExists(String bucketName, String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new StorageException("Failed to check object existence: " + objectKey, e);
        }
    }

    @Override
    public ObjectMetadata putObject(String bucketName, String objectKey, InputStream content, String contentType) {
        try {
            // 将InputStream转换为字节数组
            byte[] contentBytes = readAllBytes(content);

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey);

            if (contentType != null && !contentType.isEmpty()) {
                requestBuilder.contentType(contentType);
            }

            PutObjectResponse response = s3Client.putObject(
                    requestBuilder.build(),
                    RequestBody.fromBytes(contentBytes)
            );

            // 构建返回的元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setBucketName(bucketName);
            metadata.setObjectKey(objectKey);
            metadata.setSize((long) contentBytes.length);
            metadata.setContentType(contentType);
            metadata.setEtag(response.eTag());
            metadata.setLastModified(Chronos.now());

            return metadata;
        } catch (Exception e) {
            throw new StorageException("Failed to put object: " + objectKey, e);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            software.amazon.awssdk.core.ResponseInputStream<GetObjectResponse> response =
                    s3Client.getObject(request);

            // 将响应流转换为ByteArrayInputStream以便重复使用
            byte[] content = readAllBytes(response);
            // 确保响应流能被正确处理和关闭
            return new ByteArrayInputStream(content) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        response.close();
                    }
                }
            };
        } catch (Exception e) {
            throw new StorageException("Failed to get object: " + objectKey, e);
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + objectKey, e);
        }
    }

    @Override
    public List<ObjectInfo> listObjects(String bucketName, String prefix) {
        try {
            ListObjectsRequest.Builder requestBuilder = ListObjectsRequest.builder()
                    .bucket(bucketName);

            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsResponse response = s3Client.listObjects(requestBuilder.build());
            List<ObjectInfo> objectInfos = new ArrayList<>();

            if (response.contents() != null) {
                for (S3Object s3Object : response.contents()) {
                    ObjectInfo objectInfo = new ObjectInfo();
                    objectInfo.setKey(s3Object.key());
                    objectInfo.setSize(s3Object.size());
                    // 修复时间转换问题
                    objectInfo.setLastModified(LocalDateTime.ofInstant(s3Object.lastModified(), ZoneId.systemDefault()));
                    objectInfo.setEtag(s3Object.eTag());
                    objectInfos.add(objectInfo);
                }
            }

            return objectInfos;
        } catch (Exception e) {
            throw new StorageException("Failed to list objects in bucket: " + bucketName, e);
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setBucketName(bucketName);
            metadata.setObjectKey(objectKey);
            metadata.setSize(response.contentLength());
            metadata.setContentType(response.contentType());
            metadata.setEtag(response.eTag());
            // 修复时间转换问题
            metadata.setLastModified(LocalDateTime.ofInstant(response.lastModified(), ZoneId.systemDefault()));

            return metadata;
        } catch (Exception e) {
            throw new StorageException("Failed to get object metadata: " + objectKey, e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(request);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (Exception e) {
            throw new StorageException("Failed to check bucket existence: " + bucketName, e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(request);
        } catch (Exception e) {
            throw new StorageException("Failed to create bucket: " + bucketName, e);
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest request = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.deleteBucket(request);
        } catch (Exception e) {
            throw new StorageException("Failed to delete bucket: " + bucketName, e);
        }
    }


    /**
     * Java 8兼容的readAllBytes方法
     * 将InputStream转换为字节数组
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[8192];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * 生成预签名上传URL
     *
     * @param bucketName
     * @param objectKey
     * @param expiration
     */
    @Override
    public String generatePresignedUploadUrl(String bucketName, String objectKey, Duration expiration) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // 使用 S3Presigner 替代 Presigner
            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(s3ConfigProperties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    s3ConfigProperties.getAccessKeyId(),
                                    s3ConfigProperties.getSecretAccessKey())))
                    .endpointOverride(URI.create(s3ConfigProperties.getEndpoint()))
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .putObjectRequest(request)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned upload URL: " + e.getMessage(), e);
        }
    }

    /**
     * 生成预签名下载URL
     *
     * @param bucketName
     * @param objectKey
     * @param expiration
     */
    @Override
    public String generatePresignedDownloadUrl(String bucketName, String objectKey, Duration expiration) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // 使用 S3Presigner 替代 Presigner
            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(s3ConfigProperties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    s3ConfigProperties.getAccessKeyId(),
                                    s3ConfigProperties.getSecretAccessKey())))
                    .endpointOverride(URI.create(s3ConfigProperties.getEndpoint()))
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(request)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned download URL: " + e.getMessage(), e);
        }
    }
}