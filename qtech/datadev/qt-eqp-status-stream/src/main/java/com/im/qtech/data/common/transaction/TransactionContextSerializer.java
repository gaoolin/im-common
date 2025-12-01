package com.im.qtech.data.common.transaction;

import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSchemaCompatibility;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

/**
 * TransactionContext序列化器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 11:01:15
 */
public class TransactionContextSerializer extends TypeSerializer<TransactionContext> {

    public static final TransactionContextSerializer INSTANCE = new TransactionContextSerializer();

    @Override
    public boolean isImmutableType() {
        return true;
    }

    /**
     * 由于本序列化器是无状态的，因此可以直接返回自身实例。
     */
    @Override
    public TypeSerializer<TransactionContext> duplicate() {
        return this;
    }

    @Override
    public TransactionContext createInstance() {
        return new TransactionContext();
    }

    // 确保copy方法正确复制数据
    @Override
    public TransactionContext copy(TransactionContext from) {
        if (from == null) {
            return null;
        }
        TransactionContext copy = new TransactionContext();
        copy.setTransactionId(from.getTxnId());
        // 注意：connection是transient的，不会被复制
        return copy;
    }

    @Override
    public TransactionContext copy(TransactionContext from, TransactionContext reuse) {
        if (from == null) {
            return null;
        }
        if (reuse == null) {
            return copy(from);
        }
        reuse.setTransactionId(from.getTxnId());
        return reuse;
    }

    @Override
    public int getLength() {
        return Long.BYTES;
    }

    @Override
    public void serialize(TransactionContext record, DataOutputView target) throws IOException {
        if (record == null) {
            throw new IOException("Cannot serialize null record.");
        }
        target.writeLong(record.getTxnId());
    }

    // 确保deserialize方法正确创建实例
    @Override
    public TransactionContext deserialize(DataInputView source) throws IOException {
        long id = source.readLong();
        TransactionContext context = new TransactionContext();
        context.setTransactionId(id);
        return context;
    }

    @Override
    public TransactionContext deserialize(TransactionContext reuse, DataInputView source) throws IOException {
        return deserialize(source);
    }

    @Override
    public void copy(DataInputView source, DataOutputView target) throws IOException {
        target.writeLong(source.readLong());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransactionContextSerializer;
    }

    @Override
    public int hashCode() {
        return TransactionContextSerializer.class.hashCode();
    }

    @Override
    public TypeSerializerSnapshot<TransactionContext> snapshotConfiguration() {
        return new TransactionContextSerializerSnapshot();
    }

    // ---- 关键：自定义 Snapshot ----
    public static final class TransactionContextSerializerSnapshot implements TypeSerializerSnapshot<TransactionContext> {

        @Override
        public int getCurrentVersion() {
            return 1;
        }

        @Override
        public void writeSnapshot(DataOutputView out) throws IOException {
            // 无额外状态
        }

        @Override
        public void readSnapshot(int readVersion, DataInputView in, ClassLoader cl) throws IOException {
            // 无额外状态
        }

        @Override
        public TypeSerializer<TransactionContext> restoreSerializer() {
            return TransactionContextSerializer.INSTANCE;
        }

        @Override
        public TypeSerializerSchemaCompatibility<TransactionContext> resolveSchemaCompatibility(TypeSerializer<TransactionContext> newSerializer) {
            return TypeSerializerSchemaCompatibility.compatibleAsIs();
        }
    }
}
