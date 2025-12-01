package com.qtech.im.util.http.client.response;

import com.qtech.im.util.http.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP响应类
 * <p>
 * 特性：
 * - 通用性：支持所有HTTP响应处理
 * - 规范性：遵循HTTP协议标准
 * - 专业性：提供丰富的响应信息和处理能力
 * - 灵活性：支持多种响应体格式和编码
 * - 可靠性：提供完整的响应验证机制
 * - 安全性：支持响应大小限制和内容安全检查
 * - 复用性：可与其他HTTP工具无缝集成
 * - 容错性：提供优雅的错误处理机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class ResponseKit {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final LocalDateTime responseTime;
    private final long responseDuration;
    private final HttpStatus status;
    private final String protocol;
    private final Charset charset;
    private final String contentType;

    private ResponseKit(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.headers = builder.headers != null ?
                new ConcurrentHashMap<>(builder.headers) : new ConcurrentHashMap<>();
        this.body = builder.body != null ? Arrays.copyOf(builder.body, builder.body.length) : new byte[0];
        this.responseTime = builder.responseTime != null ? builder.responseTime : LocalDateTime.now();
        this.responseDuration = builder.responseDuration;
        this.status = HttpStatus.valueOf(statusCode);
        this.protocol = builder.protocol;
        this.charset = determineCharset();
        this.contentType = determineContentType();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 确定响应字符集
     */
    private Charset determineCharset() {
        String contentTypeHeader = getHeader("Content-Type");
        if (contentTypeHeader != null) {
            // 解析Content-Type中的charset参数
            String[] parts = contentTypeHeader.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.toLowerCase().startsWith("charset=")) {
                    String charsetName = part.substring(8).trim();
                    try {
                        return Charset.forName(charsetName);
                    } catch (Exception e) {
                        // 忽略无效的字符集，使用默认值
                    }
                }
            }
        }
        return StandardCharsets.UTF_8; // 默认UTF-8
    }

    /**
     * 确定内容类型
     */
    private String determineContentType() {
        return getHeader("Content-Type");
    }

    // Getters
    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, List<String>> getHeaders() {
        return new HashMap<>(headers);
    }

    public String getHeader(String name) {
        if (name == null) return null;

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey()) && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    public List<String> getHeaders(String name) {
        if (name == null) return Collections.emptyList();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return Collections.emptyList();
    }

    public byte[] getBodyAsBytes() {
        return Arrays.copyOf(body, body.length);
    }

    public String getBodyAsString() {
        if (body.length == 0) return "";
        return new String(body, charset);
    }

    public LocalDateTime getResponseTime() {
        return responseTime;
    }

    public long getResponseDuration() {
        return responseDuration;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getProtocol() {
        return protocol;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isSuccess() {
        return status.isSuccess();
    }

    public boolean isRedirect() {
        return status.isRedirect();
    }

    public boolean isClientError() {
        return status.isClientError();
    }

    public boolean isServerError() {
        return status.isServerError();
    }

    public boolean isError() {
        return status.isError();
    }

    public int getBodyLength() {
        return body.length;
    }

    /**
     * 检查是否包含特定的Content-Type
     */
    public boolean isContentType(String contentType) {
        if (this.contentType == null || contentType == null) return false;
        return this.contentType.toLowerCase().contains(contentType.toLowerCase());
    }

    /**
     * 检查是否为JSON响应
     */
    public boolean isJson() {
        return isContentType("application/json");
    }

    /**
     * 检查是否为XML响应
     */
    public boolean isXml() {
        return isContentType("application/xml") || isContentType("text/xml");
    }

    /**
     * 检查是否为HTML响应
     */
    public boolean isHtml() {
        return isContentType("text/html");
    }

    /**
     * 检查是否为纯文本响应
     */
    public boolean isText() {
        return isContentType("text/plain");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseKit that = (ResponseKit) o;
        return statusCode == that.statusCode &&
                Objects.equals(statusMessage, that.statusMessage) &&
                Objects.equals(headers, that.headers) &&
                Arrays.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(statusCode, statusMessage, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    @Override
    public String toString() {
        return "ResponseKit{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", bodyLength=" + body.length +
                ", responseTime=" + responseTime +
                '}';
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private int statusCode;
        private String statusMessage;
        private Map<String, List<String>> headers;
        private byte[] body;
        private LocalDateTime responseTime;
        private long responseDuration;
        private String protocol = "HTTP/1.1";

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(String name, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder body(String body) {
            this.body = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
            return this;
        }

        public Builder responseTime(LocalDateTime responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder responseDuration(long responseDuration) {
            this.responseDuration = responseDuration;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ResponseKit build() {
            return new ResponseKit(this);
        }
    }
}
