package com.qtech.im.util.http.client.response;

import com.qtech.im.util.http.client.request.RequestConfig;

import java.util.List;

/**
 * 响应验证器
 * <p>
 * 特性：
 * - 通用性：支持各种响应验证场景
 * - 规范性：遵循响应验证标准
 * - 专业性：提供专业的验证能力
 * - 灵活性：支持自定义验证规则
 * - 可靠性：确保验证的准确性
 * - 安全性：提供安全的验证机制
 * - 复用性：可被多种场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class ResponseValidator {
    private final RequestConfig config;

    public ResponseValidator(RequestConfig config) {
        this.config = config != null ? config : RequestConfig.defaultConfig();
    }

    /**
     * 验证响应
     *
     * @param response HTTP响应
     * @return 验证结果
     */
    public ValidationResult validate(ResponseKit response) {
        ValidationResult result = new ValidationResult();

        // 验证状态码
        validateStatusCode(response, result);

        // 验证响应大小
        validateResponseSize(response, result);

        // 验证内容类型
        validateContentType(response, result);

        // 验证安全相关
        validateSecurity(response, result);

        return result;
    }

    /**
     * 验证状态码
     */
    private void validateStatusCode(ResponseKit response, ValidationResult result) {
        int statusCode = response.getStatusCode();

        // 检查是否在成功范围内
        if (statusCode >= config.getSuccessStatusStart() &&
                statusCode <= config.getSuccessStatusEnd()) {
            result.addSuccess("Status code is within success range: " + statusCode);
        } else if (statusCode >= 400 && statusCode < 600) {
            result.addError("Error status code: " + statusCode + " - " + response.getStatusMessage());
        }
    }

    /**
     * 验证响应大小
     */
    private void validateResponseSize(ResponseKit response, ValidationResult result) {
        int bodyLength = response.getBodyLength();
        int maxResponseSize = config.getMaxResponseSize();

        if (bodyLength > maxResponseSize) {
            result.addWarning("Response size " + bodyLength +
                    " exceeds limit " + maxResponseSize);
        } else {
            result.addSuccess("Response size " + bodyLength + " is within limit");
        }
    }

    /**
     * 验证内容类型
     */
    private void validateContentType(ResponseKit response, ValidationResult result) {
        String contentType = response.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            result.addWarning("No Content-Type header found");
        } else {
            result.addSuccess("Content-Type: " + contentType);
        }
    }

    /**
     * 验证安全相关
     */
    private void validateSecurity(ResponseKit response, ValidationResult result) {
        // 检查是否包含敏感头信息
        for (String headerName : response.getHeaders().keySet()) {
            if (isSensitiveHeader(headerName)) {
                result.addWarning("Response contains sensitive header: " + headerName);
            }
        }

        // 检查是否包含潜在的XSS内容
        String body = response.getBodyAsString();
        if (body != null && containsPotentialXss(body)) {
            result.addWarning("Response body may contain XSS content");
        }
    }

    /**
     * 判断是否为敏感头信息
     */
    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null) return false;

        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization") ||
                lowerHeader.contains("cookie") ||
                lowerHeader.contains("set-cookie") ||
                lowerHeader.contains("x-api-key");
    }

    /**
     * 检查是否包含潜在的XSS内容
     */
    private boolean containsPotentialXss(String content) {
        if (content == null || content.isEmpty()) return false;

        String lowerContent = content.toLowerCase();
        return lowerContent.contains("<script") ||
                lowerContent.contains("javascript:") ||
                lowerContent.contains("onload=") ||
                lowerContent.contains("onerror=");
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> successes = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        private final List<String> errors = new java.util.ArrayList<>();

        public void addSuccess(String message) {
            successes.add(message);
        }

        public void addWarning(String message) {
            warnings.add(message);
        }

        public void addError(String message) {
            errors.add(message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getSuccesses() {
            return new java.util.ArrayList<>(successes);
        }

        public List<String> getWarnings() {
            return new java.util.ArrayList<>(warnings);
        }

        public List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("successes=").append(successes.size());
            sb.append(", warnings=").append(warnings.size());
            sb.append(", errors=").append(errors.size());
            sb.append('}');
            return sb.toString();
        }
    }
}
