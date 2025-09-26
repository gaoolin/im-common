package org.im.semiconductor.equipment.core;

import org.im.exception.type.eqp.EquipmentException;

/**
 * 设备命令接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */
public interface EquipmentCommand {
    <T> T executeCommand(Command<T> command) throws EquipmentException;

    interface Command<T> extends org.im.semiconductor.equipment.core.Command<T> {
        T execute(Equipment equipment) throws EquipmentException;
    }
}