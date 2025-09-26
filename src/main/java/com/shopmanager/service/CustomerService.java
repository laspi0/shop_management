package com.shopmanager.service;

import com.shopmanager.model.Customer;
import com.shopmanager.repository.CustomerRepository;

import java.util.List;

public class CustomerService {
    private final CustomerRepository repo = new CustomerRepository();

    public List<Customer> findAll() { return repo.findAll(); }
    public List<Customer> searchByName(String q) { return (q == null || q.isBlank()) ? findAll() : repo.searchByName(q); }
    public void save(Customer c) { repo.save(c); }
    public void update(Customer c) { repo.update(c); }
    public void delete(Customer c) { repo.delete(c); }
}
