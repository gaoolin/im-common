package com.qtech.im.util;

import com.qtech.im.exception.BaseException;
import com.qtech.im.exception.BusinessException;
import com.qtech.im.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * 异常处理工具类
 * <p>
 * 提供统一的异常处理方法和转换功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 13:40:05
 */
public class ExceptionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    private ExceptionUtils() {
        // 私有构造函数防止实例化
    }

    /**
     * 包装受检异常为运行时异常
     *
     * @param supplier 可能抛出异常的操作
     * @param <T>      返回值类型
     * @return 操作结果
     */
    public static <T> T wrapCheckedException(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new BusinessException("WRAPPER_ERROR", "操作执行失败", e);
        }
    }

    /**
     * 安全执行操作，捕获并记录异常
     *
     * @param operation    要执行的操作
     * @param errorMessage 错误信息
     */
    public static void safeExecute(Runnable operation, String errorMessage) {
        try {
            operation.run();
        } catch (Exception e) {
            logger.warn(errorMessage, e);
        }
    }

    /**
     * 将异常转换为指定类型的异常
     *
     * @param throwable 原始异常
     * @param converter 转换函数
     * @param <T>       目标异常类型
     * @return 转换后的异常
     */
    public static <T extends BaseException> T convertException(Throwable throwable, Function<Throwable, T> converter) {
        return converter.apply(throwable);
    }

    /**
     * 获取异常的根原因
     *
     * @param throwable 异常
     * @return 根原因异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 判断是否为业务异常
     *
     * @param throwable 异常
     * @return 是否为业务异常
     */
    public static boolean isBusinessException(Throwable throwable) {
        return throwable instanceof BusinessException;
    }

    /**
     * 判断是否为系统异常
     *
     * @param throwable 异常
     * @return 是否为系统异常
     */
    public static boolean isSystemException(Throwable throwable) {
        return throwable instanceof SystemException;
    }

    /**
     * 受检异常Supplier接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
