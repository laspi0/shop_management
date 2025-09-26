package com.shopmanager.service;

import com.shopmanager.model.Category;
import com.shopmanager.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository repo = new CategoryRepository();

    public List<Category> findAll() { return repo.findAll(); }
    public Category findByName(String name) { return repo.findByName(name); }
    public void save(Category c) { repo.save(c); }
    public void delete(Category c) { repo.delete(c); }
}
