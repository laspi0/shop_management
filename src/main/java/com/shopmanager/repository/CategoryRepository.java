package com.shopmanager.repository;

import com.shopmanager.model.Category;
import org.hibernate.Session;

import java.util.List;

public class CategoryRepository extends BaseRepository<Category> {
    public void save(Category c) { runInTransaction(s -> s.persist(c)); }
    public void delete(Category c) { runInTransaction(s -> s.remove(s.merge(c))); }
    public Category findById(Long id) { return get(s -> s.get(Category.class, id)); }
    public Category findByName(String name) { return get(s -> s.createQuery("from Category c where c.name = :n", Category.class).setParameter("n", name).uniqueResult()); }
    public List<Category> findAll() { return get((Session s) -> s.createQuery("from Category", Category.class).list()); }
}
