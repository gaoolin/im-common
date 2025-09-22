package org.im.orm.query;

import org.im.exception.type.orm.ORMException;
import org.im.orm.core.Session;
import org.im.orm.mapping.AnnotationProcessor;
import org.im.orm.mapping.EntityMetadata;
import org.im.orm.mapping.ResultSetMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询接口实现类
 * 提供链式调用的查询构建器功能的具体实现
 *
 * @param <T> 查询结果类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class QueryImpl<T> implements Query<T> {
    private Session session;
    private Class<T> resultClass;
    private EntityMetadata metadata;
    private StringBuilder whereClause;
    private List<Object> parameters;
    private StringBuilder orderByClause;
    private int limit = -1;
    private int offset = -1;
    private List<String> whereColumns; // 用于记录WHERE子句中使用的列名

    /**
     * 构造函数
     *
     * @param session     会话
     * @param resultClass 结果类
     */
    public QueryImpl(Session session, Class<T> resultClass) {
        this.session = session;
        this.resultClass = resultClass;
        this.metadata = AnnotationProcessor.processEntity(resultClass);
        this.whereClause = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.orderByClause = new StringBuilder();
        this.whereColumns = new ArrayList<>();
    }

    @Override
    public Query<T> eq(String field, Object value) {
        appendCondition(field, "=", value);
        return this;
    }

    @Override
    public Query<T> ne(String field, Object value) {
        appendCondition(field, "!=", value);
        return this;
    }

    @Override
    public Query<T> gt(String field, Object value) {
        appendCondition(field, ">", value);
        return this;
    }

    @Override
    public Query<T> ge(String field, Object value) {
        appendCondition(field, ">=", value);
        return this;
    }

    @Override
    public Query<T> lt(String field, Object value) {
        appendCondition(field, "<", value);
        return this;
    }

    @Override
    public Query<T> le(String field, Object value) {
        appendCondition(field, "<=", value);
        return this;
    }

    @Override
    public Query<T> like(String field, Object value) {
        appendCondition(field, "LIKE", value);
        return this;
    }

    @Override
    public Query<T> in(String field, List<Object> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }

        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }

        whereClause.append(columnName).append(" IN (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                whereClause.append(", ");
            }
            whereClause.append("?");
            parameters.add(values.get(i));
        }
        whereClause.append(")");
        whereColumns.add(columnName);

        return this;
    }

    @Override
    public Query<T> orderBy(String field, boolean ascending) {
        String columnName = getColumnOrFieldName(field);
        if (orderByClause.length() > 0) {
            orderByClause.append(", ");
        }
        orderByClause.append(columnName).append(" ").append(ascending ? "ASC" : "DESC");
        return this;
    }

    @Override
    public Query<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Query<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public T getSingleResult() {
        List<T> results = getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<T> getResultList() {
        try {
            String sql = buildSelectSQL();
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                bindParameters(stmt);

                try (ResultSet rs = stmt.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        T entity = ResultSetMapper.mapResultSetToEntity(rs, metadata);
                        results.add(entity);
                    }
                    return results;
                }
            }
        } catch (SQLException e) {
            throw new ORMException("执行查询失败: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            String sql = buildCountSQL();
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                bindParameters(stmt);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new ORMException("执行COUNT查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加条件
     *
     * @param field    字段名或列名
     * @param operator 操作符
     * @param value    值
     */
    private void appendCondition(String field, String operator, Object value) {
        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" ").append(operator).append(" ?");
        parameters.add(value);
        whereColumns.add(columnName);
    }

    /**
     * 获取列名或字段名
     *
     * @param field 字段名或列名
     * @return 列名
     */
    private String getColumnOrFieldName(String field) {
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();

        // 首先尝试作为字段名查找
        String columnName = fieldColumnMapping.get(field);
        if (columnName != null) {
            return columnName;
        }

        // 如果找不到，则认为是列名
        return field;
    }

    /**
     * 构建SELECT SQL语句
     *
     * @return SQL语句
     */
    private String buildSelectSQL() {
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

        // 添加WHERE子句
        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        // 添加ORDER BY子句
        if (orderByClause.length() > 0) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        // 添加LIMIT和OFFSET
        if (limit >= 0) {
            sql.append(" LIMIT ").append(limit);
        }

        if (offset >= 0) {
            sql.append(" OFFSET ").append(offset);
        }

        return sql.toString();
    }

    /**
     * 构建COUNT SQL语句
     *
     * @return SQL语句
     */
    private String buildCountSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(metadata.getTableName());

        // 添加WHERE子句
        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        return sql.toString();
    }

    /**
     * 绑定参数
     *
     * @param stmt 预处理语句
     * @throws SQLException SQL异常
     */
    private void bindParameters(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            stmt.setObject(i + 1, parameters.get(i));
        }
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