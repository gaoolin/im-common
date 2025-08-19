package com.qtech.im.handler;

import com.qtech.im.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 13:44:25
 * desc   :
 * 全局异常处理器
 * <p>
 * 统一处理系统中抛出的异常，提供灵活的异常处理机制
 */
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final GlobalExceptionHandler INSTANCE = new GlobalExceptionHandler();

    // 异常处理器映射表
    private final Map<Class<? extends Throwable>, ExceptionHandler<?>> exceptionHandlers =
            new ConcurrentHashMap<>();

    // 默认异常处理器
    private ExceptionHandler<Throwable> defaultHandler = (exception, context) -> {
        logger.error("未处理的异常: {}", exception.getMessage(), exception);
        return new BaseException("SYS_UNKNOWN_ERROR", "系统未知错误", exception);
    };

    private GlobalExceptionHandler() {
        // 初始化默认处理器
        registerDefaultHandlers();
    }

    public static GlobalExceptionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 注册异常处理器
     *
     * @param exceptionType 异常类型
     * @param handler       处理器
     * @param <T>           异常类型
     */
    public <T extends Throwable> void registerHandler(Class<T> exceptionType,
                                                      ExceptionHandler<T> handler) {
        exceptionHandlers.put(exceptionType, handler);
    }

    /**
     * 设置默认异常处理器
     *
     * @param handler 默认处理器
     */
    public void setDefaultHandler(ExceptionHandler<Throwable> handler) {
        if (handler != null) {
            this.defaultHandler = handler;
        }
    }

    /**
     * 处理异常
     *
     * @param exception 异常
     * @param context   上下文信息
     * @return 处理后的异常
     */
    @SuppressWarnings("unchecked")
    public BaseException handleException(Throwable exception, Map<String, Object> context) {
        // 查找匹配的处理器
        ExceptionHandler<?> handler = findHandler(exception.getClass());

        if (handler != null) {
            try {
                return ((ExceptionHandler<Throwable>) handler).handle(exception, context);
            } catch (Exception e) {
                logger.warn("异常处理器执行失败，使用默认处理器", e);
            }
        }

        // 使用默认处理器
        return defaultHandler.handle(exception, context);
    }

    /**
     * 查找匹配的异常处理器
     *
     * @param exceptionType 异常类型
     * @return 匹配的处理器
     */
    @SuppressWarnings("unchecked")
    private ExceptionHandler<?> findHandler(Class<? extends Throwable> exceptionType) {
        // 精确匹配
        ExceptionHandler<?> handler = exceptionHandlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }

        // 父类匹配
        Class<?> superClass = exceptionType.getSuperclass();
        while (superClass != null && Throwable.class.isAssignableFrom(superClass)) {
            handler = exceptionHandlers.get((Class<? extends Throwable>) superClass);
            if (handler != null) {
                return handler;
            }
            superClass = superClass.getSuperclass();
        }

        return null;
    }

    /**
     * 注册默认异常处理器
     */
    private void registerDefaultHandlers() {
        // 可以在这里注册一些常用的默认处理器
    }
}
