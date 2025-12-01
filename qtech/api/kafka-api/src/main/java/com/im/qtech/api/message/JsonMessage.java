package com.im.qtech.api.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.im.common.json.JsonMapperProvider;

import java.util.Objects;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/26
 */

public record JsonMessage(String id, String content) implements Message {
    private static final ObjectMapper mapper = JsonMapperProvider.getSharedInstance();

    public JsonMessage {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
        // 可以添加JSON格式验证
        validateJsonFormat(content);
    }

    private static void validateJsonFormat(String content) {
        try {
            // 使用Jackson验证JSON格式
            mapper.readTree(content);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    // 从设备消息创建JsonMessage
    public static JsonMessage fromEqpDeviceMessage(EqpLstMessage deviceMessage) throws JsonProcessingException {
        String content = deviceMessage.toJson();
        String id = deviceMessage.generateKey();
        return new JsonMessage(id, content);
    }

    // 添加便捷方法
    // 通用对象转换方法
    public <T> T toObject(Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(content, clazz);
    }

    // 特定于设备消息的转换方法
    public EqpLstMessage toEqpDeviceMessage() throws JsonProcessingException {
        return mapper.readValue(content, EqpLstMessage.class);
    }
}
