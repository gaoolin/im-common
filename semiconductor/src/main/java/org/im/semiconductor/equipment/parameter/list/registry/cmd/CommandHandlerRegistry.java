package org.im.semiconductor.equipment.parameter.list.registry.cmd;

import org.im.semiconductor.equipment.parameter.list.handler.cmd.CommandHandler;
import org.im.semiconductor.equipment.parameter.list.registry.AbstractRegistry;

/**
 * 命令处理器注册表
 * <p>
 * 特性：
 * - 通用性：支持多种命令处理器类型注册和管理
 * - 规范性：统一的命令处理器管理接口和数据结构
 * - 灵活性：可配置的命令处理器注册和获取策略
 * - 可靠性：完善的管理流程和错误处理机制
 * - 安全性：线程安全的命令处理器缓存管理
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/25
 */
public class CommandHandlerRegistry extends AbstractRegistry<String, CommandHandler<?>> {
    // 饿汉式单例实例
    private static final CommandHandlerRegistry INSTANCE = new CommandHandlerRegistry();

    // 私有构造函数，防止外部实例化
    private CommandHandlerRegistry() {
    }

    /**
     * 获取单例实例
     *
     * @return CommandHandlerRegistry单例实例
     */
    public static CommandHandlerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 获取处理器
     *
     * @param handlerName 处理器名称
     * @return 处理器实例，如果未找到则返回null
     */
    public CommandHandler<?> getHandler(String handlerName) {
        return get(handlerName);
    }

    /**
     * 获取所有已注册的处理器名称
     *
     * @return 处理器名称集合
     */
    public java.util.Set<String> getHandlerNames() {
        return getKeys();
    }
}
