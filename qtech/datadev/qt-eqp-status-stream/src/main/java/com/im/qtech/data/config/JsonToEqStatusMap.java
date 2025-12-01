package com.im.qtech.data.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.qtech.data.model.EqNetworkStatus;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.im.common.json.JsonMapperProvider;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 14:43:19
 */
public class JsonToEqStatusMap extends RichMapFunction<String, EqNetworkStatus> {

    private transient ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) {
        // 在 open 中获取 ObjectMapper 单例，避免序列化问题
        this.objectMapper = JsonMapperProvider.getSharedInstance();
    }

    @Override
    public EqNetworkStatus map(String value) throws Exception {
        // 使用成员变量 objectMapper 反序列化 JSON 字符串
        return objectMapper.readValue(value, EqNetworkStatus.class);
    }
}