package com.shopmanager.service;

import com.shopmanager.model.Product;
import com.shopmanager.model.Sale;
import com.shopmanager.model.SaleItem;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository repo = new ProductRepository();
    private final SaleRepository saleRepo = new SaleRepository();

    public List<Product> findAll() { return repo.findAll(); }
    public List<Product> search(String q) { return (q == null || q.isBlank()) ? findAll() : repo.searchByName(q); }
    public void save(Product p) { repo.save(p); }
    public void update(Product p) { repo.update(p); }
    public void delete(Product p) { repo.delete(p); }

    public long countOutOfStock() {
        return repo.countOutOfStock();
    }

    public List<Product> findLowStockProducts(int threshold) {
        return repo.findAll().stream()
                .filter(p -> p.getQuantity() <= threshold)
                .sorted(Comparator.comparing(Product::getQuantity))
                .collect(Collectors.toList());
    }

    public List<Product> findTopSellingProducts(int limit) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return saleRepo.findAll().stream()
                .filter(sale -> !sale.getDateTime().toLocalDate().isBefore(sevenDaysAgo))
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(saleItem -> saleItem.getProduct(),
                        Collectors.summingInt(SaleItem::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}