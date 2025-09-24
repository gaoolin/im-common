package org.im.orm.loader;

import org.im.orm.core.Session;
import org.im.orm.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 关联加载器
 * 用于处理实体间关联关系的加载
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class AssociationLoader {
    private static final Logger logger = LoggerFactory.getLogger(AssociationLoader.class);

    /**
     * 加载实体的关联字段
     *
     * @param entity  实体对象
     * @param session 会话
     */
    public static void loadAssociations(Object entity, Session session) {
        if (entity == null) {
            return;
        }

        Class<?> entityClass = entity.getClass();
        EntityMetadata metadata = AnnotationProcessor.processEntity(entityClass);
        List<AssociationMetadata> associations = metadata.getAssociations();

        for (AssociationMetadata association : associations) {
            // 只处理延迟加载的关联字段
            if (association.isLazy()) {
                loadLazyAssociation(entity, association, session, metadata);
            }
        }
    }

    /**
     * 加载延迟关联字段
     *
     * @param entity         实体对象
     * @param association    关联元数据
     * @param session        会话
     * @param entityMetadata 实体元数据
     */
    private static void loadLazyAssociation(Object entity, AssociationMetadata association, Session session, EntityMetadata entityMetadata) {
        try {
            Field field = association.getField();
            field.setAccessible(true);

            // 如果关联字段已经有值，则不需要再次加载
            if (field.get(entity) != null) {
                return;
            }

            switch (association.getType()) {
                case ONE_TO_ONE:
                    // 处理一对一关联
                    loadOneToOneAssociation(entity, association, session, entityMetadata, field);
                    break;
                case ONE_TO_MANY:
                    // 处理一对多关联
                    loadOneToManyAssociation(entity, association, session, entityMetadata, field);
                    break;
                case MANY_TO_ONE:
                    // 处理多对一关联
                    loadManyToOneAssociation(entity, association, session, entityMetadata, field);
                    break;
                case MANY_TO_MANY:
                    // 处理多对多关联
                    loadManyToManyAssociation(entity, association, session, entityMetadata, field);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error loading association for field: " + (association != null && association.getField() != null ? association.getField().getName() : "unknown"), e);
        }
    }

    /**
     * 加载一对一关联
     *
     * @param entity         实体对象
     * @param association    关联元数据
     * @param session        会话
     * @param entityMetadata 实体元数据
     * @param field          关联字段
     */
    private static void loadOneToOneAssociation(Object entity, AssociationMetadata association, Session session, EntityMetadata entityMetadata, Field field) {
        try {
            // 一对一关联的加载逻辑
            String mappedBy = association.getMappedBy();
            if (mappedBy != null && !mappedBy.isEmpty()) {
                // 获取目标实体类型
                Class<?> targetEntity = association.getTargetEntity();
                if (targetEntity != null) {
                    // 获取当前实体的ID值
                    Field idField = entityMetadata.getIdField();
                    idField.setAccessible(true);
                    Object idValue = idField.get(entity);

                    if (idValue != null) {
                        // 构建外键列名
                        String foreignKeyColumn = mappedBy + "_id";

                        // 查询关联的实体
                        Object associatedEntity = session.createQuery(targetEntity).eq(foreignKeyColumn, idValue).getSingleResult();

                        // 设置关联字段的值
                        if (associatedEntity != null) {
                            field.set(entity, associatedEntity);
                        }
                    }
                }
            } else {
                // 如果没有mappedBy属性，使用外键方式加载
                String foreignKey = association.getForeignKey();
                if (foreignKey != null && !foreignKey.isEmpty()) {
                    // 从实体中获取外键值
                    Field foreignKeyField = entityMetadata.getColumnFields().get(foreignKey);
                    if (foreignKeyField != null) {
                        foreignKeyField.setAccessible(true);
                        Object foreignKeyValue = foreignKeyField.get(entity);

                        // 如果外键值不为空，则加载关联实体
                        if (foreignKeyValue != null) {
                            Class<?> targetEntity = association.getTargetEntity();
                            Object associatedEntity = session.findById(targetEntity, foreignKeyValue);
                            field.set(entity, associatedEntity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading one-to-one association for field: " + field.getName(), e);
        }
    }

    /**
     * 加载一对多关联
     *
     * @param entity         实体对象
     * @param association    关联元数据
     * @param session        会话
     * @param entityMetadata 实体元数据
     * @param field          关联字段
     */
    private static void loadOneToManyAssociation(Object entity, AssociationMetadata association, Session session, EntityMetadata entityMetadata, Field field) {
        try {
            // 一对多关联的加载逻辑
            // 通过mappedBy字段查找关联的实体
            String mappedBy = association.getMappedBy();
            if (mappedBy != null && !mappedBy.isEmpty()) {
                // 获取目标实体类型
                Class<?> targetEntity = getTargetEntityClass(field);

                if (targetEntity != null) {
                    // 获取当前实体的ID值
                    Field idField = entityMetadata.getIdField();
                    idField.setAccessible(true);
                    Object idValue = idField.get(entity);

                    if (idValue != null) {
                        // 构建外键列名：mappedBy字段名 + "_id"
                        String foreignKeyColumn = mappedBy + "_id";

                        // 查询关联的实体列表
                        List<?> associatedEntities = session.createQuery(targetEntity).eq(foreignKeyColumn, idValue)  // 使用正确的列名
                                .getResultList();

                        // 设置关联字段的值
                        field.set(entity, associatedEntities);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading one-to-many association for field: " + field.getName(), e);
        }
    }

    /**
     * 加载多对一关联
     *
     * @param entity         实体对象
     * @param association    关联元数据
     * @param session        会话
     * @param entityMetadata 实体元数据
     * @param field          关联字段
     */
    private static void loadManyToOneAssociation(Object entity, AssociationMetadata association, Session session, EntityMetadata entityMetadata, Field field) {
        try {
            // 获取外键字段名
            String foreignKey = association.getForeignKey();
            if (foreignKey == null || foreignKey.isEmpty()) {
                // 如果没有指定外键字段名，使用默认规则：关联字段名 + "_id"
                foreignKey = field.getName() + "_id";
            }

            // 从实体中获取外键值
            // 通过EntityMetadata的columnFields映射查找外键字段
            Field foreignKeyField = entityMetadata.getColumnFields().get(foreignKey);
            if (foreignKeyField != null) {
                foreignKeyField.setAccessible(true);
                Object foreignKeyValue = foreignKeyField.get(entity);

                // 如果外键值不为空，则加载关联实体
                if (foreignKeyValue != null) {
                    Class<?> targetEntity = association.getTargetEntity();
                    Object associatedEntity = session.findById(targetEntity, foreignKeyValue);
                    field.set(entity, associatedEntity);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading many-to-one association for field: " + field.getName(), e);
        }
    }

    /**
     * 加载多对多关联
     *
     * @param entity         实体对象
     * @param association    关联元数据
     * @param session        会话
     * @param entityMetadata 实体元数据
     * @param field          关联字段
     */
    private static void loadManyToManyAssociation(Object entity, AssociationMetadata association, Session session, EntityMetadata entityMetadata, Field field) {
        try {
            // 多对多关联的加载逻辑
            // 获取目标实体类型
            Class<?> targetEntity = getTargetEntityClass(field);
            if (targetEntity != null) {
                // 获取当前实体的ID值
                Field idField = entityMetadata.getIdField();
                idField.setAccessible(true);
                Object idValue = idField.get(entity);

                if (idValue != null) {
                    // 多对多关联通常需要一个中间表
                    // 由于框架设计文档中没有明确说明多对多的实现方式，这里提供一个通用的处理方法
                    // 假设中间表名为: entity1_entity2 (按字母顺序排列)
                    String tableName1 = entityMetadata.getTableName();
                    String tableName2 = AnnotationProcessor.processEntity(targetEntity).getTableName();

                    // 构造中间表名（按字母顺序）
                    String joinTableName;
                    if (tableName1.compareTo(tableName2) < 0) {
                        joinTableName = tableName1 + "_" + tableName2;
                    } else {
                        joinTableName = tableName2 + "_" + tableName1;
                    }

                    // 构造外键列名
                    String foreignKeyColumn1 = tableName1 + "_id";
                    String foreignKeyColumn2 = tableName2 + "_id";

                    // 由于当前Query接口不支持JOIN操作，我们使用原生SQL查询来实现
                    String sql = "SELECT t2.* FROM " + joinTableName + " t1 JOIN " + AnnotationProcessor.processEntity(targetEntity).getTableName() + " t2 ON t1." + foreignKeyColumn2 + " = t2.id WHERE t1." + foreignKeyColumn1 + " = ?";

                    // 执行原生SQL查询
                    try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                        stmt.setObject(1, idValue);
                        try (ResultSet rs = stmt.executeQuery()) {
                            List<Object> associatedEntities = new ArrayList<>();
                            while (rs.next()) {
                                Object associatedEntity = ResultSetMapper.mapResultSetToEntity(rs, AnnotationProcessor.processEntity(targetEntity));
                                associatedEntities.add(associatedEntity);
                            }
                            field.set(entity, associatedEntities);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading many-to-many association for field: " + field.getName(), e);
        }
    }

    /**
     * 获取目标实体类
     *
     * @param field 关联字段
     * @return 目标实体类
     */
    private static Class<?> getTargetEntityClass(Field field) {
        // 首先尝试从泛型参数中获取目标实体类型
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                return (Class<?>) actualTypeArguments[0];
            }
        }

        // 如果无法从泛型参数获取，则尝试从注解中获取
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        if (manyToOne != null && manyToOne.targetEntity() != void.class) {
            return manyToOne.targetEntity();
        }

        return null;
    }
}
