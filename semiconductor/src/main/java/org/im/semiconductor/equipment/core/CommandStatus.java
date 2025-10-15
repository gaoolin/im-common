package org.im.semiconductor.equipment.core;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public enum CommandStatus {
    PENDING,      // 待执行
    EXECUTING,    // 执行中
    SUCCESS,      // 执行成功
    FAILED,       // 执行失败
    TIMEOUT,      // 超时
    CANCELLED     // 已取消
}