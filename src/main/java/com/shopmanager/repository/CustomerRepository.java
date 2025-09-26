package com.shopmanager.repository;

import com.shopmanager.model.Customer;
import org.hibernate.Session;

import java.util.List;

public class CustomerRepository extends BaseRepository<Customer> {
    public void save(Customer c) { runInTransaction(s -> s.persist(c)); }
    public void update(Customer c) { runInTransaction(s -> s.merge(c)); }
    public void delete(Customer c) { runInTransaction(s -> s.remove(s.merge(c))); }
    public Customer findById(Long id) { return get(s -> s.get(Customer.class, id)); }
    public List<Customer> findAll() { return get((Session s) -> s.createQuery("from Customer", Customer.class).list()); }
    public List<Customer> searchByName(String q) {
        return get((Session s) -> s.createQuery("from Customer c where lower(c.name) like :q", Customer.class)
                .setParameter("q", "%" + q.toLowerCase() + "%")
                .list());
    }
}
