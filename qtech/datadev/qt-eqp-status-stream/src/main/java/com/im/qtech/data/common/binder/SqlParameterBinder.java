package com.qtech.status.common.binder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 用于将特定类型对象的属性绑定到 PreparedStatement 参数的通用接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/05 09:53:04
 */
public interface SqlParameterBinder<T> {

    /**
     * 将对象的属性绑定到 PreparedStatement 参数
     *
     * @param ps     PreparedStatement 对象
     * @param record 要绑定的记录对象
     * @throws SQLException 当数据库操作出现错误时抛出
     */
    default void bindParameters(PreparedStatement ps, T record) throws SQLException {
        // 子类需要实现具体的绑定逻辑
        throw new UnsupportedOperationException("子类必须实现 bindParameters 方法");
    }

    /**
     * 解析日期字符串为 Timestamp 对象
     *
     * @param dateStr 日期字符串
     * @return 解析后的 Timestamp 对象
     * @throws IllegalArgumentException 当日期字符串无法解析时抛出
     */
    default java.sql.Timestamp parseTimestamp(String dateStr) {
        // 支持多种日期格式
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};

        for (String pattern : patterns) {
            try {
                java.util.Date utilDate = new java.text.SimpleDateFormat(pattern).parse(dateStr);
                return new java.sql.Timestamp(utilDate.getTime());
            } catch (java.text.ParseException e) {
                // 继续尝试下一个格式
            }
        }

        // 如果所有格式都失败，抛出异常
        throw new IllegalArgumentException("无法解析日期字符串: " + dateStr);
    }

    /**
     * 验证记录是否有效
     *
     * @param record 要验证的记录对象
     * @return true 如果记录有效，否则 false
     */
    default boolean isValid(T record) {
        // 默认实现，子类可以根据需要重写
        return record != null;
    }
}
