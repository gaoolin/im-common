package org.im.orm.query;

import java.util.List;

/**
 * 查询接口
 * 提供基本的查询功能
 *
 * @param <T> 查询结果类型
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public interface Query<T> {
    /**
     * 添加等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> eq(String field, Object value);

    /**
     * 添加不等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> ne(String field, Object value);

    /**
     * 添加大于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> gt(String field, Object value);

    /**
     * 添加大于等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> ge(String field, Object value);

    /**
     * 添加小于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> lt(String field, Object value);

    /**
     * 添加小于等于条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> le(String field, Object value);

    /**
     * 添加LIKE条件
     *
     * @param field 字段名
     * @param value 值
     * @return 查询对象
     */
    Query<T> like(String field, Object value);

    /**
     * 添加IN条件
     *
     * @param field  字段名
     * @param values 值列表
     * @return 查询对象
     */
    Query<T> in(String field, List<Object> values);

    /**
     * 添加NOT IN条件
     *
     * @param field  字段名
     * @param values 值列表
     * @return 查询对象
     */
    Query<T> notIn(String field, List<Object> values);

    /**
     * 添加IS NULL条件
     *
     * @param field 字段名
     * @return 查询对象
     */
    Query<T> isNull(String field);

    /**
     * 添加IS NOT NULL条件
     *
     * @param field 字段名
     * @return 查询对象
     */
    Query<T> isNotNull(String field);

    /**
     * 添加BETWEEN条件
     *
     * @param field 字段名
     * @param start 起始值
     * @param end   结束值
     * @return 查询对象
     */
    Query<T> between(String field, Object start, Object end);

    /**
     * 添加LEFT JOIN
     *
     * @param entityClass 关联实体类
     * @param joinField   关联字段
     * @param alias       别名
     * @return 查询对象
     */
    Query<T> leftJoin(Class<?> entityClass, String joinField, String alias);

    /**
     * 添加INNER JOIN
     *
     * @param entityClass 关联实体类
     * @param joinField   关联字段
     * @param alias       别名
     * @return 查询对象
     */
    Query<T> innerJoin(Class<?> entityClass, String joinField, String alias);

    /**
     * 添加WHERE条件(原生SQL)
     *
     * @param condition 条件表达式
     * @param params    参数值
     * @return 查询对象
     */
    Query<T> where(String condition, Object... params);

    /**
     * 添加GROUP BY子句
     *
     * @param fields 字段名列表
     * @return 查询对象
     */
    Query<T> groupBy(String... fields);

    /**
     * 添加HAVING子句
     *
     * @param condition 条件表达式
     * @param params    参数值
     * @return 查询对象
     */
    Query<T> having(String condition, Object... params);

    /**
     * 添加ORDER BY子句
     *
     * @param field     字段名
     * @param ascending 是否升序
     * @return 查询对象
     */
    Query<T> orderBy(String field, boolean ascending);

    /**
     * 添加多个ORDER BY子句
     *
     * @param fields 字段名和排序方向的映射
     * @return 查询对象
     */
    Query<T> orderBy(java.util.Map<String, Boolean> fields);

    /**
     * 设置LIMIT
     *
     * @param limit 限制数量
     * @return 查询对象
     */
    Query<T> limit(int limit);

    /**
     * 设置OFFSET
     *
     * @param offset 偏移量
     * @return 查询对象
     */
    Query<T> offset(int offset);

    /**
     * 设置DISTINCT
     *
     * @return 查询对象
     */
    Query<T> distinct();

    /**
     * 执行查询并返回单个结果
     *
     * @return 查询结果
     */
    T getSingleResult();

    /**
     * 执行查询并返回结果列表
     *
     * @return 查询结果列表
     */
    List<T> getResultList();

    /**
     * 执行COUNT查询
     *
     * @return 记录数
     */
    long count();

    /**
     * 执行SUM查询
     *
     * @param field 字段名
     * @return 求和结果
     */
    Number sum(String field);

    /**
     * 执行AVG查询
     *
     * @param field 字段名
     * @return 平均值结果
     */
    Number avg(String field);

    /**
     * 执行MAX查询
     *
     * @param field 字段名
     * @return 最大值结果
     */
    Object max(String field);

    /**
     * 执行MIN查询
     *
     * @param field 字段名
     * @return 最小值结果
     */
    Object min(String field);

    /**
     * 执行UPDATE操作
     *
     * @param field 字段名
     * @param value 新值
     * @return 更新记录数
     */
    int update(String field, Object value);

    /**
     * 执行DELETE操作
     *
     * @return 删除记录数
     */
    int delete();

    /**
     * 设置查询超时时间
     *
     * @param timeout 超时时间(秒)
     * @return 查询对象
     */
    Query<T> setTimeout(int timeout);

    /**
     * 设置是否缓存查询结果
     *
     * @param cacheable 是否缓存
     * @return 查询对象
     */
    Query<T> setCacheable(boolean cacheable);

    /**
     * 添加自定义SQL片段
     *
     * @param sql SQL片段
     * @return 添加自定义SQL片段
     */
    Query<T> addSql(String sql);
}
