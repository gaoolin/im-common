package com.qtech.im.semiconductor.equipment.adapter;

/**
 * 设备通信适配器工具类
 *
 * 解决问题:
 * - 不同厂商设备通信协议差异大
 * - 设备通信集成复杂
 * - 设备命令标准化困难
 * - 设备响应处理不统一
 */
public class EquipmentAdapter {
    // 多协议设备通信
    public static EquipmentConnection createConnection(String equipmentId, ProtocolType protocol);

    // 标准化设备命令
    public static EquipmentResponse sendStandardCommand(String equipmentId, StandardCommand command);

    // 设备状态轮询
    public static PollingResult pollEquipmentStatus(String equipmentId, PollingConfig config);

    // 设备事件监听
    public static void registerEventListener(String equipmentId, EquipmentEventListener listener);
}
