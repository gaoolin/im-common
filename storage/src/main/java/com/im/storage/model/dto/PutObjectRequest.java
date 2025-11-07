package com.im.storage.model.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 上传对象请求DTO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Data
public class PutObjectRequest {
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @NotBlank(message = "对象键不能为空")
    private String objectKey;

    @NotNull(message = "对象内容不能为空")
    private byte[] content;

    private String contentType;
    private String storageType;
}