package org.im.common.thread.policy;

/**
 * 智能拒绝策略 - 针对IoT场景优化
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public enum SmartRejectionPolicy {
    ABORT,              // 抛异常
    CALLER_RUNS,        // 调用者运行
    DISCARD,            // 直接丢弃
    DISCARD_OLDEST,     // 丢弃最旧任务
    BLOCKING,           // 阻塞等待
    RETRY_THEN_ABORT,   // 重试后抛异常
    IOT_CRITICAL,       // IoT关键任务策略
    IOT_BATCH           // IoT批处理策略
}