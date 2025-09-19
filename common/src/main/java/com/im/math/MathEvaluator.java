package com.im.math;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/03/28 09:27:22
 * desc   :  数学评估器，适合各种数学计算
 * <p>
 * 如果需要支持更广泛的数字格式（如科学计数法），并且对性能要求不高，第一种实现更为合理。
 * 如果只需要判断简单的整数或小数，并且对性能有一定要求，第二种实现更为合适。
 */
public final class MathEvaluator {

    /**
     * 判断一个对象的值是否是数值类型的字符串
     */
    public static boolean isNumeric(Object value) {
        if (value == null) {
            return false;
        }
        String str = value.toString().trim();
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * @param value
     * @return boolean
     * @description 扩展正则表达式以支持科学计数法等复杂格式
     * 该正则表达式支持以下格式：
     * 简单整数（如 -123）
     * 小数（如 123.45）
     * 科学计数法（如 1.23e-10 或 1.23E+10）
     * 这样可以在保持较高性能的同时，扩展对复杂数字格式的支持。
     */
    public static boolean isExtendedNumeric(Object value) {
        if (value == null) {
            return false;
        }
        String str = value.toString().trim();
        return str.matches("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
    }

    /**
     * 安全地解析字符串为双精度浮点数
     */
    public static double parseDoubleSafely(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric string: " + str, e);
        }
    }

    /**
     * 判断字符串是否为合法数字
     */
    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
