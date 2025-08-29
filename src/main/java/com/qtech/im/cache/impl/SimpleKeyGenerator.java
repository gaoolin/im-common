package com.qtech.im.cache.impl;

import com.qtech.im.cache.CacheKeyGenerator;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 简单键生成器实现
 * <p>
 * 基于类名、方法名和参数生成缓存键
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */

public class SimpleKeyGenerator implements CacheKeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return new SimpleKey(target.getClass().getName(), method.getName(), params);
    }

    /**
     * 简单键实现
     */
    public static class SimpleKey {
        private final Object[] elements;

        public SimpleKey(Object... elements) {
            this.elements = elements;
        }

        @Override
        public boolean equals(Object other) {
            return (this == other ||
                    (other instanceof SimpleKey && Arrays.deepEquals(elements, ((SimpleKey) other).elements)));
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(elements);
        }

        @Override
        public String toString() {
            return "SimpleKey [" + Arrays.deepToString(elements) + "]";
        }
    }
}