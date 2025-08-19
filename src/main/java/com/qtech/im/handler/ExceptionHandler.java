package com.qtech.im.handler;

import com.qtech.im.exception.BaseException;

import java.util.Map;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 13:45:39
 * desc   :
 * 异常处理器接口
 * <p>
 *
 * @param <T> 异常类型
 */
@FunctionalInterface
public interface ExceptionHandler<T extends Throwable> {

    /**
     * 处理异常
     *
     * @param exception 异常
     * @param context   上下文信息
     * @return 处理后的异常
     */
    BaseException handle(T exception, Map<String, Object> context);
}