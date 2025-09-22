package org.im.orm.mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体元数据类
 * 用于存储实体类的映射信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public class EntityMetadata {
    private Class<?> entityClass;
    private String tableName;
    private Field idField;
    private Map<String, Field> columnFields;
    private Map<String, String> fieldColumnMapping;
    private List<AssociationMetadata> associations;

    /**
     * 构造函数
     */
    public EntityMetadata() {
        this.columnFields = new HashMap<>();
        this.fieldColumnMapping = new HashMap<>();
        this.associations = new ArrayList<>();
    }

    /**
     * 获取实体类
     *
     * @return 实体类
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * 设置实体类
     *
     * @param entityClass 实体类
     */
    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名
     *
     * @param tableName 表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取主键字段
     *
     * @return 主键字段
     */
    public Field getIdField() {
        return idField;
    }

    /**
     * 设置主键字段
     *
     * @param idField 主键字段
     */
    public void setIdField(Field idField) {
        this.idField = idField;
    }

    /**
     * 获取列字段映射
     *
     * @return 列字段映射
     */
    public Map<String, Field> getColumnFields() {
        return columnFields;
    }

    /**
     * 设置列字段映射
     *
     * @param columnFields 列字段映射
     */
    public void setColumnFields(Map<String, Field> columnFields) {
        this.columnFields = columnFields;
    }

    /**
     * 获取字段到列的映射
     *
     * @return 字段到列的映射
     */
    public Map<String, String> getFieldColumnMapping() {
        return fieldColumnMapping;
    }

    /**
     * 设置字段到列的映射
     *
     * @param fieldColumnMapping 字段到列的映射
     */
    public void setFieldColumnMapping(Map<String, String> fieldColumnMapping) {
        this.fieldColumnMapping = fieldColumnMapping;
    }

    /**
     * 获取关联元数据列表
     *
     * @return 关联元数据列表
     */
    public List<AssociationMetadata> getAssociations() {
        return associations;
    }

    /**
     * 设置关联元数据列表
     *
     * @param associations 关联元数据列表
     */
    public void setAssociations(List<AssociationMetadata> associations) {
        this.associations = associations;
    }
}