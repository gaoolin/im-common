package org.im.semiconductor.common.handler.cmd;

import org.im.semiconductor.common.handler.AbstractHandler;

/**
 * 命令处理器抽象类
 * <p>
 * 特性：
 * - 通用性：支持各种命令处理场景
 * - 规范性：遵循统一的命令处理标准
 * - 专业性：提供专业的命令处理能力
 * - 灵活性：支持自定义命令处理逻辑
 * - 可靠性：确保命令处理的稳定性
 * - 安全性：防止命令注入攻击
 * - 复用性：可被各种命令处理场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @param <T> 命令处理器支持的返回类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2024/05/28
 */
public abstract class CommandHandler<T> extends AbstractHandler<T> {

    /**
     * 构造函数
     *
     * @param supportedType 支持的类型
     */
    protected CommandHandler(Class<T> supportedType) {
        super(supportedType);
    }

    /**
     * 处理命令
     *
     * @param parts     命令的部分
     * @param parentCmd 前缀命令（可选）
     * @return 处理结果
     */
    public abstract T handle(String[] parts, String parentCmd);

    /**
     * 处理命令（无前缀命令）
     *
     * @param parts 命令的部分
     * @return 处理结果
     */
    public T handle(String[] parts) {
        return handle(parts, null);
    }

    /**
     * 内部处理方法，将字符串数据行转换为命令部分并处理
     *
     * @param dataLine 待处理的数据行
     * @return 处理结果
     * @throws org.apache.commons.codec.DecoderException 解码异常
     */
    @Override
    protected T handleInternal(String dataLine) throws org.apache.commons.codec.DecoderException {
        if (dataLine == null || dataLine.trim().isEmpty()) {
            throw new IllegalArgumentException("Data line cannot be null or empty");
        }

        String[] parts = dataLine.trim().split("\\s+");
        return handle(parts);
    }
}
