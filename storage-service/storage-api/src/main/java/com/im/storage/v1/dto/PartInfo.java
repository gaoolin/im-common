package com.im.storage.v1.dto;

import lombok.Data;
/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Data
public class PartInfo {
    private int partNumber;
    private String eTag;
    private long size;
    private long uploadedAt; // epoch millis
}
