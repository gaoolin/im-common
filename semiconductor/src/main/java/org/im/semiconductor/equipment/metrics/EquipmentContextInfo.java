package org.im.semiconductor.equipment.metrics;

import org.im.semiconductor.common.context.equipment.EquipmentContext;
import org.im.semiconductor.common.context.location.LocationInfo;

/**
 * 与设备模块组合
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

// 设备上下文信息
public interface EquipmentContextInfo extends LocationInfo, EquipmentContext {
    // 组合地理位置和设备信息，用于设备定位和管理
}
