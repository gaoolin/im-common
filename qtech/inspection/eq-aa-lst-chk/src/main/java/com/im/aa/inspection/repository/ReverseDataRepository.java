package com.im.aa.inspection.repository;

import com.im.aa.inspection.entity.reverse.EqpReverseRecord;
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
public class ReverseDataRepository {
    private SessionFactory sessionFactory;

    public ReverseDataRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // EqpReverseRecord CRUD 操作
    public void saveEqpReverseRecord(EqpReverseRecord record) {
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

    public EqpReverseRecord findEqpReverseRecordById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(EqpReverseRecord.class, id);
    }

    public List<EqpReverseRecord> findAllEqpReverseRecords() {
        Session session = sessionFactory.getCurrentSession();
        Query<EqpReverseRecord> query = session.createQuery("FROM EqpReverseRecord", EqpReverseRecord.class);
        return query.list();
    }

    public List<EqpReverseRecord> findEqpReverseRecordsBySimId(String simId) {
        Session session = sessionFactory.getCurrentSession();
        Query<EqpReverseRecord> query = session.createQuery(
                "FROM EqpReverseRecord WHERE simId = :simId", EqpReverseRecord.class);
        query.setParameter("simId", simId);
        return query.list();
    }

    public void deleteEqpReverseRecord(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            EqpReverseRecord record = session.get(EqpReverseRecord.class, id);
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
