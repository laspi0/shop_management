package com.shopmanager.controller;

import com.shopmanager.model.Customer;
import com.shopmanager.model.Product;
import com.shopmanager.model.Sale;
import com.shopmanager.model.SaleItem;
import com.shopmanager.service.CustomerService;
import com.shopmanager.service.ProductService;
import com.shopmanager.service.ReportService;
import com.shopmanager.service.SaleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.application.Platform; // Added for Platform.runLater
import javafx.scene.control.*;
import javafx.scene.input.KeyCode; // Added for KeyCode
import javafx.stage.FileChooser;
import com.shopmanager.core.SceneManager;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SalesController {
    @FXML private Button backBtn;
    @FXML private ComboBox<String> customerCombo;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> pName;
    @FXML private TableColumn<Product, String> pPrice;
    @FXML private TableColumn<Product, Number> pQty;
    @FXML private Spinner<Integer> qtySpinner;
    @FXML private TextField barcodeScanField; // New FXML element for barcode input
    @FXML private Button addToCartBtn;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> cName;
    @FXML private TableColumn<SaleItem, Number> cQty;
    @FXML private TableColumn<SaleItem, String> cPrice;
    @FXML private Label vatLabel;
    @FXML private Label totalLabel;
    @FXML private Button removeCartItemBtn; // New FXML element for removing cart item
    @FXML private Button finalizeBtn;

    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final SaleService saleService = new SaleService();
    private final ReportService reportService = new ReportService();

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> cart = FXCollections.observableArrayList();
    private Sale sale;

    @FXML
    public void initialize() {
        if (backBtn != null) backBtn.setOnAction(e -> SceneManager.navigate("view/dashboard.fxml"));
        sale = new Sale();

        pName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        pPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f MRU", c.getValue().getPrice())));
        pQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()));
        productsTable.setItems(products);
        products.setAll(productService.findAll());

        cName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProduct().getName()));
        cQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()));
        cPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f MRU", c.getValue().getUnitPrice()*c.getValue().getQuantity())));
        cartTable.setItems(cart);

        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        addToCartBtn.setOnAction(e -> addSelectedProduct());
        removeCartItemBtn.setOnAction(e -> removeSelectedCartItem()); // Add listener for remove button
        finalizeBtn.setOnAction(e -> finalizeSale());

        customerCombo.getItems().clear();
        customerCombo.getItems().add("Client comptoir");
        customerService.findAll().forEach(c -> customerCombo.getItems().add(c.getName()));
        customerCombo.setValue("Client comptoir");

        // Barcode scanner integration
        if (barcodeScanField != null) {
            barcodeScanField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String barcode = barcodeScanField.getText().trim();
                    if (!barcode.isEmpty()) {
                        handleBarcodeScan(barcode);
                    }
                    barcodeScanField.clear(); // Clear the field after processing
                    // Focus will be requested again below
                }
            });
            // Request focus for the barcode field after the scene is shown
            Platform.runLater(() -> barcodeScanField.requestFocus());
        }

        updateTotals();
    }

    private void handleBarcodeScan(String barcode) {
        Product p = productService.findByBarcode(barcode);
        if (p == null) {
            new Alert(Alert.AlertType.WARNING, "Produit avec code-barres " + barcode + " non trouvé.").showAndWait();
            return;
        }

        // Check if product is already in cart to increment quantity
        boolean foundInCart = false;
        for (SaleItem item : sale.getItems()) { // Iterate over sale.getItems() directly
            if (item.getProduct().getId().equals(p.getId())) {
                if (item.getQuantity() + 1 > p.getQuantity()) {
                    new Alert(Alert.AlertType.WARNING, "Quantité demandée supérieure au stock disponible pour " + p.getName()).showAndWait();
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                foundInCart = true;
                break;
            }
        }

        if (!foundInCart) {
            // Use saleService.addItem for new products to ensure totals are recalculated
            saleService.addItem(sale, p, 1); // Add 1 quantity
        } else {
            // If quantity was incremented, recalculate totals manually
            saleService.recalcTotals(sale);
        }

        cart.setAll(sale.getItems()); // Refresh observable list from sale's internal list
        updateTotals();
    }

    private void removeSelectedCartItem() {
        SaleItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un article à supprimer du panier.").showAndWait();
            return;
        }
        saleService.removeItem(sale, selectedItem);
        cart.setAll(sale.getItems()); // Refresh observable list from sale's internal list
        updateTotals();
    }

    private void addSelectedProduct() {
        Product p = productsTable.getSelectionModel().getSelectedItem();
        if (p == null) return;
        int qty = qtySpinner.getValue();
        if (qty > p.getQuantity()) {
            new Alert(Alert.AlertType.WARNING, "Quantité demandée supérieure au stock disponible").showAndWait();
            return;
        }
        saleService.addItem(sale, p, qty);
        cart.setAll(sale.getItems());
        updateTotals();
    }

    private void updateTotals() {
        vatLabel.setText(String.format("TVA: %.2f MRU", sale.getVat()));
        totalLabel.setText(String.format("Total: %.2f MRU", sale.getTotal()));
    }

    private void finalizeSale() {
        String selectedCustomer = customerCombo.getValue();
        if (selectedCustomer != null && !"Client comptoir".equals(selectedCustomer)) {
            Customer c = customerService.findAll().stream().filter(cc -> selectedCustomer.equals(cc.getName())).findFirst().orElse(null);
            sale.setCustomer(c);
        }
        if (sale.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Le panier est vide").showAndWait();
            return;
        }
        saleService.finalizeAndSave(sale);
        // refresh products after saving to avoid stale session
        products.setAll(productService.findAll());
        // Generate PDF invoice
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer la facture PDF");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            String suggested = buildInvoiceFileName(sale);
            chooser.setInitialFileName(suggested);
            File target = chooser.showSaveDialog(finalizeBtn.getScene().getWindow());
            if (target != null) {
                reportService.generateInvoice(sale, target);
                new Alert(Alert.AlertType.INFORMATION, "Vente finalisée et facture générée").showAndWait();
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération de la facture").showAndWait();
        }
        // reset sale
        sale = new Sale();
        cart.clear();
        updateTotals();
    }

    private String buildInvoiceFileName(Sale sale) {
        // Date/heure Paris
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        String ts = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        // ID de vente (zero-pad 6)
        long id = sale.getId() == null ? 0L : sale.getId();
        String idStr = String.format("%06d", id);
        // Client slug
        String clientName = sale.getCustomer() != null && sale.getCustomer().getName() != null
                ? sale.getCustomer().getName() : "Client comptoir";
        String slug = slugify(clientName);
        return String.format("Facture_%s_%s_%s.pdf", ts, idStr, slug);
    }

    private String slugify(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-S", "");
        if (n.isBlank()) n = "facture";
        if (n.length() > 50) n = n.substring(0, 50);
        return n;
    }
}