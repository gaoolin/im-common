package org.im.semiconductor.testing.metrics;

import org.im.semiconductor.common.context.equipment.DeviceInfo;
import org.im.semiconductor.common.context.test.TestInfo;

/**
 * 与测试模块组合
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/28
 */

// 测试分析上下文
public interface TestingCtx extends DeviceInfo, TestInfo {
}
