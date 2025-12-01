package com.im.qtech.api.message;

import java.time.Instant;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

// 限制消息类型的继承
public sealed interface Message permits TextMessage, BinaryMessage, JsonMessage {
    String id();

    // 添加默认方法，提供更好的扩展性
    default String contentType() {
        return this.getClass().getSimpleName().toLowerCase().replace("message", "");
    }

    default Instant createdAt() {
        return Instant.now();
    }

    default String content() {
        return null;
    }
}
