package com.qtech.im.util.http.client.request;

import com.qtech.im.constant.ErrorCode;
import com.qtech.im.exception.HttpException;
import com.qtech.im.util.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * 专业的HTTP客户端工具类
 * <p>
 * 特性：
 * - 通用化：支持GET、POST、PUT、DELETE等常见HTTP方法
 * - 规范化：统一的API接口和响应处理
 * - 灵活性：支持自定义请求头、参数、超时等配置
 * - 复用性：连接池管理，支持并发请求
 * - 容错性：完善的异常处理和重试机制
 * - 可靠性：支持HTTPS、GZIP压缩、连接复用等
 * - 专业性：遵循HTTP协议规范和最佳实践
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/20
 */
public class RequestKit {
    private static final Logger logger = LoggerFactory.getLogger(RequestKit.class);

    // 连接池配置
    private static final int MAX_CONNECTIONS = 100;
    private static final int KEEP_ALIVE_TIME = 300; // 5分钟

    // 全局共享的HTTP客户端实例
    private static final RequestKit SHARED_INSTANCE = new RequestKit();

    // HTTPS信任管理器（用于跳过证书验证，生产环境应谨慎使用）
    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
    };

    // 主机名验证器（用于跳过主机名验证）
    private static final HostnameVerifier TRUST_ALL_HOSTNAME = (hostname, session) -> true;

    // 线程池用于异步请求
    private final ExecutorService executorService;

    // 每个实例独立的Cookie管理器，避免线程安全问题
    private final CookieManager cookieManager;

    // 默认配置
    private final RequestConfig defaultConfig;

    /**
     * 私有构造函数，创建全局单例
     */
    private RequestKit() {
        this(RequestConfig.defaultConfig());
    }

    /**
     * 带配置的构造函数
     *
     * @param config HTTP配置
     */
    private RequestKit(RequestConfig config) {
        this.defaultConfig = config != null ? config : RequestConfig.defaultConfig();

        // 初始化线程池
        this.executorService = new ThreadPoolExecutor(
                10, // 核心线程数
                MAX_CONNECTIONS, // 最大线程数
                KEEP_ALIVE_TIME, // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), // 任务队列
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "RequestKit-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        // 初始化Cookie管理器
        this.cookieManager = new CookieManager();
        this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        // 不设置全局默认CookieHandler，避免线程安全问题

        // 设置默认的SSL上下文（生产环境应使用标准证书验证）
        setupSSLContext();
    }

    /**
     * 获取共享的HTTP客户端实例
     *
     * @return HttpClient实例
     */
    public static RequestKit getInstance() {
        return SHARED_INSTANCE;
    }

    /**
     * 获取带指定配置的HTTP客户端实例
     *
     * @param config HTTP配置
     * @return HttpClient实例
     */
    public static RequestKit getInstance(RequestConfig config) {
        return new RequestKit(config);
    }

    /**
     * 设置SSL上下文以支持HTTPS请求
     */
    private void setupSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(TRUST_ALL_HOSTNAME);
        } catch (Exception e) {
            logger.warn("Failed to setup SSL context", e);
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param request HTTP请求对象
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse execute(HttpRequest request) throws HttpException {
        return execute(request, defaultConfig.getMaxRetries());
    }

    /**
     * 执行HTTP请求（带重试机制）
     *
     * @param request    HTTP请求对象
     * @param maxRetries 最大重试次数
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse execute(HttpRequest request, int maxRetries) throws HttpException {
        Exception lastException = null;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                HttpResponse response = doExecute(request);

                // 检查是否需要重试
                if (i < maxRetries && shouldRetry(response, request)) {
                    logger.warn("Request needs retry ({}), retrying... ({}/{})",
                            response.getStatusCode(), i + 1, maxRetries);
                    try {
                        Thread.sleep(calculateRetryDelay(i)); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new HttpException(ErrorCode.NET_REQUEST_ERROR, "Request interrupted", ie);
                    }
                    continue;
                }

                return response;
            } catch (Exception e) {
                lastException = e;
                if (i < maxRetries) {
                    logger.warn("Request failed, retrying... ({}/{})", i + 1, maxRetries, e);
                    try {
                        Thread.sleep(calculateRetryDelay(i)); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new HttpException(ErrorCode.NET_REQUEST_ERROR, "Request interrupted", ie);
                    }
                }
            }
        }

        throw new HttpException(ErrorCode.NET_REQUEST_ERROR, "Request failed after " + maxRetries + " retries", lastException);
    }

    /**
     * 判断是否需要重试
     *
     * @param response HTTP响应
     * @param request  HTTP请求
     * @return true表示需要重试
     */
    private boolean shouldRetry(HttpResponse response, HttpRequest request) {
        int statusCode = response.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(statusCode);

        // 根据配置决定是否立即失败
        if (defaultConfig.isFailOnServerError() && status.isServerError()) {
            return false; // 配置为立即失败，不重试
        }

        if (defaultConfig.isFailOnClientError() && status.isClientError()) {
            return false; // 配置为立即失败，不重试
        }

        // 服务器错误通常需要重试
        if (status.isServerError()) {
            return true;
        }

        // 特定客户端错误可能需要重试
        switch (statusCode) {
            case 408: // Request Timeout
            case 429: // Too Many Requests
            case 502: // Bad Gateway
            case 503: // Service Unavailable
            case 504: // Gateway Timeout
                return true;
            default:
                return false;
        }
    }

    /**
     * 计算重试延迟时间
     *
     * @param retryCount 重试次数
     * @return 延迟时间（毫秒）
     */
    private long calculateRetryDelay(int retryCount) {
        long baseDelay = defaultConfig.getRetryInterval();
        if (defaultConfig.isExponentialBackoff()) {
            return baseDelay * (1L << retryCount); // 指数退避
        }
        return baseDelay;
    }

    /**
     * 执行HTTP请求的核心方法
     *
     * @param request HTTP请求对象
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    private HttpResponse doExecute(HttpRequest request) throws HttpException {
        HttpURLConnection connection = null;
        try {
            // 构建URL
            URL url = buildUrl(request);

            // 创建连接
            connection = (HttpURLConnection) url.openConnection();

            // 配置连接
            configureConnection(connection, request);

            // 设置请求头
            setRequestHeaders(connection, request);

            // 发送请求体（如果有的话）
            sendRequestBody(connection, request);

            // 获取响应
            return readResponse(connection);
        } catch (Exception e) {
            throw new HttpException(ErrorCode.NET_REQUEST_ERROR, "Failed to execute HTTP request", e);
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    logger.debug("Error disconnecting connection", e);
                }
            }
        }
    }

    /**
     * 构建请求URL
     *
     * @param request HTTP请求对象
     * @return URL对象
     * @throws MalformedURLException URL格式异常
     */
    private URL buildUrl(HttpRequest request) throws MalformedURLException, UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(request.getUrl());

        // 添加查询参数
        Map<String, String> queryParams = request.getQueryParams();
        if (queryParams != null && !queryParams.isEmpty()) {
            boolean hasQuery = urlBuilder.indexOf("?") > 0;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                urlBuilder.append(hasQuery ? "&" : "?")
                        .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                hasQuery = true;
            }
        }

        return new URL(urlBuilder.toString());
    }

    /**
     * 配置HTTP连接
     *
     * @param connection HTTP连接对象
     * @param request    HTTP请求对象
     */
    private void configureConnection(HttpURLConnection connection, HttpRequest request) {
        // 设置请求方法
        try {
            connection.setRequestMethod(request.getMethod().getName());
        } catch (ProtocolException e) {
            logger.warn("Invalid HTTP method: {}", request.getMethod());
        }

        // 设置超时
        connection.setConnectTimeout(request.getConnectTimeout() > 0 ?
                request.getConnectTimeout() : defaultConfig.getConnectTimeout());
        connection.setReadTimeout(request.getReadTimeout() > 0 ?
                request.getReadTimeout() : defaultConfig.getReadTimeout());

        // 设置其他连接属性
        connection.setDoInput(true);
        connection.setDoOutput(request.getMethod().supportsRequestBody());
        connection.setUseCaches(defaultConfig.isUseCaches());
        connection.setInstanceFollowRedirects(defaultConfig.isFollowRedirects());
    }

    /**
     * 设置默认请求头
     *
     * @param connection HTTP连接对象
     */
    private void setDefaultRequestHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", defaultConfig.getUserAgent());
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "keep-alive");

        // 启用GZIP压缩
        if (defaultConfig.isEnableGzip()) {
            connection.setRequestProperty("Accept-Encoding", "gzip");
        }
    }

    /**
     * 设置请求头
     *
     * @param connection HTTP连接对象
     * @param request    HTTP请求对象
     */
    private void setRequestHeaders(HttpURLConnection connection, HttpRequest request) {
        // 设置默认请求头
        setDefaultRequestHeaders(connection);

        // 设置内容类型（对于有请求体的方法）
        if (request.getMethod().supportsRequestBody()) {
            connection.setRequestProperty("Content-Type",
                    request.getContentType() != null ?
                            request.getContentType() : defaultConfig.getDefaultContentType());
        }

        // 设置自定义请求头
        Map<String, String> headers = request.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 发送请求体
     *
     * @param connection HTTP连接对象
     * @param request    HTTP请求对象
     * @throws IOException IO异常
     */
    private void sendRequestBody(HttpURLConnection connection, HttpRequest request) throws IOException {
        String requestBody = request.getBody();
        if (requestBody != null && !requestBody.isEmpty() && request.getMethod().supportsRequestBody()) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
    }

    /**
     * 读取HTTP响应
     *
     * @param connection HTTP连接对象
     * @return HttpResponse响应对象
     * @throws IOException IO异常
     */
    private HttpResponse readResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        String statusMessage = connection.getResponseMessage();

        // 获取响应头
        Map<String, List<String>> headers = connection.getHeaderFields();

        // 读取响应体
        String responseBody = null;
        InputStream is = null;
        try {
            is = getResponseStream(connection);
            if (is != null) {
                responseBody = readInputStream(is);

                // 检查响应大小
                if (responseBody != null && responseBody.length() > defaultConfig.getMaxResponseSize()) {
                    logger.warn("Response body size {} exceeds max allowed size {}",
                            responseBody.length(), defaultConfig.getMaxResponseSize());
                }
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.debug("Error closing input stream", e);
                }
            }
        }

        return new HttpResponse(statusCode, statusMessage, headers, responseBody);
    }

    /**
     * 获取响应流（处理GZIP压缩）
     *
     * @param connection HTTP连接对象
     * @return InputStream响应流
     * @throws IOException IO异常
     */
    private InputStream getResponseStream(HttpURLConnection connection) throws IOException {
        InputStream inputStream;

        // 检查是否是错误响应
        if (connection.getResponseCode() < 400) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        if (inputStream == null) {
            return null;
        }

        // 检查是否是GZIP压缩
        if (defaultConfig.isEnableGzip()) {
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                return new GZIPInputStream(inputStream);
            }
        }

        return inputStream;
    }

    /**
     * 读取输入流内容
     *
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IO异常
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * 异步执行HTTP请求
     *
     * @param request  HTTP请求对象
     * @param callback 回调函数
     */
    public void executeAsync(HttpRequest request, HttpCallback callback) {
        executorService.submit(() -> {
            try {
                HttpResponse response = execute(request);
                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * GET请求快捷方法
     *
     * @param url URL地址
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse get(String url) throws HttpException {
        return execute(new HttpRequest(RequestMethod.GET, url));
    }

    /**
     * GET请求快捷方法（带查询参数）
     *
     * @param url         URL地址
     * @param queryParams 查询参数
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse get(String url, Map<String, String> queryParams) throws HttpException {
        HttpRequest request = new HttpRequest(RequestMethod.GET, url);
        request.setQueryParams(queryParams);
        return execute(request);
    }

    /**
     * POST请求快捷方法
     *
     * @param url  URL地址
     * @param body 请求体
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse post(String url, String body) throws HttpException {
        HttpRequest request = new HttpRequest(RequestMethod.POST, url);
        request.setBody(body);
        return execute(request);
    }

    /**
     * POST请求快捷方法（表单数据）
     *
     * @param url      URL地址
     * @param formData 表单数据
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse postForm(String url, Map<String, String> formData) throws HttpException, UnsupportedEncodingException {
        HttpRequest request = new HttpRequest(RequestMethod.POST, url);
        request.setContentType("application/x-www-form-urlencoded; charset=UTF-8");

        if (formData != null && !formData.isEmpty()) {
            StringBuilder formBody = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (!first) {
                    formBody.append("&");
                }
                formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                first = false;
            }
            request.setBody(formBody.toString());
        }

        return execute(request);
    }

    /**
     * PUT请求快捷方法
     *
     * @param url  URL地址
     * @param body 请求体
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse put(String url, String body) throws HttpException {
        HttpRequest request = new HttpRequest(RequestMethod.PUT, url);
        request.setBody(body);
        return execute(request);
    }

    /**
     * DELETE请求快捷方法
     *
     * @param url URL地址
     * @return HttpResponse响应对象
     * @throws HttpException HTTP异常
     */
    public HttpResponse delete(String url) throws HttpException {
        return execute(new HttpRequest(RequestMethod.DELETE, url));
    }

    /**
     * 关闭HTTP客户端，释放资源
     */
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * HTTP回调接口
     */
    public interface HttpCallback {
        void onSuccess(HttpResponse response);

        void onError(Exception error);
    }

    /**
     * HTTP请求类
     */
    public static class HttpRequest {
        private RequestMethod method;
        private String url;
        private Map<String, String> headers;
        private Map<String, String> queryParams;
        private String body;
        private String contentType;
        private int connectTimeout = -1;
        private int readTimeout = -1;

        public HttpRequest(RequestMethod method, String url) {
            this.method = method;
            this.url = url;
        }

        // Getters and Setters
        public RequestMethod getMethod() {
            return method;
        }

        public void setMethod(RequestMethod method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public void addHeader(String name, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(name, value);
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
        }

        public void addQueryParam(String name, String value) {
            if (this.queryParams == null) {
                this.queryParams = new HashMap<>();
            }
            this.queryParams.put(name, value);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }

    /**
     * HTTP响应类
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String statusMessage;
        private final Map<String, List<String>> headers;
        private final String body;
        private final HttpStatus status;

        public HttpResponse(int statusCode, String statusMessage,
                            Map<String, List<String>> headers, String body) {
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
            this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
            this.body = body;
            this.status = HttpStatus.valueOf(statusCode);
        }

        public boolean isSuccess() {
            return status.isSuccess();
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public String getHeader(String name) {
            List<String> values = headers.get(name);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }

        public List<String> getHeaders(String name) {
            return headers.get(name);
        }

        public String getBody() {
            return body;
        }

        public HttpStatus getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return "ResponseKit{" +
                    "statusCode=" + statusCode +
                    ", statusMessage='" + statusMessage + '\'' +
                    ", headers=" + headers +
                    ", body='" + body + '\'' +
                    '}';
        }
    }
}
