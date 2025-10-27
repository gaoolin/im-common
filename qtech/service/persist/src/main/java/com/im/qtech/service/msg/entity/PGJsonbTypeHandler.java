package com.im.qtech.service.msg.entity;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class PGJsonbTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(parameter);
            ps.setObject(i, jsonObject);
        } catch (Exception e) {
            throw new SQLException("Failed to set JSONB parameter", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        if (obj instanceof PGobject) {
            return ((PGobject) obj).getValue();
        }
        return obj != null ? obj.toString() : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        PGobject pgObject = (PGobject) rs.getObject(columnIndex);
        return pgObject != null ? pgObject.getValue() : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        PGobject pgObject = (PGobject) cs.getObject(columnIndex);
        return pgObject != null ? pgObject.getValue() : null;
    }
}