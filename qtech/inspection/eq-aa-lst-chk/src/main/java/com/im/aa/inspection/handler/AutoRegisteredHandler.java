package com.im.aa.inspection.handler;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/29
 */

// 文件路径: src/main/java/com/im/aa/inspection/handler/AutoRegisteredHandler.java

/**
 * 自动注册Handler标记接口
 * 实现此接口的Handler将通过ServiceLoader机制自动注册
 *
 * @author gaozhilin
 * @since 2025/09/25
 */
public interface AutoRegisteredHandler<T> {
    /**
     * 创建Handler实例
     *
     * @return Handler实例
     */
    // CommandHandler<T> createInstance();
    Object createInstance(); // 兼顾CommandHandler和MessageHandler
}
