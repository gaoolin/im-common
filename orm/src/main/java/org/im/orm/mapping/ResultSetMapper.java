package org.im.orm.mapping;

import org.im.exception.type.orm.ORMException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 结果集映射器
 * 用于将查询结果映射到实体对象
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class ResultSetMapper {

    /**
     * 将结果集映射为实体对象
     *
     * @param resultSet 结果集
     * @param metadata  实体元数据
     * @param <T>       实体类型
     * @return 实体对象
     */
    public static <T> T mapResultSetToEntity(ResultSet resultSet, EntityMetadata metadata) {
        try {
            // 创建实体实例
            @SuppressWarnings("unchecked")
            T entity = (T) metadata.getEntityClass().newInstance();

            // 遍历字段并设置值（仅处理非关联字段）
            for (String columnName : metadata.getColumnFields().keySet()) {
                // 检查字段是否为关联字段
                if (isAssociationColumn(metadata, columnName)) {
                    continue;
                }

                Field field = metadata.getColumnFields().get(columnName);
                Object value = getColumnValue(resultSet, columnName, field.getType());
                field.set(entity, value);
            }

            return entity;
        } catch (Exception e) {
            throw new ORMException("映射结果集到实体对象时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 从结果集中获取列值
     *
     * @param resultSet  结果集
     * @param columnName 列名
     * @param fieldType  字段类型
     * @return 列值
     * @throws SQLException SQL异常
     */
    private static Object getColumnValue(ResultSet resultSet, String columnName, Class<?> fieldType) throws SQLException {
        if (fieldType == String.class) {
            return resultSet.getString(columnName);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return resultSet.getInt(columnName);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return resultSet.getLong(columnName);
        } else if (fieldType == Double.class || fieldType == double.class) {
            return resultSet.getDouble(columnName);
        } else if (fieldType == Float.class || fieldType == float.class) {
            return resultSet.getFloat(columnName);
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return resultSet.getBoolean(columnName);
        } else if (fieldType == java.util.Date.class) {
            return resultSet.getDate(columnName);
        } else if (fieldType == LocalDateTime.class) {
            // 处理LocalDateTime类型
            java.sql.Timestamp timestamp = resultSet.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } else {
            // 默认情况，尝试以Object类型获取
            return resultSet.getObject(columnName);
        }
    }

    /**
     * 判断列是否为关联字段
     *
     * @param metadata   实体元数据
     * @param columnName 列名
     * @return 是否为关联字段
     */
    private static boolean isAssociationColumn(EntityMetadata metadata, String columnName) {
        return metadata.getAssociations().stream()
                .anyMatch(assoc -> {
                    String fieldName = assoc.getField().getName();
                    String assocColumnName = metadata.getFieldColumnMapping().get(fieldName);
                    return columnName.equals(assocColumnName);
                });
    }
}