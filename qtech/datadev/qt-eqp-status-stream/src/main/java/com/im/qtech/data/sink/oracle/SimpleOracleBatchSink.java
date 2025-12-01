package com.im.qtech.data.sink.oracle;

import com.im.qtech.data.common.BaseJdbcSimpleSink;
import com.im.qtech.data.common.binder.EqNetworkStatusParameterBinder;
import com.im.qtech.data.model.EqNetworkStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.im.qtech.data.util.Constants.*;


/**
 * 批量写入Oracle
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/04 11:04:23
 */
public final class SimpleOracleBatchSink extends BaseJdbcSimpleSink<EqNetworkStatus> implements EqNetworkStatusParameterBinder {

    public SimpleOracleBatchSink() {
        super("oracle-ds", ORACLE_URL, ORACLE_USER, ORACLE_PASSWORD, ORACLE_DRIVER_CLASS);
    }

    @Override
    protected String getSql() {
        return ORACLE_SQL;
    }

    @Override
    public void setPreparedStatementParams(PreparedStatement ps, EqNetworkStatus record) throws SQLException {
        EqNetworkStatusParameterBinder.super.bindParameters(ps, record);
    }
}
