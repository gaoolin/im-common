package com.im.equipment.parameter.list.handler.msg;

import com.im.equipment.parameter.list.handler.AbstractHandler;
import org.apache.commons.codec.DecoderException;

/**
 * 消息处理器抽象类
 * <p>
 * 特性：
 * - 通用性：支持各种消息处理场景
 * - 规范性：遵循统一的消息处理标准
 * - 专业性：提供专业的消息处理能力
 * - 灵活性：支持自定义消息处理逻辑
 * - 可靠性：确保消息处理的稳定性
 * - 安全性：防止消息注入攻击
 * - 复用性：可被各种消息处理场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @param <T> 消息处理器支持的返回类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2024/05/12
 */
public abstract class MessageHandler<T> extends AbstractHandler<T> {

    /**
     * 构造函数
     *
     * @param supportedType 支持的类型
     */
    protected MessageHandler(Class<T> supportedType) {
        super(supportedType);
    }

    /**
     * 处理消息
     *
     * @param msg 消息内容
     * @return 处理结果
     * @throws DecoderException 解码异常
     */
    public abstract T handle(String msg) throws DecoderException;

    /**
     * 内部处理方法，调用具体的handle方法处理消息
     *
     * @param dataLine 待处理的数据行
     * @return 处理结果
     * @throws DecoderException 解码异常
     */
    @Override
    protected T handleInternal(String dataLine) throws DecoderException {
        return handle(dataLine);
    }
}
