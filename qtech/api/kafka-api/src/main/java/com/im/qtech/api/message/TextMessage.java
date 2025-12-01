package com.im.qtech.api.message;

import java.util.Objects;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

public record TextMessage(String id, String content) implements Message {
    public TextMessage {
        // 添加验证逻辑
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
    }

    // 可以添加自定义方法
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    @Override
    public String toString() {
        return "TextMessage[id=%s, contentLength=%d, createdAt=%s]"
                .formatted(id(), content().length(), createdAt());
    }
}
