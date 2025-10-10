package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/10
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

    // EqLstTplInfoPO CRUD 操作
    public void saveEqLstTplInfoPO(EqLstTplInfoPO tplInfoPO) {
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

    public EqLstTplInfoPO findEqLstTplInfoPOById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqLstTplInfoPO.class, id);
    }

    public List<EqLstTplInfoPO> findAllEqLstTplInfoPO() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplInfoPO> query = session.createQuery("FROM EqLstTplInfoPO", EqLstTplInfoPO.class);
        return query.list();
    }

    public void deleteEqLstTplInfoPO(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqLstTplInfoPO tplInfoPO = session.get(EqLstTplInfoPO.class, id);
            if (tplInfoPO != null) {
                session.delete(tplInfoPO);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }


    public EqLstTplDO findByModule(String module) {
        Session session = sessionFactory.getCurrentSession();
        Query<EqLstTplDO> query = session.createQuery("FROM EqLstTplDO WHERE module = :module", EqLstTplDO.class);
        return null;
    }
}
