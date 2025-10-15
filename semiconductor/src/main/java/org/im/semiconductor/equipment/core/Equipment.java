package org.im.semiconductor.equipment.core;

/**
 * 设备基本信息属性
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/25
 */

public interface Equipment {
    String getId();              // 设备唯一标识

    String getName();            // 设备名称

    String getModel();           // 设备型号

    String getManufacturer();    // 制造商

    String getSerialNumber();    // 序列号

    EquipmentType getType();     // 设备类型
}