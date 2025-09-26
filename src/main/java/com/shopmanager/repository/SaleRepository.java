package com.shopmanager.repository;

import com.shopmanager.model.Sale;

public class SaleRepository extends BaseRepository<Sale> {
    public void save(Sale s) { runInTransaction(session -> session.persist(s)); }
}
