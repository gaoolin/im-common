package com.qtech.im.util.http.client.request;

import java.util.Objects;

/**
 * HTTP方法枚举类
 * <p>
 * 特性：
 * - 通用性：支持所有标准HTTP方法
 * - 规范性：遵循RFC 7231标准
 * - 专业性：提供方法属性和行为定义
 * - 灵活性：支持扩展自定义方法
 * - 可靠性：提供完整的验证机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public class RequestMethod {
    // 标准HTTP方法
    public static final RequestMethod GET = new RequestMethod("GET", true, false, true);
    public static final RequestMethod HEAD = new RequestMethod("HEAD", true, false, true);
    public static final RequestMethod POST = new RequestMethod("POST", false, true, false);
    public static final RequestMethod PUT = new RequestMethod("PUT", false, true, false);
    public static final RequestMethod PATCH = new RequestMethod("PATCH", false, true, false);
    public static final RequestMethod DELETE = new RequestMethod("DELETE", false, true, true);
    public static final RequestMethod OPTIONS = new RequestMethod("OPTIONS", true, false, true);
    public static final RequestMethod TRACE = new RequestMethod("TRACE", true, false, true);

    private final String name;
    private final boolean safe;           // 安全方法（不会修改服务器状态）
    private final boolean idempotent;     // 幂等方法（多次执行效果相同）
    private final boolean cacheable;      // 可缓存方法

    /**
     * 构造标准HTTP方法
     *
     * @param name       方法名称
     * @param safe       是否为安全方法
     * @param idempotent 是否为幂等方法
     * @param cacheable  是否可缓存
     */
    private RequestMethod(String name, boolean safe, boolean idempotent, boolean cacheable) {
        this.name = name;
        this.safe = safe;
        this.idempotent = idempotent;
        this.cacheable = cacheable;
    }

    /**
     * 创建自定义HTTP方法
     *
     * @param name       方法名称
     * @param safe       是否为安全方法
     * @param idempotent 是否为幂等方法
     * @param cacheable  是否可缓存
     * @return HttpMethod实例
     */
    public static RequestMethod custom(String name, boolean safe, boolean idempotent, boolean cacheable) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method name cannot be null or empty");
        }
        return new RequestMethod(name.toUpperCase().trim(), safe, idempotent, cacheable);
    }

    /**
     * 根据名称获取HTTP方法
     *
     * @param name 方法名称
     * @return 对应的HttpMethod实例
     */
    public static RequestMethod fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method name cannot be null or empty");
        }

        String upperName = name.toUpperCase().trim();
        switch (upperName) {
            case "GET":
                return GET;
            case "HEAD":
                return HEAD;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "PATCH":
                return PATCH;
            case "DELETE":
                return DELETE;
            case "OPTIONS":
                return OPTIONS;
            case "TRACE":
                return TRACE;
            default:
                return custom(upperName, false, false, false);
        }
    }

    /**
     * 验证方法是否支持请求体
     *
     * @return true表示支持请求体，false表示不支持
     */
    public boolean supportsRequestBody() {
        return !safe; // 通常安全方法不支持请求体
    }

    /**
     * 验证方法是否支持响应体
     *
     * @return true表示支持响应体，false表示不支持
     */
    public boolean supportsResponseBody() {
        return this != HEAD; // HEAD方法不返回响应体
    }

    /**
     * 获取方法名称
     *
     * @return 方法名称
     */
    public String getName() {
        return name;
    }

    /**
     * 判断是否为安全方法
     * 安全方法不会修改服务器状态，如GET、HEAD、OPTIONS等
     *
     * @return true表示安全方法
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * 判断是否为幂等方法
     * 幂等方法多次执行效果相同，如GET、PUT、DELETE等
     *
     * @return true表示幂等方法
     */
    public boolean isIdempotent() {
        return idempotent;
    }

    /**
     * 判断是否可缓存
     * 可缓存方法的响应可以被缓存，如GET、HEAD等
     *
     * @return true表示可缓存
     */
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMethod that = (RequestMethod) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
