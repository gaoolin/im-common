package org.im.semiconductor.quality.metrics;

import org.im.semiconductor.common.context.product.ProductInfo;
import org.im.semiconductor.common.context.production.LotInfo;
import org.im.semiconductor.common.context.test.TestInfo;

/**
 * 与品质模块组合
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

// 品质分析上下文
public interface QualityContextInfo extends ProductInfo, LotInfo, TestInfo {
    // 组合产品、批次和测试信息，用于品质追溯和分析
}