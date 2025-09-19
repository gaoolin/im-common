package com.im.security;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:27:47
 */

/**
 * 访问权限枚举
 */
public enum AccessLevel {
    /**
     * 读写权限
     */
    READ_WRITE,

    /**
     * 只读权限
     */
    READ_ONLY,

    /**
     * 写入权限
     */
    WRITE_ONLY,

    /**
     * 无权限
     */
    NONE
}
