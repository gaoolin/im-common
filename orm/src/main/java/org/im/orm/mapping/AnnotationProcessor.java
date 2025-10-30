package org.im.orm.mapping;

import org.im.common.exception.type.orm.ORMException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解处理器
 * 用于解析实体类的注解并生成元数据
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */
public class AnnotationProcessor {

    /**
     * 处理实体类，生成元数据
     *
     * @param entityClass 实体类
     * @return 实体元数据
     */
    public static EntityMetadata processEntity(Class<?> entityClass) {
        // 检查是否标记了@Entity注解
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new ORMException("实体类必须使用@Entity注解标记: " + entityClass.getName());
        }

        EntityMetadata metadata = new EntityMetadata();
        metadata.setEntityClass(entityClass);

        // 处理@Entity注解
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.table();
        if (tableName == null || tableName.isEmpty()) {
            // 如果没有指定表名，使用类名作为表名
            tableName = camelToUnderscore(entityClass.getSimpleName());
        }
        metadata.setTableName(tableName);

        // 处理字段
        Field[] fields = entityClass.getDeclaredFields();
        List<AssociationMetadata> associations = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);

            // 处理@Id注解
            if (field.isAnnotationPresent(Id.class)) {
                metadata.setIdField(field);
            }

            // 处理关联注解
            if (entityAnnotation.enableAssociations()) {
                AssociationMetadata association = processAssociation(field);
                if (association != null) {
                    associations.add(association);
                }
            }

            // 处理@Column注解
            String columnName = null;
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                columnName = columnAnnotation.name();
                if (columnName == null || columnName.isEmpty()) {
                    // 如果没有指定列名，使用字段名作为列名
                    columnName = camelToUnderscore(field.getName());
                }
            } else {
                // 没有@Column注解的字段，使用字段名作为列名
                columnName = camelToUnderscore(field.getName());
            }

            metadata.getColumnFields().put(columnName, field);
            metadata.getFieldColumnMapping().put(field.getName(), columnName);
        }

        // 设置关联元数据
        metadata.setAssociations(associations);

        // 检查是否指定了主键字段
        if (metadata.getIdField() == null) {
            throw new ORMException("实体类必须包含一个使用@Id注解标记的主键字段: " + entityClass.getName());
        }

        return metadata;
    }

    /**
     * 处理关联注解
     *
     * @param field 字段
     * @return 关联元数据
     */
    private static AssociationMetadata processAssociation(Field field) {
        // 处理@OneToOne注解
        if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne annotation = field.getAnnotation(OneToOne.class);
            Class<?> targetEntity = annotation.targetEntity();
            if (targetEntity == void.class) {
                targetEntity = field.getType();
            }
            return new AssociationMetadata(field, AssociationMetadata.AssociationType.ONE_TO_ONE,
                    targetEntity, annotation.foreignKey(), annotation.lazy(), annotation.mappedBy());
        }

        // 处理@OneToMany注解
        if (field.isAnnotationPresent(OneToMany.class)) {
            OneToMany annotation = field.getAnnotation(OneToMany.class);
            Class<?> targetEntity = annotation.targetEntity();
            if (targetEntity == void.class) {
                // 对于集合类型字段，需要获取泛型参数类型
                targetEntity = getCollectionGenericType(field);
            }
            return new AssociationMetadata(field, AssociationMetadata.AssociationType.ONE_TO_MANY,
                    targetEntity, "", annotation.lazy(), annotation.mappedBy());
        }

        // 处理@ManyToOne注解
        if (field.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne annotation = field.getAnnotation(ManyToOne.class);
            Class<?> targetEntity = annotation.targetEntity();
            if (targetEntity == void.class) {
                targetEntity = field.getType();
            }
            return new AssociationMetadata(field, AssociationMetadata.AssociationType.MANY_TO_ONE,
                    targetEntity, annotation.foreignKey(), annotation.lazy(), "");
        }

        // 处理@ManyToMany注解
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            Class<?> targetEntity = annotation.targetEntity();
            if (targetEntity == void.class) {
                // 对于集合类型字段，需要获取泛型参数类型
                targetEntity = getCollectionGenericType(field);
            }
            return new AssociationMetadata(field, AssociationMetadata.AssociationType.MANY_TO_MANY,
                    targetEntity, "", annotation.lazy(), annotation.mappedBy());
        }

        return null;
    }

    /**
     * 获取集合字段的泛型参数类型
     *
     * @param field 字段
     * @return 泛型参数类型
     */
    private static Class<?> getCollectionGenericType(Field field) {
        // 简化处理，实际项目中可能需要更复杂的泛型解析
        return field.getType();
    }

    /**
     * 驼峰命名转下划线命名
     *
     * @param camelCase 驼峰命名
     * @return 下划线命名
     */
    private static String camelToUnderscore(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}