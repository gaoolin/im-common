package com.im.storage.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对象信息DTO
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */

@Data
public class ObjectInfo {
    private String key;
    private Long size;
    private LocalDateTime lastModified;
    private String etag;
}
