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
     * 添加ORDER BY子句
     *
     * @param field     字段名
     * @param ascending 是否升序
     * @return 查询对象
     */
    Query<T> orderBy(String field, boolean ascending);

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
}