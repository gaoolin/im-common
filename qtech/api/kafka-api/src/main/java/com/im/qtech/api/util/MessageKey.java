package com.im.qtech.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/27
 */

public class MessageKey {
    private static final Logger logger = LoggerFactory.getLogger(MessageKey.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    public static String generateLstMsgKey(String message) {
        try {
            // 使用正则表达式直接提取，避免完整JSON解析
            String opCode = extractFieldWithRegex(message, "OpCode");
            String woCode = extractFieldWithRegex(message, "WoCode");

            if (opCode != null && woCode != null) {
                return opCode + "_" + woCode;
            }
        } catch (Exception e) {
            logger.debug("Could not extract key with regex");
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String extractFieldWithRegex(String message, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }
}
