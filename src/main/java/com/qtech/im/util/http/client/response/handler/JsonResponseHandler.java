package com.qtech.im.util.http.client.response.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.qtech.im.util.http.client.response.ResponseHandlingException;
import com.qtech.im.util.http.client.response.ResponseKit;

/**
 * JSON响应处理器
 * <p>
 * 特性：
 * - 通用性：支持标准JSON响应处理
 * - 规范性：遵循JSON处理标准
 * - 专业性：提供专业的JSON处理能力
 * - 灵活性：支持自定义ObjectMapper配置
 * - 可靠性：确保JSON处理的稳定性
 * - 安全性：防止JSON注入攻击
 * - 复用性：可被各种JSON场景复用
 * - 容错性：具备良好的JSON错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class JsonResponseHandler implements ResponseHandler<JsonNode> {
    private final ObjectMapper objectMapper;
    private final int maxJsonSize;

    public JsonResponseHandler() {
        this(new ObjectMapper(), 10 * 1024 * 1024); // 默认10MB限制
    }

    public JsonResponseHandler(ObjectMapper objectMapper, int maxJsonSize) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.maxJsonSize = maxJsonSize > 0 ? maxJsonSize : 10 * 1024 * 1024;
    }

    @Override
    public JsonNode handle(ResponseKit response) throws ResponseHandlingException {
        // 验证响应
        if (!canHandle(response)) {
            throw new ResponseHandlingException(
                    "Response is not JSON: " + response.getContentType(),
                    response,
                    ResponseHandlingException.ErrorType.UNSUPPORTED_CONTENT_TYPE
            );
        }

        // 检查响应大小
        if (response.getBodyLength() > maxJsonSize) {
            throw new ResponseHandlingException(
                    "JSON response too large: " + response.getBodyLength() + " bytes",
                    response,
                    ResponseHandlingException.ErrorType.RESPONSE_TOO_LARGE
            );
        }

        try {
            String json = response.getBodyAsString();
            if (json == null || json.trim().isEmpty()) {
                return NullNode.getInstance();
            }

            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new ResponseHandlingException(
                    "Failed to parse JSON response: " + e.getMessage(),
                    e,
                    response,
                    ResponseHandlingException.ErrorType.PARSING_ERROR
            );
        }
    }

    @Override
    public boolean canHandle(ResponseKit response) {
        return response.isJson() ||
                response.isContentType("text/json") ||
                (response.getContentType() == null && looksLikeJson(response.getBodyAsString()));
    }

    @Override
    public String getName() {
        return "JsonResponseHandler";
    }

    /**
     * 简单检查字符串是否像JSON
     */
    private boolean looksLikeJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String trimmed = content.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
