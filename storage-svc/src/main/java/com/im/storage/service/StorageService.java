package com.im.storage.service;

import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;

import java.io.InputStream;
import java.util.List;

/**
 * 统一存储服务接口
 * 支持多存储后端的抽象接口定义
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
public interface StorageService {

    /**
     * 上传对象
     *
     * @param bucketName  存储桶名称
     * @param objectKey   对象键
     * @param content     对象内容输入流
     * @param contentType 内容类型
     * @return 对象元数据
     */
    ObjectMetadata putObject(String bucketName, String objectKey, InputStream content, String contentType);

    /**
     * 下载对象
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键
     * @return 对象输入流
     */
    InputStream getObject(String bucketName, String objectKey);

    /**
     * 删除对象
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键
     */
    void deleteObject(String bucketName, String objectKey);

    /**
     * 列出对象
     *
     * @param bucketName 存储桶名称
     * @param prefix     前缀过滤
     * @return 对象列表
     */
    List<ObjectInfo> listObjects(String bucketName, String prefix);

    /**
     * 获取对象元数据
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键
     * @return 对象元数据
     */
    ObjectMetadata getObjectMetadata(String bucketName, String objectKey);

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名称
     */
    void deleteBucket(String bucketName);
}