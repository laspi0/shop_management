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
    // FXML fields from the new dashboard.fxml
    @FXML private Label salesTodayValue;
    @FXML private Label revenueValue;
    @FXML private Label outOfStockValue;
    @FXML private Label outOfStockLabel;
    
    @FXML private Button refreshBtn;
    @FXML private Button toggleThemeBtn;
    @FXML private Button logoutBtn;

    // Navigation buttons
    @FXML private Button productsBtn;
    @FXML private Button customersBtn;
    @FXML private Button salesBtn;
    @FXML private Button usersBtn;
    @FXML private Button salesHistoryBtn;

    private final SaleService saleService = new SaleService();
    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        // Load statistics
        loadStats();

        // Top bar actions
        if (refreshBtn != null) refreshBtn.setOnAction(e -> loadStats());
        if (toggleThemeBtn != null) toggleThemeBtn.setOnAction(e -> SceneManager.toggleTheme());
        if (logoutBtn != null) logoutBtn.setOnAction(e -> SceneManager.navigate("view/login.fxml"));

        // Navigation actions
        if (productsBtn != null) productsBtn.setOnAction(e -> SceneManager.navigate("view/products.fxml"));
        if (customersBtn != null) customersBtn.setOnAction(e -> SceneManager.navigate("view/customers.fxml"));
        if (salesBtn != null) salesBtn.setOnAction(e -> SceneManager.navigate("view/sales.fxml"));
        if (salesHistoryBtn != null) salesHistoryBtn.setOnAction(e -> SceneManager.navigate("view/sales_history.fxml"));
        if (usersBtn != null) usersBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Module Utilisateurs à venir").showAndWait());

        // Role-based UI: hide Users button for CASHIER role
        User currentUser = SceneManager.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != null) {
            String roleName = currentUser.getRole().getName();
            if ("CASHIER".equalsIgnoreCase(roleName)) {
                if (usersBtn != null && usersBtn.getParent() != null) {
                    usersBtn.getParent().setVisible(false);
                    usersBtn.getParent().setManaged(false);
                }
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

        // Update the out of stock status label
        if (outOfStockLabel != null) {
            if (outOfStock > 0) {
                outOfStockLabel.setText("⚠️ Action requise");
                // Assuming 'warning' style is defined in CSS
                outOfStockLabel.getStyleClass().setAll("stats-change");
                 if(outOfStockLabel.getParent() != null) outOfStockLabel.getParent().getStyleClass().setAll("stats-card", "warning");

            } else {
                outOfStockLabel.setText("✅ Stock OK");
                // Assuming 'success' style is defined in CSS
                outOfStockLabel.getStyleClass().setAll("stats-change");
                if(outOfStockLabel.getParent() != null) outOfStockLabel.getParent().getStyleClass().setAll("stats-card", "success");
            }
        }
    }
}
