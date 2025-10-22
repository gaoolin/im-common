package com.im.qtech.data.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/11/26 09:40:46
 */

public class EqpNetworkStatusSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value);
    }
}