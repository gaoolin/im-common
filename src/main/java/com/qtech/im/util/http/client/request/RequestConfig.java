package com.qtech.im.util.http.client.request;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端配置类
 * <p>
 * 特性：
 * - 通用性：支持所有HTTP客户端通用配置项
 * - 灵活性：支持链式调用和构建器模式
 * - 可扩展性：易于添加新的配置项
 * - 规范性：遵循HTTP协议标准和最佳实践
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 */
public class RequestConfig {
    // 连接配置
    private int connectTimeout = 5000; // 毫秒
    private int readTimeout = 10000;   // 毫秒
    private int writeTimeout = 10000;  // 毫秒

    // 连接池配置
    private int maxConnections = 100;
    private int maxConnectionsPerRoute = 20;
    private long keepAliveDuration = 300; // 秒
    private TimeUnit keepAliveTimeUnit = TimeUnit.SECONDS;

    // 重试配置
    private int maxRetries = 3;
    private long retryInterval = 1000; // 毫秒
    private boolean exponentialBackoff = true;

    // 请求配置
    private String userAgent = "RequestKit/1.0";
    private String defaultContentType = "application/json; charset=UTF-8";
    private boolean followRedirects = true;
    private boolean useCaches = false;

    // 响应配置
    private boolean enableGzip = true;
    private int maxResponseSize = 10 * 1024 * 1024; // 10MB

    // 安全配置
    private boolean verifySsl = true;
    private boolean verifyHostname = true;
    private String[] supportedProtocols = {"TLSv1.2", "TLSv1.3"};
    private String[] supportedCipherSuites = null; // 使用默认

    // 状态码配置
    private int successStatusStart = 200;
    private int successStatusEnd = 299;

    // 容错配置
    private boolean failOnServerError = false; // 是否在5xx错误时立即失败
    private boolean failOnClientError = false; // 是否在4xx错误时立即失败

    /**
     * 默认构造函数
     */
    public RequestConfig() {
    }

    /**
     * 构建器模式构造函数
     */
    private RequestConfig(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.maxConnections = builder.maxConnections;
        this.maxConnectionsPerRoute = builder.maxConnectionsPerRoute;
        this.keepAliveDuration = builder.keepAliveDuration;
        this.keepAliveTimeUnit = builder.keepAliveTimeUnit;
        this.maxRetries = builder.maxRetries;
        this.retryInterval = builder.retryInterval;
        this.exponentialBackoff = builder.exponentialBackoff;
        this.userAgent = builder.userAgent;
        this.defaultContentType = builder.defaultContentType;
        this.followRedirects = builder.followRedirects;
        this.useCaches = builder.useCaches;
        this.enableGzip = builder.enableGzip;
        this.maxResponseSize = builder.maxResponseSize;
        this.verifySsl = builder.verifySsl;
        this.verifyHostname = builder.verifyHostname;
        this.supportedProtocols = builder.supportedProtocols;
        this.supportedCipherSuites = builder.supportedCipherSuites;
        this.successStatusStart = builder.successStatusStart;
        this.successStatusEnd = builder.successStatusEnd;
        this.failOnServerError = builder.failOnServerError;
        this.failOnClientError = builder.failOnClientError;
    }

    /**
     * 获取构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建默认配置
     */
    public static RequestConfig defaultConfig() {
        return new RequestConfig();
    }

    /**
     * 创建宽松配置（适用于测试环境）
     */
    public static RequestConfig looseConfig() {
        return RequestConfig.builder()
                .verifySsl(false)
                .verifyHostname(false)
                .maxRetries(5)
                .build();
    }

    /**
     * 创建严格配置（适用于生产环境）
     */
    public static RequestConfig strictConfig() {
        return RequestConfig.builder()
                .verifySsl(true)
                .verifyHostname(true)
                .maxRetries(2)
                .failOnServerError(true)
                .build();
    }

    // Getters and Setters (链式)
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public RequestConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public RequestConfig setConnectTimeout(Duration duration) {
        this.connectTimeout = (int) duration.toMillis();
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public RequestConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public RequestConfig setReadTimeout(Duration duration) {
        this.readTimeout = (int) duration.toMillis();
        return this;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public RequestConfig setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public RequestConfig setWriteTimeout(Duration duration) {
        this.writeTimeout = (int) duration.toMillis();
        return this;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public RequestConfig setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public RequestConfig setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        return this;
    }

    public long getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public RequestConfig setKeepAliveDuration(long keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
        return this;
    }

    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    public RequestConfig setKeepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
        this.keepAliveTimeUnit = keepAliveTimeUnit;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public RequestConfig setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public RequestConfig setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }

    public RequestConfig setExponentialBackoff(boolean exponentialBackoff) {
        this.exponentialBackoff = exponentialBackoff;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public RequestConfig setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getDefaultContentType() {
        return defaultContentType;
    }

    public RequestConfig setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
        return this;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public RequestConfig setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public boolean isUseCaches() {
        return useCaches;
    }

    public RequestConfig setUseCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }

    public boolean isEnableGzip() {
        return enableGzip;
    }

    public RequestConfig setEnableGzip(boolean enableGzip) {
        this.enableGzip = enableGzip;
        return this;
    }

    public int getMaxResponseSize() {
        return maxResponseSize;
    }

    public RequestConfig setMaxResponseSize(int maxResponseSize) {
        this.maxResponseSize = maxResponseSize;
        return this;
    }

    public boolean isVerifySsl() {
        return verifySsl;
    }

    public RequestConfig setVerifySsl(boolean verifySsl) {
        this.verifySsl = verifySsl;
        return this;
    }

    public boolean isVerifyHostname() {
        return verifyHostname;
    }

    public RequestConfig setVerifyHostname(boolean verifyHostname) {
        this.verifyHostname = verifyHostname;
        return this;
    }

    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    public RequestConfig setSupportedProtocols(String[] supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
        return this;
    }

    public String[] getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    public RequestConfig setSupportedCipherSuites(String[] supportedCipherSuites) {
        this.supportedCipherSuites = supportedCipherSuites;
        return this;
    }

    public int getSuccessStatusStart() {
        return successStatusStart;
    }

    public RequestConfig setSuccessStatusStart(int successStatusStart) {
        this.successStatusStart = successStatusStart;
        return this;
    }

    public int getSuccessStatusEnd() {
        return successStatusEnd;
    }

    public RequestConfig setSuccessStatusEnd(int successStatusEnd) {
        this.successStatusEnd = successStatusEnd;
        return this;
    }

    public boolean isFailOnServerError() {
        return failOnServerError;
    }

    public RequestConfig setFailOnServerError(boolean failOnServerError) {
        this.failOnServerError = failOnServerError;
        return this;
    }

    public boolean isFailOnClientError() {
        return failOnClientError;
    }

    public RequestConfig setFailOnClientError(boolean failOnClientError) {
        this.failOnClientError = failOnClientError;
        return this;
    }

    /**
     * 构建器类
     */
    public static class Builder {
        // 连接配置
        private int connectTimeout = 5000;
        private int readTimeout = 10000;
        private int writeTimeout = 10000;

        // 连接池配置
        private int maxConnections = 100;
        private int maxConnectionsPerRoute = 20;
        private long keepAliveDuration = 300;
        private TimeUnit keepAliveTimeUnit = TimeUnit.SECONDS;

        // 重试配置
        private int maxRetries = 3;
        private long retryInterval = 1000;
        private boolean exponentialBackoff = true;

        // 请求配置
        private String userAgent = "RequestKit/1.0";
        private String defaultContentType = "application/json; charset=UTF-8";
        private boolean followRedirects = true;
        private boolean useCaches = false;

        // 响应配置
        private boolean enableGzip = true;
        private int maxResponseSize = 10 * 1024 * 1024;

        // 安全配置
        private boolean verifySsl = true;
        private boolean verifyHostname = true;
        private String[] supportedProtocols = {"TLSv1.2", "TLSv1.3"};
        private String[] supportedCipherSuites = null;

        // 状态码配置
        private int successStatusStart = 200;
        private int successStatusEnd = 299;

        // 容错配置
        private boolean failOnServerError = false;
        private boolean failOnClientError = false;

        public Builder() {
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder connectTimeout(Duration duration) {
            this.connectTimeout = (int) duration.toMillis();
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder readTimeout(Duration duration) {
            this.readTimeout = (int) duration.toMillis();
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder writeTimeout(Duration duration) {
            this.writeTimeout = (int) duration.toMillis();
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        public Builder keepAliveDuration(long keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Builder keepAliveTimeUnit(TimeUnit keepAliveTimeUnit) {
            this.keepAliveTimeUnit = keepAliveTimeUnit;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public Builder exponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder defaultContentType(String defaultContentType) {
            this.defaultContentType = defaultContentType;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder useCaches(boolean useCaches) {
            this.useCaches = useCaches;
            return this;
        }

        public Builder enableGzip(boolean enableGzip) {
            this.enableGzip = enableGzip;
            return this;
        }

        public Builder maxResponseSize(int maxResponseSize) {
            this.maxResponseSize = maxResponseSize;
            return this;
        }

        public Builder verifySsl(boolean verifySsl) {
            this.verifySsl = verifySsl;
            return this;
        }

        public Builder verifyHostname(boolean verifyHostname) {
            this.verifyHostname = verifyHostname;
            return this;
        }

        public Builder supportedProtocols(String[] supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
            return this;
        }

        public Builder supportedCipherSuites(String[] supportedCipherSuites) {
            this.supportedCipherSuites = supportedCipherSuites;
            return this;
        }

        public Builder successStatusStart(int successStatusStart) {
            this.successStatusStart = successStatusStart;
            return this;
        }

        public Builder successStatusEnd(int successStatusEnd) {
            this.successStatusEnd = successStatusEnd;
            return this;
        }

        public Builder failOnServerError(boolean failOnServerError) {
            this.failOnServerError = failOnServerError;
            return this;
        }

        public Builder failOnClientError(boolean failOnClientError) {
            this.failOnClientError = failOnClientError;
            return this;
        }

        public Builder fromConfig(RequestConfig config) {
            this.connectTimeout = config.getConnectTimeout();
            this.readTimeout = config.getReadTimeout();
            this.writeTimeout = config.getWriteTimeout();
            this.maxConnections = config.getMaxConnections();
            this.maxConnectionsPerRoute = config.getMaxConnectionsPerRoute();
            this.keepAliveDuration = config.getKeepAliveDuration();
            this.keepAliveTimeUnit = config.getKeepAliveTimeUnit();
            this.maxRetries = config.getMaxRetries();
            this.retryInterval = config.getRetryInterval();
            this.exponentialBackoff = config.isExponentialBackoff();
            this.userAgent = config.getUserAgent();
            this.defaultContentType = config.getDefaultContentType();
            this.followRedirects = config.isFollowRedirects();
            this.useCaches = config.isUseCaches();
            this.enableGzip = config.isEnableGzip();
            this.maxResponseSize = config.getMaxResponseSize();
            this.verifySsl = config.isVerifySsl();
            this.verifyHostname = config.isVerifyHostname();
            this.supportedProtocols = config.getSupportedProtocols();
            this.supportedCipherSuites = config.getSupportedCipherSuites();
            this.successStatusStart = config.getSuccessStatusStart();
            this.successStatusEnd = config.getSuccessStatusEnd();
            this.failOnServerError = config.isFailOnServerError();
            this.failOnClientError = config.isFailOnClientError();
            return this;
        }

        public RequestConfig build() {
            return new RequestConfig(this);
        }
    }
}
