package com.im.qtech.api.dto;

import java.time.Instant;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

public record MessageDto(String id, String content, Instant timestamp) {
}


