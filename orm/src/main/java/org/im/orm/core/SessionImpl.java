package org.im.orm.core;

import org.im.common.exception.type.orm.ORMException;
import org.im.orm.datasource.ConnectionProvider;
import org.im.orm.loader.AssociationLoader;
import org.im.orm.mapping.*;
import org.im.orm.query.Query;
import org.im.orm.query.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session接口的具体实现
 * 负责与数据库的实际交互
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class SessionImpl implements Session {
    private static final Logger logger = LoggerFactory.getLogger(SessionImpl.class);
    private final ConnectionProvider connectionProvider;
    private final Map<Class<?>, EntityMetadata> metadataCache;
    private Connection connection;

    /**
     * 构造函数
     *
     * @param connectionProvider 连接提供者
     */
    public SessionImpl(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.metadataCache = new ConcurrentHashMap<>();
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        try {
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateSelectByIdSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                stmt.setObject(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        T entity = ResultSetMapper.mapResultSetToEntity(rs, metadata);
                        // 加载关联字段
                        AssociationLoader.loadAssociations(entity, this);
                        return entity;
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ORMException("根据ID查询实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        try {
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateSelectAllSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    T entity = ResultSetMapper.mapResultSetToEntity(rs, metadata);
                    // 加载关联字段
                    AssociationLoader.loadAssociations(entity, this);
                    results.add(entity);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new ORMException("查询所有实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Object entity) {
        try {
            @SuppressWarnings("unchecked")
            Class<Object> entityClass = (Class<Object>) entity.getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateInsertSQL(metadata);

            // 使用Statement.RETURN_GENERATED_KEYS获取自动生成的主键
            try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindEntityToStatement(stmt, entity, metadata, false);
                stmt.executeUpdate();

                // 获取自动生成的主键并设置到实体中
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Field idField = metadata.getIdField();
                        idField.setAccessible(true);
                        Class<?> idType = idField.getType();

                        Object generatedId;
                        if (idType == Long.class || idType == long.class) {
                            generatedId = generatedKeys.getLong(1);
                        } else if (idType == Integer.class || idType == int.class) {
                            generatedId = generatedKeys.getInt(1);
                        } else {
                            generatedId = generatedKeys.getObject(1);
                        }

                        idField.set(entity, generatedId);
                    }
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new ORMException("保存实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Object entity) {
        try {
            @SuppressWarnings("unchecked")
            Class<Object> entityClass = (Class<Object>) entity.getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateUpdateSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                bindEntityToStatement(stmt, entity, metadata, true);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ORMException("更新实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Object entity) {
        try {
            @SuppressWarnings("unchecked")
            Class<Object> entityClass = (Class<Object>) entity.getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateDeleteSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                Field idField = metadata.getIdField();
                idField.setAccessible(true);
                Object idValue = idField.get(entity);
                stmt.setObject(1, idValue);
                stmt.executeUpdate();
            } catch (IllegalAccessException e) {
                throw new ORMException("获取实体ID值失败: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new ORMException("删除实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> void saveBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entities.get(0).getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateInsertSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (T entity : entities) {
                    bindEntityToStatement(stmt, entity, metadata, false);
                    stmt.addBatch();
                }

                stmt.executeBatch();

                // 获取自动生成的主键并设置到实体中
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    int index = 0;
                    while (generatedKeys.next() && index < entities.size()) {
                        T entity = entities.get(index);
                        Field idField = metadata.getIdField();
                        idField.setAccessible(true);
                        Class<?> idType = idField.getType();

                        Object generatedId;
                        if (idType == Long.class || idType == long.class) {
                            generatedId = generatedKeys.getLong(1);
                        } else if (idType == Integer.class || idType == int.class) {
                            generatedId = generatedKeys.getInt(1);
                        } else {
                            generatedId = generatedKeys.getObject(1);
                        }

                        idField.set(entity, generatedId);
                        index++;
                    }
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new ORMException("批量保存实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> void updateBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entities.get(0).getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateUpdateSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (T entity : entities) {
                    bindEntityToStatement(stmt, entity, metadata, true);
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new ORMException("批量更新实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> void deleteBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entities.get(0).getClass();
            EntityMetadata metadata = getEntityMetadata(entityClass);
            String sql = SQLGenerator.generateDeleteSQL(metadata);

            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (T entity : entities) {
                    Field idField = metadata.getIdField();
                    idField.setAccessible(true);
                    Object idValue = idField.get(entity);
                    stmt.setObject(1, idValue);
                    stmt.addBatch();
                }

                stmt.executeBatch();
            } catch (IllegalAccessException e) {
                throw new ORMException("获取实体ID值失败: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new ORMException("批量删除实体失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass) {
        return new QueryImpl<>(this, resultClass);
    }

    @Override
    public void beginTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new ORMException("开启事务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void commit() {
        try {
            getConnection().commit();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new ORMException("提交事务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void rollback() {
        try {
            getConnection().rollback();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new ORMException("回滚事务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("Error closing connection", e);
            } finally {
                connection = null;
            }
        }

        if (connectionProvider != null) {
            connectionProvider.releaseConnection(connection);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = connectionProvider.getConnection();
        }
        return connection;
    }

    /**
     * 获取实体元数据
     *
     * @param entityClass 实体类
     * @return 实体元数据
     */
    private EntityMetadata getEntityMetadata(Class<?> entityClass) {
        return metadataCache.computeIfAbsent(entityClass, AnnotationProcessor::processEntity);
    }

    /**
     * 将实体绑定到预处理语句
     *
     * @param stmt     预处理语句
     * @param entity   实体对象
     * @param metadata 实体元数据
     * @param isUpdate 是否为更新操作
     * @throws SQLException SQL异常
     * @throws ORMException ORM异常
     */
    private void bindEntityToStatement(PreparedStatement stmt, Object entity, EntityMetadata metadata, boolean isUpdate)
            throws SQLException, ORMException {
        try {
            int paramIndex = 1;

            // 创建字段列表，确保顺序与SQL中一致
            List<Map.Entry<String, String>> fieldEntries = new ArrayList<>();
            for (Map.Entry<String, String> entry : metadata.getFieldColumnMapping().entrySet()) {
                String fieldName = entry.getKey();

                // 检查是否是主键字段
                boolean isIdField = metadata.getIdField().getName().equals(fieldName);

                // 如果是主键字段，检查是否是自动生成的
                if (isIdField) {
                    Id idAnnotation = metadata.getIdField().getAnnotation(Id.class);
                    if (idAnnotation != null && idAnnotation.autoGenerate()) {
                        // 跳过自动生成的主键字段
                        continue;
                    }
                }

                // 跳过关联字段
                if (isAssociationField(metadata, fieldName)) {
                    continue;
                }

                // 如果是更新操作，跳过主键字段（主键在WHERE子句中处理）
                if (isUpdate && isIdField) {
                    continue;
                }

                fieldEntries.add(entry);
            }

            // 按顺序绑定字段值
            for (Map.Entry<String, String> entry : fieldEntries) {
                String fieldName = entry.getKey();
                Field field = metadata.getEntityClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                Object value = field.get(entity);
                // 修复：正确处理null值，避免PostgreSQL将null转换为0
                if (value == null) {
                    stmt.setNull(paramIndex++, Types.OTHER);
                } else {
                    stmt.setObject(paramIndex++, value);
                }
            }

            // 如果是更新操作，绑定主键字段到WHERE子句
            if (isUpdate) {
                Field idField = metadata.getIdField();
                Object idValue = idField.get(entity);
                stmt.setObject(paramIndex, idValue);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ORMException("绑定实体到预处理语句失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断字段是否为与关联字段对应的外键字段
     *
     * @param metadata  实体元数据
     * @param fieldName 字段名
     * @return 是否为外键字段
     */
    private boolean isForeignKeyField(EntityMetadata metadata, String fieldName) {
        // 检查字段是否与某个关联字段的外键相同
        for (AssociationMetadata assoc : metadata.getAssociations()) {
            ManyToOne manyToOne = assoc.getField().getAnnotation(ManyToOne.class);
            if (manyToOne != null && manyToOne.foreignKey().equals(metadata.getFieldColumnMapping().get(fieldName))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字段是否为关联字段
     *
     * @param metadata  实体元数据
     * @param fieldName 字段名
     * @return 是否为关联字段
     */
    private boolean isAssociationField(EntityMetadata metadata, String fieldName) {
        return metadata.getAssociations().stream()
                .anyMatch(assoc -> assoc.getField().getName().equals(fieldName));
    }
}