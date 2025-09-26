package com.shopmanager.service;

import com.shopmanager.core.AppConfig;
import com.shopmanager.model.Product;
import com.shopmanager.model.Sale;
import com.shopmanager.model.SaleItem;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Sale> findAll() {
        return saleRepo.findAll();
    }

    public List<Sale> findByFilters(LocalDate from, LocalDate to, String customerName) {
        return saleRepo.findByFilters(from, to, customerName);
    }

    public long getSalesToday() {
        return saleRepo.countSalesToday();
    }

    public double getRevenueToday() {
        return saleRepo.sumRevenueToday();
    }

    public List<Sale> findRecentSales(int limit) {
        return saleRepo.findAll().stream()
                .sorted(Comparator.comparing(Sale::getDateTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<LocalDate, Double> getSalesPerDayForLastWeek() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Double> salesPerDay = new LinkedHashMap<>();

        // Initialize map with last 7 days and 0 sales
        for (int i = 6; i >= 0; i--) {
            salesPerDay.put(today.minusDays(i), 0.0);
        }

        saleRepo.findAll().stream()
                .filter(sale -> !sale.getDateTime().toLocalDate().isBefore(today.minusDays(6)))
                .forEach(sale -> {
                    LocalDate saleDate = sale.getDateTime().toLocalDate();
                    salesPerDay.computeIfPresent(saleDate, (date, currentTotal) -> currentTotal + sale.getTotal());
                });

        return salesPerDay;
    }
}
