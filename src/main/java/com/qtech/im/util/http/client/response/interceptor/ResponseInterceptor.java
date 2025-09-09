package com.qtech.im.util.http.client.response.interceptor;

import com.qtech.im.util.http.client.response.ResponseHandlingException;
import com.qtech.im.util.http.client.response.ResponseKit;

/**
 * 响应拦截器接口
 * <p>
 * 特性：
 * - 通用性：支持各种响应拦截场景
 * - 规范性：定义标准的拦截流程
 * - 专业性：提供专业的拦截能力
 * - 灵活性：支持自定义拦截逻辑
 * - 可靠性：确保拦截的稳定性
 * - 安全性：提供安全的拦截机制
 * - 复用性：可被多种场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public interface ResponseInterceptor {

    /**
     * 预处理响应
     *
     * @param response HTTP响应
     * @return 处理后的响应，返回null表示中断处理
     * @throws ResponseHandlingException 响应处理异常
     */
    ResponseKit preHandle(ResponseKit response) throws ResponseHandlingException;

    /**
     * 后处理响应
     *
     * @param response HTTP响应
     * @param result   处理结果
     * @return 处理后的结果
     * @throws ResponseHandlingException 响应处理异常
     */
    <T> T postHandle(ResponseKit response, T result) throws ResponseHandlingException;

    /**
     * 处理完成后的回调
     *
     * @param response HTTP响应
     */
    void afterCompletion(ResponseKit response);

    /**
     * 获取拦截器优先级
     *
     * @return 优先级，数值越小优先级越高
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 获取拦截器名称
     *
     * @return 拦截器名称
     */
    String getName();
}
