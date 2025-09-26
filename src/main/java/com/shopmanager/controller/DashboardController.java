package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import com.shopmanager.model.Product;
import com.shopmanager.model.Sale;
import com.shopmanager.model.User;
import com.shopmanager.service.ProductService;
import com.shopmanager.service.SaleService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {
    // FXML fields from the new dashboard.fxml
    @FXML private Label salesTodayValue;
    @FXML private Label revenueValue;
    @FXML private Label outOfStockValue;
    @FXML private Label outOfStockLabel;

    @FXML private VBox recentSalesVBox;
    @FXML private VBox stockAlertsVBox;
    @FXML private HBox performanceChartHBox;
    @FXML private VBox topProductsVBox;
    
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
        loadRecentSales();
        loadStockAlerts();
        loadWeeklyPerformance();
        loadTopProducts();

        // Top bar actions
        if (refreshBtn != null) refreshBtn.setOnAction(e -> {
            loadStats();
            loadRecentSales();
            loadStockAlerts();
            loadWeeklyPerformance();
            loadTopProducts();
        });
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

        if (salesTodayValue != null) salesTodayValue.setText(String.valueOf(outOfStock));
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

    private void loadRecentSales() {
        if (recentSalesVBox == null) return;
        recentSalesVBox.getChildren().clear(); // Clear existing static content

        List<Sale> recentSales = saleService.findRecentSales(5); // Get last 5 sales
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Sale sale : recentSales) {
            HBox saleEntry = new HBox(10);
            saleEntry.setAlignment(Pos.CENTER_LEFT);

            Circle circle = new Circle(4);
            circle.setStyle("-fx-fill: #10b981;"); // Green color for sales

            VBox details = new VBox(2);
            details.setPrefWidth(0); // Allow HBox.hgrow to work
            HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

            Label saleIdLabel = new Label("Vente #" + sale.getId());
            saleIdLabel.getStyleClass().add("body");
            saleIdLabel.setStyle("-fx-font-weight: 500;");

            String customerName = sale.getCustomer() != null ? sale.getCustomer().getName() : "Client comptoir";
            Label saleDetailsLabel = new Label("Client: " + customerName + " - " + String.format("%.2f MRU", sale.getTotal()));
            saleDetailsLabel.getStyleClass().add("caption");

            details.getChildren().addAll(saleIdLabel, saleDetailsLabel);

            Label timeLabel = new Label(sale.getDateTime().format(formatter));
            timeLabel.getStyleClass().add("caption");

            saleEntry.getChildren().addAll(circle, details, timeLabel);
            recentSalesVBox.getChildren().add(saleEntry);
        }
    }

    private void loadStockAlerts() {
        if (stockAlertsVBox == null) return;
        stockAlertsVBox.getChildren().clear(); // Clear existing static content

        List<Product> lowStockProducts = productService.findLowStockProducts(5); // Products with 5 or less in stock

        for (Product product : lowStockProducts) {
            HBox alertEntry = new HBox(10);
            alertEntry.setAlignment(Pos.CENTER_LEFT);

            Circle circle = new Circle(4);
            String circleColor = product.getQuantity() == 0 ? "-fx-fill: #ef4444;" : "-fx-fill: #f59e0b;"; // Red for out of stock, orange for low stock
            circle.setStyle(circleColor);

            VBox details = new VBox(2);
            details.setPrefWidth(0); // Allow HBox.hgrow to work
            HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

            Label productNameLabel = new Label(product.getName());
            productNameLabel.getStyleClass().add("body");
            productNameLabel.setStyle("-fx-font-weight: 500;");

            Label stockDetailsLabel = new Label("Stock: " + product.getQuantity() + " unités");
            stockDetailsLabel.getStyleClass().add("caption");
            stockDetailsLabel.setStyle(circleColor); // Apply color to stock details

            details.getChildren().addAll(productNameLabel, stockDetailsLabel);

            Label statusLabel = new Label(product.getQuantity() == 0 ? "Rupture" : "Faible");
            statusLabel.getStyleClass().add("caption");
            statusLabel.setStyle(circleColor + "; -fx-font-weight: 600;");

            alertEntry.getChildren().addAll(circle, details, statusLabel);
            stockAlertsVBox.getChildren().add(alertEntry);
        }
    }

    private void loadWeeklyPerformance() {
        if (performanceChartHBox == null) return;
        performanceChartHBox.getChildren().clear(); // Clear existing static content

        Map<LocalDate, Double> salesPerDay = saleService.getSalesPerDayForLastWeek();

        // Find max sales value for scaling
        double maxSales = salesPerDay.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);

        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE"); // Mon, Tue, etc.

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double sales = salesPerDay.getOrDefault(date, 0.0);

            VBox barContainer = new VBox(4);
            barContainer.setAlignment(Pos.BOTTOM_CENTER);
            barContainer.setPrefWidth(30); // Fixed width for bars

            Rectangle bar = new Rectangle(30, (sales / maxSales) * 90); // Max height 90
            bar.setArcWidth(6);
            bar.setArcHeight(6);
            bar.setFill(new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#667eea")),
                    new Stop(1, Color.web("#764ba2"))
            ));

            Label dayLabel = new Label(date.format(dayFormatter));
            dayLabel.getStyleClass().add("caption");

            barContainer.getChildren().addAll(bar, dayLabel);
            performanceChartHBox.getChildren().add(barContainer);
        }
    }

    private void loadTopProducts() {
        if (topProductsVBox == null) return;
        topProductsVBox.getChildren().clear(); // Clear existing static content

        List<Product> topProducts = productService.findTopSellingProducts(3); // Get top 3 products

        for (int i = 0; i < topProducts.size(); i++) {
            Product product = topProducts.get(i);

            HBox productEntry = new HBox(12);
            productEntry.setAlignment(Pos.CENTER_LEFT);

            Circle rankCircle = new Circle(12);
            Label rankLabel = new Label(String.valueOf(i + 1));
            rankLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 12px;");
            rankLabel.setAlignment(Pos.CENTER);
            rankLabel.setPrefSize(24, 24); // Make label cover the circle

            // Set circle color based on rank
            if (i == 0) {
                rankCircle.setStyle("-fx-fill: linear-gradient(135deg, #ffd700, #ffb347);"); // Gold
            } else if (i == 1) {
                rankCircle.setStyle("-fx-fill: linear-gradient(135deg, #c0c0c0, #9e9e9e);"); // Silver
            } else if (i == 2) {
                rankCircle.setStyle("-fx-fill: linear-gradient(135deg, #cd7f32, #a0522d);"); // Bronze
            }

            StackPane rankPane = new StackPane(rankCircle, rankLabel);

            VBox details = new VBox(2);
            details.setPrefWidth(0); // Allow HBox.hgrow to work
            HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

            Label productNameLabel = new Label(product.getName());
            productNameLabel.getStyleClass().add("body");
            productNameLabel.setStyle("-fx-font-weight: 600;");

            // This part is tricky as SaleItem is not directly available here.
            // For simplicity, I'll just show "Stock: X units" or similar.
            // A more accurate "X sales - Y€" would require more complex data aggregation in ProductService.
            Label productDetailsLabel = new Label("Stock: " + product.getQuantity() + " unités");
            productDetailsLabel.getStyleClass().add("caption");
            productDetailsLabel.setStyle("-fx-text-fill: #10b981;"); // Green color

            details.getChildren().addAll(productNameLabel, productDetailsLabel);

            productEntry.getChildren().addAll(rankPane, details);
            topProductsVBox.getChildren().add(productEntry);
        }
    }
}
