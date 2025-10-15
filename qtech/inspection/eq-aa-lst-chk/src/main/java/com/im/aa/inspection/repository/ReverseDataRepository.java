package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.reverse.EqpReverseDO;
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
public class ReverseDataRepository {
    private SessionFactory sessionFactory;

    public ReverseDataRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // EqpReverseDO CRUD 操作
    public void saveEqpReverseRecord(EqpReverseDO record) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(record);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    public EqpReverseDO findEqpReverseRecordById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqpReverseDO.class, id);
    }

    public List<EqpReverseDO> findAllEqpReverseRecords() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqpReverseDO> query = session.createQuery("FROM EqpReverseDO", EqpReverseDO.class);
        return query.list();
    }

    public List<EqpReverseDO> findEqpReverseRecordsBySimId(String simId) {
        Session session = sessionFactory.getCurrentSession();
        Query<EqpReverseDO> query = session.createQuery(
                "FROM EqpReverseDO WHERE simId = :simId", EqpReverseDO.class);
        query.setParameter("simId", simId);
        return query.list();
    }

    public void deleteEqpReverseRecord(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqpReverseDO record = session.get(EqpReverseDO.class, id);
            if (record != null) {
                session.delete(record);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }
}
