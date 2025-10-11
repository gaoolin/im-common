package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
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
    private final SessionFactory sessionFactory;

    public EqLstTplInfoRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // 保存或更新
    public void save(EqLstTplInfoDO entity) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate(entity);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("保存实体失败", e);
        }
    }

    // 根据 ID 查询
    public EqLstTplInfoDO findById(Long id) {
        return sessionFactory.getCurrentSession().get(EqLstTplInfoDO.class, id);
    }

    // 查询所有
    public List<EqLstTplInfoDO> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM EqLstTplInfoDO", EqLstTplInfoDO.class).list();
    }

    // 根据 module 查询
    public EqLstTplInfoDO findByModule(String module) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            Query<EqLstTplInfoDO> query = session.createQuery(
                    "FROM EqLstTplInfoDO e WHERE e.module = :module",
                    EqLstTplInfoDO.class
            );
            query.setParameter("module", module);
            EqLstTplInfoDO result = query.uniqueResult();
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("查询失败", e);
        }
    }
}