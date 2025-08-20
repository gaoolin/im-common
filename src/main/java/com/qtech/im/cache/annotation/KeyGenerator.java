package com.qtech.im.cache.annotation;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:58:29
 * desc   :  im-common-IntelliJ IDEA
 */

import java.lang.reflect.Method;

/**
 * 缓存键生成器接口
 * <p>
 * 用于生成缓存键
 */
public interface KeyGenerator {

    /**
     * 生成缓存键
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param params 方法参数
     * @return 缓存键
     */
    Object generate(Object target, Method method, Object... params);
}
