package org.im.semiconductor.equipment.core;

/**
 * 完整设备接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface FullEquipment extends
        Equipment,
        EquipmentState,
        EquipmentConnection,
        EquipmentCommand,
        EquipmentLifecycle,
        EquipmentConfigurable {
    // 继承所有设备相关功能
}