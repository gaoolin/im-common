package com.im.storage.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 列表对象请求DTO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

@Data
public class ListObjectsRequest {
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    private String prefix;
    private String delimiter;
    private Integer maxKeys;
    private String storageType;
}