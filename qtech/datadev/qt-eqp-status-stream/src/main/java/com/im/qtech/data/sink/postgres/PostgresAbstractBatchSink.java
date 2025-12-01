package com.im.qtech.data.sink.postgres;

import com.im.qtech.data.common.AbstractBatchSink;
import com.im.qtech.data.common.binder.EqNetworkStatusParameterBinder;
import com.im.qtech.data.model.EqNetworkStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.im.qtech.data.util.Constants.*;

/**
 * PostgresSQL 批量 Sink
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 11:11:57
 */
public final class PostgresAbstractBatchSink extends AbstractBatchSink<EqNetworkStatus> implements EqNetworkStatusParameterBinder {

    public PostgresAbstractBatchSink() {
        super(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DRIVER_CLASS, 500,       // batchSize
                5);
    }

    @Override
    protected String getSql() {
        return POSTGRES_SQL;
    }

    // 添加自定义序列化方法确保兼容性
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // 重新初始化 transient 字段
    }

    @Override
    public void setPreparedStatementParams(PreparedStatement ps, EqNetworkStatus record) throws SQLException {
        EqNetworkStatusParameterBinder.super.bindParameters(ps, record);
    }
}
