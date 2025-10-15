package org.im.semiconductor.equipment.core;

/**
 * 设备状态接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface EquipmentState {
    EquipmentState getState();     // 当前状态

    boolean isAvailable();         // 是否可用

    boolean isConnected();         // 是否连接

    long getLastUpdateTime();      // 最后更新时间

    enum StateValue {
        UNKNOWN, IDLE, RUNNING, PAUSED, ERROR, MAINTENANCE, OFFLINE
    }
}