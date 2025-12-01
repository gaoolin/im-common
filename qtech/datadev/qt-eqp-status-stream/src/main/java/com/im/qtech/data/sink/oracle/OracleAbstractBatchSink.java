package com.im.qtech.data.sink.oracle;

import com.im.qtech.data.common.AbstractBatchSink;
import com.im.qtech.data.common.binder.EqNetworkStatusParameterBinder;
import com.im.qtech.data.model.EqNetworkStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.im.qtech.data.util.Constants.*;

/**
 * Oracle 批量 Sink
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/01 11:18:02
 */
public final class OracleAbstractBatchSink extends AbstractBatchSink<EqNetworkStatus> implements EqNetworkStatusParameterBinder {
    public OracleAbstractBatchSink() {
        super(ORACLE_URL, ORACLE_USER, ORACLE_PASSWORD, ORACLE_DRIVER_CLASS, ORACLE_BATCH_SIZE, ORACLE_MAX_RETRIES);
    }

    @Override
    protected String getSql() {
        return ORACLE_SQL;
    }

    // 添加自定义序列化方法以确保兼容性
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    @Override
    public void setPreparedStatementParams(PreparedStatement ps, EqNetworkStatus record) throws SQLException {
        EqNetworkStatusParameterBinder.super.bindParameters(ps, record);
    }
}
