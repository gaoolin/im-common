package org.im.semiconductor.equipment.core;

import org.im.exception.type.eqp.EquipmentException;

/**
 * 设备生命周期接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface EquipmentLifecycle {
    void initialize() throws EquipmentException;

    void start() throws EquipmentException;

    void pause() throws EquipmentException;

    void resume() throws EquipmentException;

    void stop() throws EquipmentException;

    void shutdown() throws EquipmentException;
}
