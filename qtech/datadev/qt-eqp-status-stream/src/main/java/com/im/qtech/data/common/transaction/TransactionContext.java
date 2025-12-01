package com.im.qtech.data.common.transaction;

import lombok.Getter;

import java.sql.Connection;
import java.util.Objects;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 11:00:57
 */
public class TransactionContext {

    @Getter
    private transient Connection connection; // 不参与序列化
    private volatile long transactionId; // 提高多线程可见性

    public TransactionContext() {
        // 初始化默认值
        this.transactionId = 0L;
    }

    public TransactionContext(Connection connection, long transactionId) {
        setConnection(connection);         // 使用 setter 进行防御性检查
        setTransactionId(transactionId);   // 使用 setter 进行防御性检查
    }

    public void setConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }

    public long getTxnId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionContext)) return false;
        TransactionContext that = (TransactionContext) o;
        return transactionId == that.transactionId &&
                Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection, transactionId);
    }
}
