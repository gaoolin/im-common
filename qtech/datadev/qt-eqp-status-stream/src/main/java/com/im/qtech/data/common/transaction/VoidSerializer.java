package com.im.qtech.data.common.transaction;

import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.base.TypeSerializerSingleton;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * VoidSerializer
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 09:56:14
 */
public final class VoidSerializer extends TypeSerializerSingleton<Void> {

    public static final VoidSerializer INSTANCE = new VoidSerializer();

    private VoidSerializer() {
        // 私有构造函数，防止外部实例化
    }

    @Override
    public boolean isImmutableType() {
        return true;
    }

    @Override
    @Nullable
    public Void createInstance() {
        return null;
    }

    @Override
    @Nullable
    public Void copy(Void from) {
        return copyInstance();
    }

    @Override
    @Nullable
    public Void copy(Void from, Void reuse) {
        return copyInstance();
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public void serialize(Void record, DataOutputView target) throws IOException {
        // Void 类型无需序列化任何数据
    }

    @Override
    @Nullable
    public Void deserialize(DataInputView source) throws IOException {
        return null;
    }

    @Override
    @Nullable
    public Void deserialize(Void reuse, DataInputView source) throws IOException {
        return null;
    }

    @Override
    public void copy(DataInputView source, DataOutputView target) throws IOException {
        // Void 类型无需复制任何数据
    }

    @Override
    public SimpleTypeSerializerSnapshot<Void> snapshotConfiguration() {
        return new VoidSerializerSnapshot();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidSerializer;
    }

    @Override
    public int hashCode() {
        return VoidSerializer.class.hashCode();
    }

    // 统一处理 copy 操作
    @Nullable
    private Void copyInstance() {
        return null;
    }
}