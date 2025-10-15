package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/10
 */
public class TemplateDataRepository {
    private SessionFactory sessionFactory;

    public TemplateDataRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // EqLstTplDO CRUD 操作
    public void saveEqLstTplDO(EqLstTplDO tplDO) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(tplDO);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    public EqLstTplDO findEqLstTplDOById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqLstTplDO.class, id);
    }

    public List<EqLstTplDO> findAllEqLstTplDO() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplDO> query = session.createQuery("FROM EqLstTplDO", EqLstTplDO.class);
        return query.list();
    }

    public void deleteEqLstTplDO(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqLstTplDO tplDO = session.get(EqLstTplDO.class, id);
            if (tplDO != null) {
                session.delete(tplDO);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    // EqLstTplInfoDO CRUD 操作
    public void saveEqLstTplInfoPO(EqLstTplInfoDO tplInfoPO) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(tplInfoPO);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    public EqLstTplInfoDO findEqLstTplInfoPOById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqLstTplInfoDO.class, id);
    }

    public List<EqLstTplInfoDO> findAllEqLstTplInfoPO() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplInfoDO> query = session.createQuery("FROM EqLstTplInfoDO", EqLstTplInfoDO.class);
        return query.list();
    }

    public void deleteEqLstTplInfoPO(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqLstTplInfoDO tplInfoPO = session.get(EqLstTplInfoDO.class, id);
            if (tplInfoPO != null) {
                session.delete(tplInfoPO);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    // EqLstTplDO 根据 tplInfo.module 查询
    public EqLstTplDO findByModule(String module) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            Query<EqLstTplDO> query = session.createQuery(
                    "SELECT t FROM EqLstTplDO t JOIN t.tplInfo i WHERE i.module = :module",
                    EqLstTplDO.class
            );
            query.setParameter("module", module);
            EqLstTplDO result = query.uniqueResult();
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("查询失败", e);
        }
    }
}
