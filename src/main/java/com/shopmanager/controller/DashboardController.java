package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import com.shopmanager.model.User;

public class DashboardController {
    @FXML private Label salesTodayValue;
    @FXML private Label revenueValue;
    @FXML private Label outOfStockValue;
    @FXML private Button productsBtn;
    @FXML private Button customersBtn;
    @FXML private Button salesBtn;
    @FXML private Button usersBtn;
    @FXML private Button toggleThemeBtn;

    @FXML
    public void initialize() {
        // Initial demo values (could be wired to services later)
        if (salesTodayValue != null) salesTodayValue.setText("0");
        if (revenueValue != null) revenueValue.setText("0.00€");
        if (outOfStockValue != null) outOfStockValue.setText("0");
        toggleThemeBtn.setOnAction(e -> SceneManager.toggleTheme());

        productsBtn.setOnAction(e -> SceneManager.navigate("view/products.fxml"));
        customersBtn.setOnAction(e -> SceneManager.navigate("view/customers.fxml"));
        salesBtn.setOnAction(e -> SceneManager.navigate("view/sales.fxml"));
        usersBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Module Utilisateurs à venir").showAndWait());

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
}
