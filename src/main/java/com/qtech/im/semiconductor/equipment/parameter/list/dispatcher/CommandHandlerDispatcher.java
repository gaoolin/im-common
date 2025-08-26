package com.qtech.im.semiconductor.equipment.parameter.list.dispatcher;

import com.qtech.im.semiconductor.equipment.parameter.list.handler.cmd.CommandHandler;
import com.qtech.im.semiconductor.equipment.parameter.list.registry.cmd.CommandHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AA命令处理器分发器（饿汉式单例）
 * <p>
 * 特性：
 * - 通用性：支持多种AA命令处理器类型分发
 * - 规范性：统一的命令处理器分发接口和数据结构
 * - 灵活性：可配置的命令处理器注册和获取策略
 * - 可靠性：完善的分发流程和错误处理机制
 * - 安全性：线程安全的命令处理器缓存管理
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2024/05/28
 */
public class CommandHandlerDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerDispatcher.class);

    // 饿汉式单例实例
    private static final CommandHandlerDispatcher INSTANCE = new CommandHandlerDispatcher();

    // 命令处理器注册表
    private volatile CommandHandlerRegistry commandHandlerRegistry;

    // 私有构造函数，防止外部实例化
    private CommandHandlerDispatcher() {
        // 初始化处理器注册表
        this.commandHandlerRegistry = CommandHandlerRegistry.getInstance();
    }

    /**
     * 获取单例实例
     *
     * @return HandlerDispatcher单例实例
     */
    public static CommandHandlerDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * 设置命令处理器注册表
     *
     * @param commandHandlerRegistry 处理器注册表
     */
    public void setHandlerRegistry(CommandHandlerRegistry commandHandlerRegistry) {
        this.commandHandlerRegistry = commandHandlerRegistry;
    }

    /**
     * 根据处理器名称获取命令处理器
     *
     * @param handlerName 处理器名称
     * @param <T>         处理器类型
     * @return 命令处理器实例，如果未找到则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> CommandHandler<T> getCommandHandler(String handlerName) {
        if (handlerName == null || handlerName.isEmpty()) {
            logger.warn("Invalid handler name provided for retrieval");
            return null;
        }

        if (commandHandlerRegistry != null) {
            CommandHandler<?> handler = commandHandlerRegistry.getHandler(handlerName);
            if (handler != null) {
                return (CommandHandler<T>) handler;
            }
        }

        logger.warn("Command handler not found for name: {}", handlerName);
        return null;
    }

    /**
     * 注册命令处理器
     *
     * @param handlerName    处理器名称
     * @param commandHandler 命令处理器实例
     * @param <T>            处理器类型
     * @return 是否注册成功
     */
    public <T> boolean registerHandler(String handlerName, CommandHandler<T> commandHandler) {
        if (handlerName == null || handlerName.isEmpty()) {
            logger.warn("Invalid handler name provided for registration");
            return false;
        }

        if (commandHandler == null) {
            logger.warn("Null handler provided for registration with name: {}", handlerName);
            return false;
        }

        if (commandHandlerRegistry != null) {
            return commandHandlerRegistry.register(handlerName, commandHandler);
        }

        logger.warn("Command handler registry is not initialized");
        return false;
    }

    /**
     * 移除命令处理器
     *
     * @param handlerName 处理器名称
     * @return 是否移除成功
     */
    public boolean removeHandler(String handlerName) {
        if (handlerName == null || handlerName.isEmpty()) {
            logger.warn("Invalid handler name provided for removal");
            return false;
        }

        if (commandHandlerRegistry != null) {
            return commandHandlerRegistry.remove(handlerName);
        }

        logger.warn("Command handler registry is not initialized");
        return false;
    }

    /**
     * 检查是否存在指定名称的命令处理器
     *
     * @param handlerName 处理器名称
     * @return 是否存在
     */
    public boolean containsHandler(String handlerName) {
        if (handlerName == null || handlerName.isEmpty()) {
            return false;
        }

        if (commandHandlerRegistry != null) {
            return commandHandlerRegistry.contains(handlerName);
        }

        return false;
    }

    /**
     * 获取已注册的命令处理器数量
     *
     * @return 处理器数量
     */
    public int getHandlerCount() {
        if (commandHandlerRegistry != null) {
            return commandHandlerRegistry.size();
        }
        return 0;
    }

    /**
     * 清空所有已注册的命令处理器
     */
    public void clearHandlers() {
        if (commandHandlerRegistry != null) {
            commandHandlerRegistry.clear();
            logger.debug("Cleared all command handlers");
        } else {
            logger.warn("Command handler registry is not initialized");
        }
    }

    /**
     * 获取所有已注册的处理器名称
     *
     * @return 处理器名称集合
     */
    public java.util.Set<String> getHandlerNames() {
        if (commandHandlerRegistry != null) {
            return commandHandlerRegistry.getHandlerNames();
        }
        return new java.util.HashSet<>();
    }
}
