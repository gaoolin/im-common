package org.im.orm.query;

import org.im.exception.type.orm.ORMException;
import org.im.orm.core.Session;
import org.im.orm.mapping.AnnotationProcessor;
import org.im.orm.mapping.AssociationMetadata;
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
 * @date 2025/09/22
 */
public class QueryImpl<T> implements Query<T> {
    private Session session;
    private Class<T> resultClass;
    private EntityMetadata metadata;
    private StringBuilder whereClause;
    private List<Object> parameters;
    private StringBuilder orderByClause;
    private StringBuilder groupByClause;
    private StringBuilder havingClause;
    private List<Object> havingParameters;
    private int limit = -1;
    private int offset = -1;
    private boolean distinct = false;
    private int timeout = 0; // 查询超时时间(秒)
    private boolean cacheable = false; // 是否缓存查询结果
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
        this.groupByClause = new StringBuilder();
        this.havingClause = new StringBuilder();
        this.havingParameters = new ArrayList<>();
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

    /**
     * 添加NOT IN条件
     *
     * @param field  字段名
     * @param values 值列表
     * @return 查询对象
     */
    @Override
    public Query<T> notIn(String field, List<Object> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }

        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }

        whereClause.append(columnName).append(" NOT IN (");
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

    /**
     * 添加IS NULL条件
     *
     * @param field 字段名
     * @return 查询对象
     */
    @Override
    public Query<T> isNull(String field) {
        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" IS NULL");
        whereColumns.add(columnName);

        return this;
    }

    /**
     * 添加IS NOT NULL条件
     *
     * @param field 字段名
     * @return 查询对象
     */
    @Override
    public Query<T> isNotNull(String field) {
        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" IS NOT NULL");
        whereColumns.add(columnName);

        return this;
    }

    /**
     * 添加BETWEEN条件
     *
     * @param field 字段名
     * @param start 起始值
     * @param end   结束值
     * @return 查询对象
     */
    @Override
    public Query<T> between(String field, Object start, Object end) {
        String columnName = getColumnOrFieldName(field);
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(columnName).append(" BETWEEN ? AND ?");
        parameters.add(start);
        parameters.add(end);
        whereColumns.add(columnName);

        return this;
    }

    /**
     * 添加LEFT JOIN
     *
     * @param entityClass 关联实体类
     * @param joinField   关联字段
     * @param alias       别名
     * @return 查询对象
     */
    @Override
    public Query<T> leftJoin(Class<?> entityClass, String joinField, String alias) {
        // LEFT JOIN功能需要在SQL构建逻辑中实现
        // 这里暂时只做接口实现，具体逻辑需要在buildSelectSQL中实现
        return this;
    }

    /**
     * 添加INNER JOIN
     *
     * @param entityClass 关联实体类
     * @param joinField   关联字段
     * @param alias       别名
     * @return 查询对象
     */
    @Override
    public Query<T> innerJoin(Class<?> entityClass, String joinField, String alias) {
        // INNER JOIN功能需要在SQL构建逻辑中实现
        // 这里暂时只做接口实现，具体逻辑需要在buildSelectSQL中实现
        return this;
    }

    /**
     * 添加WHERE条件(原生SQL)
     *
     * @param condition 条件表达式
     * @param params    参数值
     * @return 查询对象
     */
    @Override
    public Query<T> where(String condition, Object... params) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(condition);

        // 添加参数
        for (Object param : params) {
            parameters.add(param);
        }

        return this;
    }

    /**
     * 添加GROUP BY子句
     *
     * @param fields 字段名列表
     * @return 查询对象
     */
    @Override
    public Query<T> groupBy(String... fields) {
        if (fields == null || fields.length == 0) {
            return this;
        }

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                groupByClause.append(", ");
            }
            String columnName = getColumnOrFieldName(fields[i]);
            groupByClause.append(columnName);
        }

        return this;
    }

    /**
     * 添加HAVING子句
     *
     * @param condition 条件表达式
     * @param params    参数值
     * @return 查询对象
     */
    @Override
    public Query<T> having(String condition, Object... params) {
        if (havingClause.length() > 0) {
            havingClause.append(" AND ");
        }
        havingClause.append(condition);

        // 添加参数
        for (Object param : params) {
            havingParameters.add(param);
        }

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

    /**
     * 添加多个ORDER BY子句
     *
     * @param fields 字段名和排序方向的映射
     * @return 查询对象
     */
    @Override
    public Query<T> orderBy(Map<String, Boolean> fields) {
        if (fields == null || fields.isEmpty()) {
            return this;
        }

        for (Map.Entry<String, Boolean> entry : fields.entrySet()) {
            String field = entry.getKey();
            Boolean ascending = entry.getValue();
            orderBy(field, ascending != null ? ascending : true);
        }

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

    /**
     * 设置DISTINCT
     *
     * @return 查询对象
     */
    @Override
    public Query<T> distinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public T getSingleResult() {
        limit(1);
        List<T> results = getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<T> getResultList() {
        try {
            String sql = buildSelectSQL();
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                // 设置查询超时
                if (timeout > 0) {
                    stmt.setQueryTimeout(timeout);
                }

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
                // 设置查询超时
                if (timeout > 0) {
                    stmt.setQueryTimeout(timeout);
                }

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
     * 执行SUM查询
     *
     * @param field 字段名
     * @return 求和结果
     */
    @Override
    public Number sum(String field) {
        try {
            String sql = buildAggregateSQL("SUM", field);
            return executeAggregateQuery(sql);
        } catch (SQLException e) {
            throw new ORMException("执行SUM查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行AVG查询
     *
     * @param field 字段名
     * @return 平均值结果
     */
    @Override
    public Number avg(String field) {
        try {
            String sql = buildAggregateSQL("AVG", field);
            return executeAggregateQuery(sql);
        } catch (SQLException e) {
            throw new ORMException("执行AVG查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行MAX查询
     *
     * @param field 字段名
     * @return 最大值结果
     */
    @Override
    public Object max(String field) {
        try {
            String sql = buildAggregateSQL("MAX", field);
            return executeObjectQuery(sql);
        } catch (SQLException e) {
            throw new ORMException("执行MAX查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行MIN查询
     *
     * @param field 字段名
     * @return 最小值结果
     */
    @Override
    public Object min(String field) {
        try {
            String sql = buildAggregateSQL("MIN", field);
            return executeObjectQuery(sql);
        } catch (SQLException e) {
            throw new ORMException("执行MIN查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行UPDATE操作
     *
     * @param field 字段名
     * @param value 新值
     * @return 更新记录数
     */
    @Override
    public int update(String field, Object value) {
        try {
            String sql = buildUpdateSQL(field);
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                // 设置查询超时
                if (timeout > 0) {
                    stmt.setQueryTimeout(timeout);
                }

                // 设置更新值
                stmt.setObject(1, value);

                // 绑定WHERE条件参数
                bindParameters(stmt, 2);

                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ORMException("执行UPDATE操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行DELETE操作
     *
     * @return 删除记录数
     */
    @Override
    public int delete() {
        try {
            String sql = buildDeleteSQL();
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                // 设置查询超时
                if (timeout > 0) {
                    stmt.setQueryTimeout(timeout);
                }

                // 绑定WHERE条件参数
                bindParameters(stmt, 1);

                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ORMException("执行DELETE操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置查询超时时间
     *
     * @param timeout 超时时间(秒)
     * @return 查询对象
     */
    @Override
    public Query<T> setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置是否缓存查询结果
     *
     * @param cacheable 是否缓存
     * @return 查询对象
     */
    @Override
    public Query<T> setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
        return this;
    }

    /**
     * 添加自定义SQL片段
     *
     * @param sql SQL片段
     * @return 添加自定义SQL片段
     */
    @Override
    public Query<T> addSql(String sql) {
        return null;
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

        // 添加DISTINCT关键字
        if (distinct) {
            sql.append("DISTINCT ");
        }

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

        // 添加GROUP BY子句
        if (groupByClause.length() > 0) {
            sql.append(" GROUP BY ").append(groupByClause);
        }

        // 添加HAVING子句
        if (havingClause.length() > 0) {
            sql.append(" HAVING ").append(havingClause);
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

        // 添加GROUP BY子句
        if (groupByClause.length() > 0) {
            sql.append(" GROUP BY ").append(groupByClause);
        }

        // 添加HAVING子句
        if (havingClause.length() > 0) {
            sql.append(" HAVING ").append(havingClause);
        }

        return sql.toString();
    }

    /**
     * 构建聚合函数SQL语句
     *
     * @param function 聚合函数名
     * @param field    字段名
     * @return SQL语句
     */
    private String buildAggregateSQL(String function, String field) {
        StringBuilder sql = new StringBuilder();
        String columnName = getColumnOrFieldName(field);
        sql.append("SELECT ").append(function).append("(").append(columnName).append(") FROM ")
                .append(metadata.getTableName());

        // 添加WHERE子句
        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        // 添加GROUP BY子句
        if (groupByClause.length() > 0) {
            sql.append(" GROUP BY ").append(groupByClause);
        }

        // 添加HAVING子句
        if (havingClause.length() > 0) {
            sql.append(" HAVING ").append(havingClause);
        }

        return sql.toString();
    }

    /**
     * 构建UPDATE SQL语句
     *
     * @param field 字段名
     * @return SQL语句
     */
    private String buildUpdateSQL(String field) {
        StringBuilder sql = new StringBuilder();
        String columnName = getColumnOrFieldName(field);
        sql.append("UPDATE ").append(metadata.getTableName())
                .append(" SET ").append(columnName).append(" = ?");

        // 添加WHERE子句
        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }

        return sql.toString();
    }

    /**
     * 构建DELETE SQL语句
     *
     * @return SQL语句
     */
    private String buildDeleteSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(metadata.getTableName());

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
        bindParameters(stmt, 1);
    }

    /**
     * 绑定参数
     *
     * @param stmt       预处理语句
     * @param startIndex 起始索引
     * @throws SQLException SQL异常
     */
    private void bindParameters(PreparedStatement stmt, int startIndex) throws SQLException {
        int index = startIndex;

        // 绑定WHERE条件参数
        for (Object param : parameters) {
            stmt.setObject(index++, param);
        }

        // 绑定HAVING条件参数
        for (Object param : havingParameters) {
            stmt.setObject(index++, param);
        }
    }

    /**
     * 执行聚合查询
     *
     * @param sql SQL语句
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    private Number executeAggregateQuery(String sql) throws SQLException {
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            // 设置查询超时
            if (timeout > 0) {
                stmt.setQueryTimeout(timeout);
            }

            bindParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (Number) rs.getObject(1);
                }
                return null;
            }
        }
    }

    /**
     * 执行对象查询
     *
     * @param sql SQL语句
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    private Object executeObjectQuery(String sql) throws SQLException {
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            // 设置查询超时
            if (timeout > 0) {
                stmt.setQueryTimeout(timeout);
            }

            bindParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
                return null;
            }
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
        for (AssociationMetadata assoc : metadata.getAssociations()) {
            if (assoc.getField().getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
