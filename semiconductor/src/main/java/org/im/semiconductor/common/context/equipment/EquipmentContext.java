package org.im.semiconductor.common.context.equipment;

/**
 * 设备相关信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface EquipmentContext {
    String getDeviceId();    // 设备ID

    String getDeviceName();  // 设备名称

    String getEqpType();     // 设备类型

    String getLine();        // 生产线

    String getStep();        // 工艺步骤

    String getRecipe();      // 配方名称

    String getParamGroup();  // 参数群组
}
