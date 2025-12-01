package com.im.qtech.api.service;

import org.springframework.stereotype.Service;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

@Service
public class MessageProcessor {

    public void processMessage(Object message) {
        switch (message) {
            case String str when str.length() > 100 -> handleLargeString(str);
            case String str -> handleNormalString(str);
            case byte[] bytes when bytes.length > 1024 -> handleLargeBytes(bytes);
            case byte[] bytes -> handleNormalBytes(bytes);
            case null, default -> throw new IllegalArgumentException("Unsupported message type");
        }
    }

    private void handleNormalBytes(byte[] bytes) {
        // 处理字节数组消息
    }

    private void handleLargeBytes(byte[] bytes) {
    }

    private void handleNormalString(String str) {
    }

    private void handleLargeString(String str) {
    }
}
