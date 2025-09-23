package org.im.orm.core;

import org.im.exception.type.orm.ORMException;
import org.im.orm.datasource.ConnectionProvider;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多数据源会话的具体实现
 * 支持在不同数据源之间切换
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class MultiDataSourceSessionImpl implements MultiDataSourceSession {
    private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceSessionImpl.class);
    private String currentDataSourceName;
    private Map<String, Session> sessions = new HashMap<>();
    private boolean closed = false;

    /**
     * 构造函数
     *
     * @param defaultDataSourceName 默认数据源名称
     */
    public MultiDataSourceSessionImpl(String defaultDataSourceName) {
        if (!DataSourceManager.getDataSourceNames().contains(defaultDataSourceName)) {
            throw new ORMException("默认数据源不存在: " + defaultDataSourceName);
        }
        this.currentDataSourceName = defaultDataSourceName;
    }

    @Override
    public void switchDataSource(String dataSourceName) {
        checkClosed();
        if (!DataSourceManager.getDataSourceNames().contains(dataSourceName)) {
            throw new ORMException("数据源不存在: " + dataSourceName);
        }
        this.currentDataSourceName = dataSourceName;
    }

    @Override
    public String getCurrentDataSourceName() {
        return currentDataSourceName;
    }

    /**
     * 获取当前会话
     *
     * @return 当前会话
     */
    private Session getCurrentSession() {
        checkClosed();
        Session session = sessions.get(currentDataSourceName);
        if (session == null) {
            ConnectionProvider provider = DataSourceManager.getDataSource(currentDataSourceName);
            if (provider == null) {
                throw new ORMException("无法获取数据源连接提供者: " + currentDataSourceName);
            }
            session = new SessionImpl(provider);
            sessions.put(currentDataSourceName, session);
        }
        return session;
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        return getCurrentSession().findById(entityClass, id);
    }

    @Override
    public <T> java.util.List<T> findAll(Class<T> entityClass) {
        return getCurrentSession().findAll(entityClass);
    }

    @Override
    public void save(Object entity) {
        getCurrentSession().save(entity);
    }

    @Override
    public void update(Object entity) {
        getCurrentSession().update(entity);
    }

    @Override
    public void delete(Object entity) {
        getCurrentSession().delete(entity);
    }

    @Override
    public <T> void saveBatch(List<T> entities) {
        getCurrentSession().saveBatch(entities);
    }

    @Override
    public <T> void updateBatch(List<T> entities) {
        getCurrentSession().updateBatch(entities);
    }

    @Override
    public <T> void deleteBatch(List<T> entities) {
        getCurrentSession().deleteBatch(entities);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass) {
        return getCurrentSession().createQuery(resultClass);
    }

    @Override
    public void beginTransaction() {
        getCurrentSession().beginTransaction();
    }

    @Override
    public void commit() {
        getCurrentSession().commit();
    }

    @Override
    public void rollback() {
        getCurrentSession().rollback();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        for (Session session : sessions.values()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("关闭数据源会话失败", e);
            }
        }
        sessions.clear();
        closed = true;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getCurrentSession().getConnection();
    }

    /**
     * 检查会话是否已关闭
     */
    private void checkClosed() {
        if (closed) {
            throw new ORMException("会话已关闭");
        }
    }
}