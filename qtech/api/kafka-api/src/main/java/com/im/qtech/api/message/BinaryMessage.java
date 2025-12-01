package com.im.qtech.api.message;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

public record BinaryMessage(String id, String content) implements Message {
    @Override
    public String id() {
        return null;
    }

    @Override
    public String content() {
        return Message.super.content();
    }
}