package org.im.semiconductor.common.context.production;

/**
 * 生产批次信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface LotInfo {
    String getWorkOrder();   // 工单号码

    String getLot();         // 批次号码

    String getSubLot();      // 子批次号码
}
