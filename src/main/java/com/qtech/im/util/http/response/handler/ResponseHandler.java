// http/response/ResponseHandler.java
package com.qtech.im.util.http.response.handler;

import com.qtech.im.util.http.response.ResponseHandlingException;
import com.qtech.im.util.http.response.ResponseKit;

/**
 * 响应处理器接口
 * <p>
 * 特性：
 * - 通用性：支持各种类型的响应处理
 * - 规范性：定义标准的响应处理流程
 * - 专业性：提供专业化的响应处理能力
 * - 灵活性：支持自定义处理逻辑
 * - 可靠性：确保响应处理的稳定性
 * - 安全性：提供安全的响应处理机制
 * - 复用性：可被多种场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @param <T> 处理结果类型
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public interface ResponseHandler<T> {

    /**
     * 处理HTTP响应
     *
     * @param response HTTP响应
     * @return 处理结果
     * @throws ResponseHandlingException 响应处理异常
     */
    T handle(ResponseKit response) throws ResponseHandlingException;

    /**
     * 验证响应是否可处理
     *
     * @param response HTTP响应
     * @return true表示可处理，false表示不可处理
     */
    boolean canHandle(ResponseKit response);

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    String getName();
}
