package org.im.semiconductor.equipment.parameter.list.dispatcher;

import org.apache.commons.codec.DecoderException;
import org.im.semiconductor.equipment.parameter.list.handler.msg.MessageHandler;
import org.im.semiconductor.equipment.parameter.list.registry.msg.MessageHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 通用消息分发器
 * <p>
 * 特性：
 * - 通用性：支持多种消息类型分发
 * - 规范性：统一的消息分发接口和数据结构
 * - 灵活性：可配置的消息处理器注册和获取策略
 * - 可靠性：完善的分发流程和错误处理机制
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
public class MessageHandlerDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerDispatcher.class);

    // 饿汉式单例实例
    private static final MessageHandlerDispatcher INSTANCE = new MessageHandlerDispatcher();

    // 消息处理器注册表
    private volatile MessageHandlerRegistry messageHandlerRegistry;

    // 私有构造函数，防止外部实例化
    private MessageHandlerDispatcher() {
        // 初始化处理器注册表
        this.messageHandlerRegistry = MessageHandlerRegistry.getInstance();
    }

    /**
     * 获取单例实例
     *
     * @return MessageProcessor单例实例
     */
    public static MessageHandlerDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * 设置消息处理器注册表
     *
     * @param messageHandlerRegistry 处理器注册表
     */
    public void setHandlerRegistry(MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * 处理消息
     *
     * @param clazz 消息类型Class
     * @param msg   消息内容
     * @param <R>   返回类型
     * @return 处理结果
     */
    @SuppressWarnings("unchecked")
    public <R> R processMessage(Class<R> clazz, String msg) {
        if (clazz == null) {
            logger.warn("Null class provided for message processing");
            return null;
        }

        if (messageHandlerRegistry != null) {
            // 从注册表中查找处理器
            String handlerKey = clazz.getSimpleName();
            MessageHandler<?> messageHandler = messageHandlerRegistry.getHandler(handlerKey);

            if (messageHandler != null) {
                try {
                    MessageHandler<R> typedHandler = (MessageHandler<R>) messageHandler;
                    return typedHandler.handleByType(clazz, msg);
                } catch (ClassCastException | DecoderException e) {
                    logger.error("Handler did not return expected type for class: {}", clazz.getName(), e);
                    throw new IllegalStateException("Handler did not return expected type.", e);
                } catch (Exception e) {
                    logger.error("Unexpected error occurred while handling message for class: {}", clazz.getName(), e);
                    throw new RuntimeException("Error processing message.", e);
                }
            }
        }

        logger.warn("No handler found for message type: {}", clazz.getName());
        return null;
    }

    /**
     * 注册消息处理器
     *
     * @param clazz          消息类型Class
     * @param messageHandler 消息处理器
     * @param <T>            处理器类型
     * @return 是否注册成功
     */
    public <T> boolean registerHandler(Class<T> clazz, MessageHandler<T> messageHandler) {
        if (clazz == null) {
            logger.warn("Invalid class provided for handler registration");
            return false;
        }

        if (messageHandler == null) {
            logger.warn("Null handler provided for registration with class: {}", clazz.getName());
            return false;
        }

        if (messageHandlerRegistry != null) {
            boolean result = messageHandlerRegistry.register(clazz.getSimpleName(), messageHandler);
            if (result && logger.isDebugEnabled()) {
                logger.debug("Registered message handler for class: {}", clazz.getName());
            }
            return result;
        }

        logger.warn("Message handler registry is not initialized");
        return false;
    }

    /**
     * 移除消息处理器
     *
     * @param clazz 消息类型Class
     * @return 是否移除成功
     */
    public boolean removeHandler(Class<?> clazz) {
        if (clazz == null) {
            logger.warn("Invalid class provided for handler removal");
            return false;
        }

        if (messageHandlerRegistry != null) {
            boolean result = messageHandlerRegistry.remove(clazz.getSimpleName());
            if (result && logger.isDebugEnabled()) {
                logger.debug("Removed message handler for class: {}", clazz.getName());
            }
            return result;
        }

        logger.warn("Message handler registry is not initialized");
        return false;
    }

    /**
     * 检查是否存在指定类型的消息处理器
     *
     * @param clazz 消息类型Class
     * @return 是否存在
     */
    public boolean containsHandler(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (messageHandlerRegistry != null) {
            return messageHandlerRegistry.contains(clazz.getSimpleName());
        }

        return false;
    }

    /**
     * 获取已注册的消息处理器数量
     *
     * @return 处理器数量
     */
    public int getHandlerCount() {
        if (messageHandlerRegistry != null) {
            return messageHandlerRegistry.size();
        }
        return 0;
    }

    /**
     * 清空所有已注册的消息处理器
     */
    public void clearHandlers() {
        if (messageHandlerRegistry != null) {
            messageHandlerRegistry.clear();
            if (logger.isDebugEnabled()) {
                logger.debug("Cleared all message handlers");
            }
        } else {
            logger.warn("Message handler registry is not initialized");
        }
    }

    /**
     * 获取所有已注册的消息处理器类型
     *
     * @return 处理器类型集合
     */
    public Set<Class<?>> getHandlerTypes() {
        if (messageHandlerRegistry != null) {
            // 由于注册表中存储的是String键，这里返回空集合
            return new HashSet<>();
        }
        return new HashSet<>();
    }
}
