package com.qtech.im.util.http.client.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * HTTP响应工厂类
 * <p>
 * 特性：
 * - 通用性：支持创建各种类型的HTTP响应
 * - 规范性：遵循响应创建标准
 * - 专业性：提供专业的响应创建能力
 * - 灵活性：支持自定义响应属性
 * - 可靠性：确保响应创建的稳定性
 * - 安全性：提供安全的响应创建机制
 * - 复用性：可被多种场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class ResponseFactory {

    /**
     * 创建成功响应
     */
    public static ResponseKit createSuccessResponse(String body) {
        return ResponseKit.builder()
                .statusCode(200)
                .statusMessage("OK")
                .body(body)
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建JSON响应
     */
    public static ResponseKit createJsonResponse(int statusCode, String jsonBody) {
        return ResponseKit.builder()
                .statusCode(statusCode)
                .statusMessage(getStatusMessage(statusCode))
                .body(jsonBody)
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建错误响应
     */
    public static ResponseKit createErrorResponse(int statusCode, String errorMessage) {
        return ResponseKit.builder()
                .statusCode(statusCode)
                .statusMessage(getStatusMessage(statusCode))
                .body("{\"error\":\"" + errorMessage + "\"}")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建重定向响应
     */
    public static ResponseKit createRedirectResponse(String location) {
        return ResponseKit.builder()
                .statusCode(302)
                .statusMessage("Found")
                .addHeader("Location", location)
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * 从现有数据创建响应
     */
    public static ResponseKit createResponse(int statusCode, String statusMessage,
                                             Map<String, List<String>> headers,
                                             byte[] body,
                                             long responseDuration) {
        return ResponseKit.builder()
                .statusCode(statusCode)
                .statusMessage(statusMessage)
                .headers(headers)
                .body(body)
                .responseTime(LocalDateTime.now())
                .responseDuration(responseDuration)
                .build();
    }

    /**
     * 获取状态码对应的消息
     */
    private static String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 301:
                return "Moved Permanently";
            case 302:
                return "Found";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            default:
                return "Unknown Status";
        }
    }
}
