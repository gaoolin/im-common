package org.im.cache.core;

import java.lang.reflect.Method;

/**
 * 缓存键生成器接口
 * <p>
 * 用于生成缓存键
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19
 */

public interface CacheKeyGenerator {

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