package com.im.qtech.data.common.binder;

import com.im.qtech.data.model.EqNetworkStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于将 EqNetworkStatus 对象属性绑定到 PreparedStatement 参数的实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/05 09:54:35
 */
public interface EqNetworkStatusParameterBinder extends com.qtech.status.common.binder.SqlParameterBinder<EqNetworkStatus> {

    @Override
    default void bindParameters(PreparedStatement ps, EqNetworkStatus record) throws SQLException {
        if (!isValid(record)) {
            throw new SQLException("Invalid EqNetworkStatus record: " + record);
        }

        ps.setString(1, record.getDeviceId());
        if (record.getReceiveDate() != null && !record.getReceiveDate().isEmpty()) {
            java.sql.Timestamp timestamp = parseTimestamp(record.getReceiveDate());
            ps.setTimestamp(2, timestamp);
        } else {
            ps.setNull(2, java.sql.Types.TIMESTAMP);
        }
        ps.setString(3, record.getDeviceType());
        ps.setString(4, record.getLotName());
        if (record.getStatus() != null) {
            try {
                ps.setShort(5, Short.parseShort(record.getStatus()));
            } catch (NumberFormatException e) {
                throw new SQLException("Invalid status value: " + record.getStatus(), e);
            }
        } else {
            ps.setNull(5, java.sql.Types.SMALLINT);
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(record.getRemoteControl())) {
            ps.setString(6, record.getRemoteControl());
        } else {
            ps.setNull(6, java.sql.Types.VARCHAR);
        }
        if (record.getLastUpdated() != null) {
            ps.setTimestamp(7, new java.sql.Timestamp(record.getLastUpdated().getTime()));
        } else {
            ps.setNull(7, java.sql.Types.TIMESTAMP);
        }
    }

    @Override
    default boolean isValid(EqNetworkStatus record) {
        // 验证记录是否有效
        return record != null && record.getDeviceId() != null;
    }
}