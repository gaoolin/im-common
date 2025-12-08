package org.im.cache.util;

/**
 * 验证工具类
 * <p>
 * 提供参数验证相关的通用工具方法
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/08
 */
public class ValidationUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private ValidationUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 检查对象是否为null
     *
     * @param obj     要检查的对象
     * @param message 异常消息
     * @throws IllegalArgumentException 如果对象为null
     */
    public static void checkNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查字符串是否为空
     *
     * @param str     要检查的字符串
     * @param message 异常消息
     * @throws IllegalArgumentException 如果字符串为空
     */
    public static void checkNotEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查数值是否为正数
     *
     * @param value   要检查的数值
     * @param message 异常消息
     * @throws IllegalArgumentException 如果数值不是正数
     */
    public static void checkPositive(long value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查数值是否为非负数
     *
     * @param value   要检查的数值
     * @param message 异常消息
     * @throws IllegalArgumentException 如果数值为负数
     */
    public static void checkNonNegative(long value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}

