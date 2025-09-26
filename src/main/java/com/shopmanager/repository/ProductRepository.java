package com.shopmanager.repository;

import com.shopmanager.model.Product;
import org.hibernate.Session;

import java.util.List;

public class ProductRepository extends BaseRepository<Product> {
    public void save(Product p) { runInTransaction(s -> s.persist(p)); }
    public void update(Product p) { runInTransaction(s -> s.merge(p)); }
    public void delete(Product p) { runInTransaction(s -> s.remove(s.merge(p))); }
    public Product findById(Long id) { return get(s -> s.get(Product.class, id)); }
    public List<Product> findAll() { return get((Session s) -> s.createQuery("from Product", Product.class).list()); }
    public List<Product> searchByName(String q) {
        return get((Session s) -> s.createQuery("from Product p where lower(p.name) like :q", Product.class)
                .setParameter("q", "%" + q.toLowerCase() + "%")
                .list());
    }

    public long countOutOfStock() {
        return get(s -> s.createQuery("select count(p) from Product p where p.quantity = 0", Long.class).getSingleResult());
    }
}
