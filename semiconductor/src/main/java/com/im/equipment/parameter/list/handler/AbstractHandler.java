package com.im.equipment.parameter.list.handler;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象处理器基类
 * <p>
 * 特性：
 * - 通用性：提供处理器的通用实现
 * - 规范性：遵循统一的处理器标准
 * - 专业性：提供专业的处理能力
 * - 灵活性：支持自定义处理逻辑
 * - 可靠性：确保处理的稳定性
 * - 安全性：防止注入攻击
 * - 复用性：可被各种处理场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @param <T> 处理器支持的参数类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/25
 */
public abstract class AbstractHandler<T> implements Handler<T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<T> supportedType;

    /**
     * 构造函数
     *
     * @param supportedType 支持的类型
     */
    protected AbstractHandler(Class<T> supportedType) {
        this.supportedType = supportedType;
    }

    /**
     * 根据目标类型处理数据行
     *
     * @param targetType 目标类型，不能为null
     * @param dataLine   待处理的数据行，不能为null
     * @param <R>        返回类型，必须与targetType兼容
     * @return 处理结果对象，当无法处理或处理失败时返回null
     * @throws DecoderException         当数据解码失败时抛出
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    @Override
    public <R> R handleByType(Class<R> targetType, String dataLine) throws DecoderException {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }
        if (dataLine == null) {
            throw new IllegalArgumentException("Data line cannot be null");
        }

        try {
            if (supportsType(targetType)) {
                @SuppressWarnings("unchecked")
                R result = (R) handleInternal(dataLine);
                return result;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while handling data line: {}", dataLine, e);
            throw new DecoderException("Failed to handle data line", e);
        }
    }

    /**
     * 内部处理方法，由子类实现具体的处理逻辑
     *
     * @param dataLine 待处理的数据行
     * @return 处理结果
     * @throws DecoderException 解码异常
     */
    protected abstract T handleInternal(String dataLine) throws DecoderException;

    /**
     * 判断处理器是否支持指定的目标类型
     *
     * @param targetType 目标类型，可以为null
     * @param <U>        类型参数
     * @return true表示支持该类型，false表示不支持
     */
    @Override
    public <U> boolean supportsType(Class<U> targetType) {
        return targetType != null && supportedType.isAssignableFrom(targetType);
    }

    /**
     * 获取处理器支持的参数类型
     *
     * @return 支持的参数类型，不能为null
     */
    @Override
    public Class<T> getSupportedType() {
        return supportedType;
    }

    /**
     * 验证输入数据的有效性
     *
     * @param dataLine 待验证的数据行，不能为null
     * @return 验证结果，true表示数据有效，false表示数据无效
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    @Override
    public boolean validateInput(String dataLine) {
        if (dataLine == null) {
            throw new IllegalArgumentException("Data line cannot be null");
        }
        return !dataLine.trim().isEmpty();
    }
}
