package com.qtech.im.semiconductor.equipment.parameter.mgr;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:25:54
 */

/**
 * 参数类型枚举
 */
public enum ParameterType {
    /**
     * 整数类型
     */
    INTEGER,

    /**
     * 浮点数类型
     */
    FLOAT,

    /**
     * 字符串类型
     */
    STRING,

    /**
     * 布尔类型
     */
    BOOLEAN,

    /**
     * 枚举类型
     */
    ENUM,

    /**
     * 数组类型
     */
    ARRAY,

    /**
     * 对象类型
     */
    OBJECT,

    /**
     * 时间类型
     */
    TIMESTAMP,

    /**
     * 二进制类型
     */
    BINARY,

    /**
     * 自定义类型
     */
    CUSTOM
}
