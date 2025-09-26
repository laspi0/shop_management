package com.shopmanager.repository;

import com.shopmanager.model.Sale;
import org.hibernate.Session;

public class SaleRepository extends BaseRepository<Sale> {
    public void save(Sale s) { runInTransaction(session -> session.persist(s)); }

    public java.util.List<Sale> findAll() {
        return get(session -> session.createQuery("from Sale s order by s.dateTime desc", Sale.class).list());
    }

    public java.util.List<Sale> findByFilters(java.time.LocalDate from, java.time.LocalDate to, String customerName) {
        return get(session -> buildFilteredQuery(session, from, to, customerName).list());
    }

    private org.hibernate.query.Query<Sale> buildFilteredQuery(Session session, java.time.LocalDate from, java.time.LocalDate to, String customerName) {
        StringBuilder hql = new StringBuilder("from Sale s where 1=1");
        if (from != null) hql.append(" and s.dateTime >= :from");
        if (to != null) hql.append(" and s.dateTime < :toNext");
        if (customerName != null && !customerName.isBlank()) hql.append(" and lower(s.customer.name) like :cust");
        hql.append(" order by s.dateTime desc");
        var q = session.createQuery(hql.toString(), Sale.class);
        if (from != null) q.setParameter("from", from.atStartOfDay());
        if (to != null) q.setParameter("toNext", to.plusDays(1).atStartOfDay());
        if (customerName != null && !customerName.isBlank()) q.setParameter("cust", "%" + customerName.toLowerCase() + "%");
        return q;
    }
}
