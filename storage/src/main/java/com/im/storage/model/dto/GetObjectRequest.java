package com.im.storage.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 下载对象请求DTO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Data
public class GetObjectRequest {
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    @NotBlank(message = "对象键不能为空")
    private String objectKey;

    private String versionId;
    private String storageType;
}
