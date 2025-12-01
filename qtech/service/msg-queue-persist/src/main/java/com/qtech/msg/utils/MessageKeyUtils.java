package com.qtech.msg.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
public class MessageKeyUtils {

    /**
     * 安全地将对象转换为字符串，避免 null
     */
    public static String safeToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * 计算字符串的 SHA-256 哈希值（返回十六进制表示）
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return UUID.randomUUID().toString(); // fallback，防止系统异常中断
        }
    }

    /**
     * 构建基于多个字段的 Redis 去重 Key（字段以 | 拼接后再计算 SHA-256）
     *
     * @param parts 任意数量的字段对象
     * @return 哈希后的去重 Key
     */
    public static String buildRedisKey(Object... parts) {
        StringJoiner joiner = new StringJoiner("|");
        for (Object part : parts) {
            joiner.add(safeToString(part));
        }
        return sha256(joiner.toString());
    }
}