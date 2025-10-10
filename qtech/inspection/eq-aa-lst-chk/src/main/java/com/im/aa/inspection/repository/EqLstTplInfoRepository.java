package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * EqLstTplInfoPO实体的数据库操作仓库类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/07/22
 */
public class EqLstTplInfoRepository {
    private SessionFactory sessionFactory;

    public EqLstTplInfoRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * 保存或更新EqLstTplInfoPO实体
     *
     * @param entity 要保存的实体对象
     */
    public void save(EqLstTplInfoPO entity) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("保存实体失败", e);
        }
    }

    /**
     * 根据ID查找EqLstTplInfoPO实体
     *
     * @param id 实体ID
     * @return EqLstTplInfoPO实体对象
     */
    public EqLstTplInfoPO findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqLstTplInfoPO.class, id);
    }

    /**
     * 查找所有EqLstTplInfoPO实体
     *
     * @return 实体列表
     */
    public List<EqLstTplInfoPO> findAll() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplInfoPO> query = session.createQuery("FROM EqLstTplInfoPO", EqLstTplInfoPO.class);
        return query.list();
    }

    /**
     * 根据ID删除EqLstTplInfoPO实体
     *
     * @param id 实体ID
     */
    public void deleteById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqLstTplInfoPO entity = session.get(EqLstTplInfoPO.class, id);
            if (entity != null) {
                session.delete(entity);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("删除实体失败", e);
        }
    }

    /**
     * 删除EqLstTplInfoPO实体
     *
     * @param entity 要删除的实体对象
     */
    public void delete(EqLstTplInfoPO entity) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.delete(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("删除实体失败", e);
        }
    }

    /**
     * 根据module查找EqLstTplInfoPO实体
     *
     * @param module 模块名称
     * @return EqLstTplInfoPO实体对象
     */
    public EqLstTplInfoPO findByModule(String module) {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplInfoPO> query = session.createQuery(
                "SELECT e FROM EqLstTplInfoPO e WHERE e.module = :module",
                EqLstTplInfoPO.class
        );
        query.setParameter("module", module);
        return query.uniqueResult();
    }
}
