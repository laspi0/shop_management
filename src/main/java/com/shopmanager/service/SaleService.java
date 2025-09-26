package com.shopmanager.service;

import com.shopmanager.core.AppConfig;
import com.shopmanager.model.Product;
import com.shopmanager.model.Sale;
import com.shopmanager.model.SaleItem;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;

import java.util.ArrayList;
import java.util.List;

public class SaleService {
    private final SaleRepository saleRepo = new SaleRepository();
    private final ProductRepository productRepo = new ProductRepository();

    public void addItem(Sale sale, Product p, int qty) {
        if (qty <= 0) return;
        SaleItem it = new SaleItem();
        it.setSale(sale);
        it.setProduct(p);
        it.setQuantity(qty);
        it.setUnitPrice(p.getPrice());
        sale.getItems().add(it);
        recalcTotals(sale);
    }

    public void removeItem(Sale sale, SaleItem it) {
        sale.getItems().remove(it);
        recalcTotals(sale);
    }

    public void recalcTotals(Sale sale) {
        double subtotal = sale.getItems().stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        double vatRate = AppConfig.getDouble("app.vat", 0.2);
        double vat = subtotal * vatRate;
        sale.setVat(vat);
        sale.setTotal(subtotal + vat);
    }

    public void finalizeAndSave(Sale sale) {
        // decrease stock
        List<Product> toUpdate = new ArrayList<>();
        for (SaleItem it : sale.getItems()) {
            Product p = it.getProduct();
            p.setQuantity(Math.max(0, p.getQuantity() - it.getQuantity()));
            toUpdate.add(p);
        }
        saleRepo.save(sale);
        toUpdate.forEach(productRepo::update);
    }
}
