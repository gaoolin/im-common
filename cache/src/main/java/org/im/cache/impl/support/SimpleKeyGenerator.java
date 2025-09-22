package org.im.cache.impl.support;

import org.im.cache.core.CacheKeyGenerator;

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

    private static final String KEY_PREFIX = "#p";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length == 0) {
            return method.getName();
        }
        if (params.length == 1 && params[0] != null) {
            return params[0];
        }
        return Arrays.hashCode(params);
    }

    public Object generateKey(String keyExpression, Object target, Method method, Object... params) {
        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            return generate(target, method, params);
        }
        // 简单解析 #p0, #p1 等形式的表达式
        if (keyExpression.startsWith(KEY_PREFIX)) {
            try {
                int paramIndex = Integer.parseInt(keyExpression.substring(KEY_PREFIX.length()));
                if (paramIndex >= 0 && paramIndex < params.length && params[paramIndex] != null) {
                    return params[paramIndex];
                }
            } catch (NumberFormatException e) {
                // 无效索引，回退到默认
            }
        }
        // 直接使用 keyExpression 作为键
        return keyExpression;
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