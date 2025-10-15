package org.im.orm.mapping;

import java.lang.reflect.Field;

/**
 * 关联元数据类
 * 用于存储实体类间的关联关系信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class AssociationMetadata {
    private Field field;
    private AssociationType type;
    private Class<?> targetEntity;
    private String foreignKey;
    private boolean lazy;
    private String mappedBy;

    /**
     * 构造函数
     *
     * @param field        关联字段
     * @param type         关联类型
     * @param targetEntity 目标实体类
     * @param foreignKey   外键字段名
     * @param lazy         是否延迟加载
     * @param mappedBy     关联字段名
     */
    public AssociationMetadata(Field field, AssociationType type, Class<?> targetEntity,
                               String foreignKey, boolean lazy, String mappedBy) {
        this.field = field;
        this.type = type;
        this.targetEntity = targetEntity;
        this.foreignKey = foreignKey;
        this.lazy = lazy;
        this.mappedBy = mappedBy;
    }

    /**
     * 获取关联字段
     *
     * @return 关联字段
     */
    public Field getField() {
        return field;
    }

    /**
     * 设置关联字段
     *
     * @param field 关联字段
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * 获取关联类型
     *
     * @return 关联类型
     */
    public AssociationType getType() {
        return type;
    }

    /**
     * 设置关联类型
     *
     * @param type 关联类型
     */
    public void setType(AssociationType type) {
        this.type = type;
    }

    /**
     * 获取目标实体类
     *
     * @return 目标实体类
     */
    public Class<?> getTargetEntity() {
        return targetEntity;
    }

    /**
     * 设置目标实体类
     *
     * @param targetEntity 目标实体类
     */
    public void setTargetEntity(Class<?> targetEntity) {
        this.targetEntity = targetEntity;
    }

    /**
     * 获取外键字段名
     *
     * @return 外键字段名
     */
    public String getForeignKey() {
        return foreignKey;
    }

    /**
     * 设置外键字段名
     *
     * @param foreignKey 外键字段名
     */
    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    /**
     * 是否延迟加载
     *
     * @return 是否延迟加载
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * 设置是否延迟加载
     *
     * @param lazy 是否延迟加载
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * 获取关联字段名
     *
     * @return 关联字段名
     */
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * 设置关联字段名
     *
     * @param mappedBy 关联字段名
     */
    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    /**
     * 关联类型枚举
     */
    public enum AssociationType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }
}