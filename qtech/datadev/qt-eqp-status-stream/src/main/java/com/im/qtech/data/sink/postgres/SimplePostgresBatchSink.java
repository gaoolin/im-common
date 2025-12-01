package com.im.qtech.data.sink.postgres;

import com.im.qtech.data.common.BaseJdbcSimpleSink;
import com.im.qtech.data.common.binder.EqNetworkStatusParameterBinder;
import com.im.qtech.data.model.EqNetworkStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.im.qtech.data.util.Constants.*;

/**
 * Postgres批量写入
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/04 11:23:46
 */
public final class SimplePostgresBatchSink extends BaseJdbcSimpleSink<EqNetworkStatus> implements EqNetworkStatusParameterBinder {
    public SimplePostgresBatchSink() {
        super("postgres-ds", POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DRIVER_CLASS);
    }

    @Override
    protected String getSql() {
        return POSTGRES_SQL;
    }

    @Override
    public void setPreparedStatementParams(PreparedStatement ps, EqNetworkStatus record) throws SQLException {
        EqNetworkStatusParameterBinder.super.bindParameters(ps, record);
    }
}