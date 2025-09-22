package org.im.orm.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL生成器
 * 用于根据实体元数据生成SQL语句
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class SQLGenerator {

    /**
     * 生成插入SQL语句
     *
     * @param metadata 实体元数据
     * @return 插入SQL语句
     */
    public static String generateInsertSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(metadata.getTableName());

        List<String> columnNames = new ArrayList<>();

        // 只添加非自动生成主键的字段和非关联字段
        for (Map.Entry<String, String> entry : metadata.getFieldColumnMapping().entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();

            // 检查是否是主键字段
            if (metadata.getIdField().getName().equals(fieldName)) {
                // 检查主键是否是自动生成的
                Id idAnnotation = metadata.getIdField().getAnnotation(Id.class);
                if (idAnnotation != null && idAnnotation.autoGenerate()) {
                    // 跳过自动生成的主键字段
                    continue;
                }
            }

            // 检查是否是关联字段
            if (isAssociationField(metadata, fieldName)) {
                // 跳过关联字段
                continue;
            }

            columnNames.add(columnName);
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(columnNames.get(i));
            placeholders.append("?");
        }

        sql.append(" (").append(columns).append(") VALUES (").append(placeholders).append(")");
        return sql.toString();
    }

    /**
     * 生成根据ID查询SQL语句
     *
     * @param metadata 实体元数据
     * @return 查询SQL语句
     */
    public static String generateSelectByIdSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // 添加所有非关联字段
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        int i = 0;
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();

            // 跳过关联字段
            if (isAssociationField(metadata, fieldName)) {
                continue;
            }

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(entry.getValue());
            i++;
        }

        sql.append(" FROM ").append(metadata.getTableName());
        sql.append(" WHERE ").append(getIdColumnName(metadata)).append(" = ?");
        return sql.toString();
    }

    /**
     * 生成更新SQL语句
     *
     * @param metadata 实体元数据
     * @return 更新SQL语句
     */
    public static String generateUpdateSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(metadata.getTableName()).append(" SET ");

        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        int i = 0;
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();

            // 跳过主键字段和关联字段
            if (columnName.equals(getIdColumnName(metadata)) || isAssociationField(metadata, fieldName)) {
                continue;
            }

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columnName).append(" = ?");
            i++;
        }

        sql.append(" WHERE ").append(getIdColumnName(metadata)).append(" = ?");
        return sql.toString();
    }

    /**
     * 生成删除SQL语句
     *
     * @param metadata 实体元数据
     * @return 删除SQL语句
     */
    public static String generateDeleteSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(metadata.getTableName());
        sql.append(" WHERE ").append(getIdColumnName(metadata)).append(" = ?");
        return sql.toString();
    }

    /**
     * 生成查询所有记录SQL语句
     *
     * @param metadata 实体元数据
     * @return 查询SQL语句
     */
    public static String generateSelectAllSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // 添加所有非关联字段
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        int i = 0;
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();

            // 跳过关联字段
            if (isAssociationField(metadata, fieldName)) {
                continue;
            }

            if (i > 0) {
                sql.append(", ");
            }
            sql.append(entry.getValue());
            i++;
        }

        sql.append(" FROM ").append(metadata.getTableName());
        return sql.toString();
    }

    /**
     * 获取主键列名
     *
     * @param metadata 实体元数据
     * @return 主键列名
     */
    private static String getIdColumnName(EntityMetadata metadata) {
        String idFieldName = metadata.getIdField().getName();
        return metadata.getFieldColumnMapping().get(idFieldName);
    }

    /**
     * 判断字段是否为关联字段
     *
     * @param metadata  实体元数据
     * @param fieldName 字段名
     * @return 是否为关联字段
     */
    private static boolean isAssociationField(EntityMetadata metadata, String fieldName) {
        return metadata.getAssociations().stream()
                .anyMatch(assoc -> assoc.getField().getName().equals(fieldName));
    }
}