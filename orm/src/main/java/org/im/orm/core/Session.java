package org.im.orm.core;

import org.im.orm.query.Query;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * ORM框架核心Session接口
 * 负责与数据库的交互，是ORM框架的核心入口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */
public interface Session {
    /**
     * 根据ID查找实体
     *
     * @param entityClass 实体类
     * @param id          ID值
     * @param <T>         实体类型
     * @return 实体对象
     */
    <T> T findById(Class<T> entityClass, Object id);

    /**
     * 查找所有实体
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 实体列表
     */
    <T> List<T> findAll(Class<T> entityClass);

    /**
     * 保存实体
     *
     * @param entity 实体对象
     */
    void save(Object entity);

    /**
     * 更新实体
     *
     * @param entity 实体对象
     */
    void update(Object entity);

    /**
     * 删除实体
     *
     * @param entity 实体对象
     */
    void delete(Object entity);

    /**
     * 批量保存实体
     *
     * @param entities 实体对象列表
     * @param <T>      实体类型
     */
    <T> void saveBatch(List<T> entities);

    /**
     * 批量更新实体
     *
     * @param entities 实体对象列表
     * @param <T>      实体类型
     */
    <T> void updateBatch(List<T> entities);

    /**
     * 批量删除实体
     *
     * @param entities 实体对象列表
     * @param <T>      实体类型
     */
    <T> void deleteBatch(List<T> entities);

    /**
     * 创建查询对象
     *
     * @param resultClass 结果类
     * @param <T>         结果类型
     * @return 查询对象
     */
    <T> Query<T> createQuery(Class<T> resultClass);

    /**
     * 开启事务
     */
    void beginTransaction();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 回滚事务
     */
    void rollback();

    /**
     * 关闭会话
     */
    void close();

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection() throws SQLException;
}