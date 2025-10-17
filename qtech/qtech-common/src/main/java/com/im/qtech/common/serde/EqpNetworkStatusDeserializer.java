package com.im.qtech.common.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/11/26 09:39:47
 */

public class EqpNetworkStatusDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.getText();
    }
}