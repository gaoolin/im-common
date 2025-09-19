package com.im.equipment.parameter.list.registry.msg;

import com.im.equipment.parameter.list.handler.msg.MessageHandler;
import com.im.equipment.parameter.list.registry.AbstractRegistry;

/**
 * 消息处理器注册表
 * <p>
 * 特性：
 * - 通用性：支持多种消息处理器类型注册和管理
 * - 规范性：统一的消息处理器管理接口和数据结构
 * - 灵活性：可配置的消息处理器注册和获取策略
 * - 可靠性：完善的消息处理器管理流程和错误处理机制
 * - 安全性：线程安全的消息处理器缓存管理
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/25
 */
public class MessageHandlerRegistry extends AbstractRegistry<String, MessageHandler<?>> {
    // 饿汉式单例实例
    private static final MessageHandlerRegistry INSTANCE = new MessageHandlerRegistry();

    // 私有构造函数，防止外部实例化
    private MessageHandlerRegistry() {
    }

    /**
     * 获取单例实例
     *
     * @return MessageHandlerRegistry单例实例
     */
    public static MessageHandlerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 根据键获取消息处理器
     *
     * @param messageType 消息类型
     * @return 消息处理器实例，如果未找到则返回null
     */
    public MessageHandler<?> getHandler(String messageType) {
        return get(messageType);
    }

    /**
     * 获取所有已注册的消息处理器类型
     *
     * @return 消息处理器类型集合
     */
    public java.util.Set<String> getMessageTypes() {
        return getKeys();
    }
}
