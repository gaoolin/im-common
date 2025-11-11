package com.im.storage.v1;

import com.im.storage.v1.model.FileInfo;
import com.im.storage.v1.model.UploadRequest;
import com.im.storage.v1.model.UploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

public interface StorageServiceV1 {
    UploadResponse upload(UploadRequest request);

    /**
     * 小文件下载
     */
    byte[] downloadBytes(String bucket, String key);

    /**
     * 返回流，由调用方自己关闭
     */
    InputStream downloadStream(String bucket, String key);

    /**
     * 写入到输出流（例如 HttpServletResponse）
     */
    void downloadTo(String bucket, String key, OutputStream os);

    /**
     * 下载到本地文件
     */
    void downloadToFile(String bucket, String key, Path path);

    String getPresignedUrl(String bucket, String key, long expireSeconds);

    boolean delete(String bucket, String key);

    FileInfo getFileInfo(String bucket, String key);

    /**
     * 初始化分片上传，会在后端创建 uploadId 并返回会话信息
     */
    InitMultipartResponse initMultipart(InitMultipartRequest req);

    /**
     * 上传单个分片，返回该分片的 ETag。
     * 实现注意：该方法应支持幂等（相同 partNumber 重复调用返回相同 eTag）
     */
    UploadPartResponse uploadPart(String uploadId, int partNumber, InputStream inputStream, long partSize);

    /**
     * 列举已上传的 part（按 partNumber 升序）
     */
    List<PartInfo> listParts(String uploadId);

    /**
     * 完成分片上传（服务端从持久化记录读取 parts 列表并调用 S3 Complete）
     */
    void completeMultipart(String uploadId);

    /**
     * 中止分片上传（abort）
     */
    void abortMultipart(String uploadId);

    /**
     * 给定 candidate partNumbers 返回缺失的 partNumbers（用于断点续传决定）
     */
    List<Integer> getMissingParts(String uploadId, List<Integer> candidatePartNumbers);
}

