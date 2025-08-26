package com.qtech.im.semiconductor.equipment.parameter.mgr;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:27:15
 */

/**
 * 参数状态枚举
 */
public enum ParameterStatus {
    /**
     * 正常状态
     */
    NORMAL,

    /**
     * 警告状态
     */
    WARNING,

    /**
     * 错误状态
     */
    ERROR,

    /**
     * 维护状态
     */
    MAINTENANCE,

    /**
     * 离线状态
     */
    OFFLINE,

    /**
     * 未知状态
     */
    UNKNOWN
}
