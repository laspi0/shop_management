package com.shopmanager.service;

import com.shopmanager.model.Product;
import com.shopmanager.repository.ProductRepository;

import java.util.List;

public class ProductService {
    private final ProductRepository repo = new ProductRepository();

    public List<Product> findAll() { return repo.findAll(); }
    public List<Product> search(String q) { return (q == null || q.isBlank()) ? findAll() : repo.searchByName(q); }
    public void save(Product p) { repo.save(p); }
    public void update(Product p) { repo.update(p); }
    public void delete(Product p) { repo.delete(p); }
}
