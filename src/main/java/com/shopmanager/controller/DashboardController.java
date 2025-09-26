package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

public class DashboardController {
    @FXML private Label salesTodayLabel;
    @FXML private Label revenueLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Button productsBtn;
    @FXML private Button customersBtn;
    @FXML private Button salesBtn;
    @FXML private Button usersBtn;
    @FXML private Button toggleThemeBtn;

    @FXML
    public void initialize() {
        salesTodayLabel.setText("Ventes du jour: 0");
        revenueLabel.setText("Chiffre d'affaires: 0.00 €");
        outOfStockLabel.setText("Produits en rupture: 0");
        toggleThemeBtn.setOnAction(e -> SceneManager.toggleTheme());

        productsBtn.setOnAction(e -> SceneManager.navigate("view/products.fxml"));
        customersBtn.setOnAction(e -> SceneManager.navigate("view/customers.fxml"));
        salesBtn.setOnAction(e -> SceneManager.navigate("view/sales.fxml"));
        usersBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Module Utilisateurs à venir").showAndWait());
    }
}
