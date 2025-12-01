package com.im.qtech.data.common.transaction;

import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;

/**
 * Void类型序列化器快照
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 10:02:25
 */
public final class VoidSerializerSnapshot extends SimpleTypeSerializerSnapshot<Void> {

    public VoidSerializerSnapshot() {
        super(() -> VoidSerializer.INSTANCE);
    }
}