package com.shopmanager.repository;

import com.shopmanager.core.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.function.Consumer;

public abstract class BaseRepository<T> {
    protected void runInTransaction(Consumer<Session> work) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            work.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    protected <R> R get(FunctionWithSession<R> work) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return work.apply(session);
        }
    }

    @FunctionalInterface
    protected interface FunctionWithSession<R> {
        R apply(Session session);
    }
}
