package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import com.shopmanager.model.User;
import com.shopmanager.service.ProductService;
import com.shopmanager.service.SaleService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label salesTodayValue;
    @FXML private Label revenueValue;
    @FXML private Label outOfStockValue;
    @FXML private Label outOfStockLabel; // Ajout du label pour le statut
    @FXML private Button productsBtn;
    @FXML private Button customersBtn;
    @FXML private Button salesBtn;
    @FXML private Button usersBtn;
    @FXML private Button salesHistoryBtn;
    @FXML private Button toggleThemeBtn;

    private final SaleService saleService = new SaleService();
    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadStats();
        toggleThemeBtn.setOnAction(e -> SceneManager.toggleTheme());

        productsBtn.setOnAction(e -> SceneManager.navigate("view/products.fxml"));
        customersBtn.setOnAction(e -> SceneManager.navigate("view/customers.fxml"));
        salesBtn.setOnAction(e -> SceneManager.navigate("view/sales.fxml"));
        usersBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Module Utilisateurs à venir").showAndWait());
        if (salesHistoryBtn != null) salesHistoryBtn.setOnAction(e -> SceneManager.navigate("view/sales_history.fxml"));

        // Role-based UI: hide Users for CASHIER
        User u = SceneManager.getCurrentUser();
        if (u != null && u.getRole() != null) {
            String role = u.getRole().getName();
            if ("CASHIER".equalsIgnoreCase(role)) {
                usersBtn.setVisible(false);
                usersBtn.setManaged(false);
            }
        }
    }

    private void loadStats() {
        long salesToday = saleService.getSalesToday();
        double revenueToday = saleService.getRevenueToday();
        long outOfStock = productService.countOutOfStock();

        if (salesTodayValue != null) salesTodayValue.setText(String.valueOf(salesToday));
        if (revenueValue != null) revenueValue.setText(String.format("%.2f MRU", revenueToday));
        if (outOfStockValue != null) outOfStockValue.setText(String.valueOf(outOfStock));

        // Mise à jour du label de statut des stocks
        if (outOfStockLabel != null) {
            if (outOfStock > 0) {
                outOfStockLabel.setText("Action requise");
                outOfStockLabel.getStyleClass().setAll("stats-change", "negative");
            } else {
                outOfStockLabel.setText("Aucune action requise");
                outOfStockLabel.getStyleClass().setAll("stats-change", "positive");
            }
        }
    }
}