package com.im.storage.service;

import com.im.storage.model.dto.ObjectInfo;
import com.im.storage.model.dto.ObjectMetadata;

import java.io.InputStream;
import java.util.List;

/**
 * 统一存储服务接口
 * 支持多存储后端的抽象接口定义
 * <p>
 * 提供技术导向的方法命名
 * 专注单一存储后端的技术实现
 * 抛出底层技术相关的异常
 * 保持接口简洁和专注
 * </p>
 * <p>
 * 设计要点说明
 * <p>
 * 职责分离：
 * OssService 作为服务编排层，提供带 StorageType 参数的方法支持多存储路由
 * StorageService 作为具体执行层，专注于单一存储后端的技术实现
 * 接口稳定性：
 * 两个接口都保持方法签名稳定，遵循开闭原则
 * 通过工厂模式实现扩展性而非修改接口
 * 异常处理：
 * 接口不处理具体异常，由实现类负责转换和抛出适当异常
 * 保证接口的纯净性和通用性
 * 资源管理：
 * InputStream 返回值要求调用方负责资源释放
 * 在实现类中提供适当的资源管理机制
 * 这样的设计既满足了当前需求，又为未来扩展提供了良好的架构基础。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

public interface StorageService {
    /**
     * 判断对象是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键
     * @return boolean
     */
    boolean objectExists(String bucketName, String objectKey);

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
