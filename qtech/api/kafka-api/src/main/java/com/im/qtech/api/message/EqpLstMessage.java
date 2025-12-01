package com.im.qtech.api.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.im.common.json.JsonMapperProvider;

import java.util.Objects;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/27
 */

public record EqpLstMessage(
        @JsonProperty("OpCode") String opCode,
        @JsonProperty("WoCode") String woCode,
        @JsonProperty("FactoryName") String factoryName
) {
    private static final ObjectMapper mapper = JsonMapperProvider.getSharedInstance();

    public EqpLstMessage {
        Objects.requireNonNull(opCode, "OpCode cannot be null");
        Objects.requireNonNull(woCode, "WoCode cannot be null");
        Objects.requireNonNull(factoryName, "FactoryName cannot be null");
    }

    // 从JSON字符串创建实例
    public static EqpLstMessage fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, EqpLstMessage.class);
    }

    // 生成用于Kafka的唯一键
    public String generateKey() {
        return opCode + "_" + woCode;
    }

    // 转换为JSON字符串
    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }
}
