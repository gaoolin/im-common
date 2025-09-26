package org.im.semiconductor.equipment.core;

import org.im.exception.type.eqp.EquipmentException;

/**
 * 设备连接接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface EquipmentConnection {
    void connect() throws EquipmentException;

    void disconnect() throws EquipmentException;

    boolean ping() throws EquipmentException;

    String getConnectionString();
}