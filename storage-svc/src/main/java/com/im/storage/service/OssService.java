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
 * 提供业务导向的方法命名
 * 支持多存储类型的路由
 * 处理跨存储的事务和一致性
 * 包含业务级别的异常处理
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
 * <p>
 * OssService 和 StorageService 的关系与区别
 * 关系
 * OssService 依赖于 StorageService 接口的具体实现
 * OssService 是对外统一的服务接口层，负责协调和路由不同存储类型的操作
 * StorageService 是底层存储操作的抽象定义，每种存储类型都需要实现这个接口
 * 主要区别
 * 职责范围
 * OssService: 提供统一的OSS操作接口，支持多版本和多存储类型的路由，是门面模式的体现
 * StorageService: 定义具体存储操作的基础接口，专注于单一存储后端的操作实现
 * 方法参数
 * OssService: 包含带 StorageType 参数的方法，支持指定存储类型操作
 * StorageService: 所有方法都不包含存储类型参数，针对单一存储实现
 * 实现方式
 * OssService: 通过 StorageServiceFactory 获取对应存储类型的 StorageService 实现实例
 * StorageService: 由具体的存储实现类（如 CephS3StorageService）直接实现
 * 默认行为
 * OssService: 提供默认存储类型的操作方法（调用默认存储服务）
 * StorageService: 每个实现类本身就是特定存储类型的实现
 * 扩展性支持
 * OssService: 通过工厂模式支持运行时动态选择不同存储类型
 * StorageService: 作为SPI（服务提供接口），每个实现都是静态绑定到特定存储类型
 * 总的来说，OssService 是服务编排层，而 StorageService 是具体执行层。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

public interface OssService {
    /**
     * 初始化OSS服务
     */
    void init();

    /**
     * 对象是否存在 - 默认存储类型
     */
    boolean objectExists(String bucketName, String objectKey);

    /**
     * 对象是否存在 - 指定存储类型
     */
    boolean objectExists(String bucketName, String objectKey, StorageType storageType);

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
