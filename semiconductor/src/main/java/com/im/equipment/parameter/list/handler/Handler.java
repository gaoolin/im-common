package com.im.equipment.parameter.list.handler;

import org.apache.commons.codec.DecoderException;

import java.io.Serializable;

/**
 * 设备参数处理器通用接口
 * <p>
 * 特性：
 * - 通用性：支持各种设备参数处理场景
 * - 规范性：遵循统一的处理器接口标准
 * - 专业性：提供专业的参数处理能力
 * - 灵活性：支持自定义处理逻辑
 * - 可靠性：确保参数处理的稳定性
 * - 安全性：防止参数注入攻击
 * - 复用性：可被各种参数处理场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @param <T> 处理器支持的参数类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2024/05/28
 */
public interface Handler<T> extends Serializable {

    /**
     * 根据目标类型处理数据行
     * <p>
     * 该方法负责将输入的数据行解析为目标类型的对象，是处理器的核心功能。
     * 实现类应确保：
     * 1. 正确解析输入数据
     * 2. 处理可能的格式异常
     * 3. 返回正确类型的对象或null（当无法处理时）
     * </p>
     *
     * @param targetType 目标类型，不能为null
     * @param dataLine   待处理的数据行，不能为null
     * @param <R>        返回类型，必须与targetType兼容
     * @return 处理结果对象，当无法处理或处理失败时返回null
     * @throws DecoderException         当数据解码失败时抛出
     * @throws IllegalArgumentException 当参数不合法时抛出
     * @throws SecurityException        当存在安全风险时抛出
     */
    default <R> R handleByType(Class<R> targetType, String dataLine) throws DecoderException {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }
        if (dataLine == null) {
            throw new IllegalArgumentException("Data line cannot be null");
        }
        return null;
    }

    /**
     * 判断处理器是否支持指定的目标类型
     * <p>
     * 该方法用于检查当前处理器是否能够处理指定类型的参数，避免错误的类型处理。
     * 实现类应确保：
     * 1. 准确判断支持的类型范围
     * 2. 处理null输入的边界情况
     * 3. 返回准确的布尔值
     * </p>
     *
     * @param targetType 目标类型，可以为null
     * @param <U>        类型参数
     * @return true表示支持该类型，false表示不支持
     */
    default <U> boolean supportsType(Class<U> targetType) {
        return targetType != null && targetType.isAssignableFrom(getSupportedType());
    }

    /**
     * 获取处理器支持的参数类型
     * <p>
     * 该方法返回处理器原生支持的参数类型，用于类型检查和匹配。
     * </p>
     *
     * @return 支持的参数类型，不能为null
     */
    Class<T> getSupportedType();

    /**
     * 获取处理器名称
     * <p>
     * 该方法返回处理器的唯一标识名称，用于日志记录和调试。
     * 默认实现基于类名生成，实现类可重写以提供自定义名称。
     * </p>
     *
     * @return 处理器名称，不能为null或空
     */
    default String getHandlerName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取消息类型
     * <p>
     * 该方法返回处理器处理的消息类型，用于消息路由和分类。
     * 默认实现基于类名生成，实现类可重写以提供自定义消息类型。
     * </p>
     *
     * @return 消息类型名称，不能为null或空
     */
    default String getMessageType() {
        String simpleName = this.getClass().getSimpleName();
        if (simpleName.endsWith("Handler")) {
            return simpleName.substring(0, simpleName.length() - 7);
        }
        return simpleName;
    }

    /**
     * 验证输入数据的有效性
     * <p>
     * 该方法用于验证输入数据是否符合处理要求，防止恶意或错误数据导致的问题。
     * 实现类应重写此方法以提供具体的验证逻辑。
     * </p>
     *
     * @param dataLine 待验证的数据行，不能为null
     * @return 验证结果，true表示数据有效，false表示数据无效
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    default boolean validateInput(String dataLine) {
        if (dataLine == null) {
            throw new IllegalArgumentException("Data line cannot be null");
        }
        return !dataLine.trim().isEmpty();
    }

    /**
     * 获取处理器版本信息
     * <p>
     * 该方法返回处理器的版本信息，用于版本控制和兼容性检查。
     * </p>
     *
     * @return 版本信息字符串
     */
    default String getVersion() {
        return "1.0.0";
    }
}
