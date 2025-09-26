package com.shopmanager.controller;

import com.shopmanager.model.Category;
import com.shopmanager.model.Product;
import com.shopmanager.service.CategoryService;
import com.shopmanager.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.shopmanager.core.SceneManager;

import java.util.List;
import java.util.stream.Collectors;

public class ProductsController {
    @FXML private Button backBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, Long> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Number> colPrice;
    @FXML private TableColumn<Product, Number> colQty;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    private final ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (backBtn != null) backBtn.setOnAction(e -> SceneManager.navigate("view/dashboard.fxml"));
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCategory() != null ? c.getValue().getCategory().getName() : ""));
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()));
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()));
        table.setItems(products);

        loadCategories();
        refresh();

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        addBtn.setOnAction(e -> onAdd());
        editBtn.setOnAction(e -> onEdit());
        deleteBtn.setOnAction(e -> onDelete());
    }

    private void loadCategories() {
        List<String> cats = categoryService.findAll().stream().map(Category::getName).collect(Collectors.toList());
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("");
        categoryFilter.getItems().addAll(cats);
        categoryFilter.setValue("");
    }

    private void refresh() {
        try {
            products.setAll(productService.findAll());
        } catch (Exception e) {
            products.clear();
        }
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = categoryFilter.getValue();
        List<Product> source = productService.search(q);
        if (cat != null && !cat.isBlank()) {
            source = source.stream().filter(p -> p.getCategory() != null && cat.equals(p.getCategory().getName())).collect(Collectors.toList());
        }
        products.setAll(source);
    }

    private void onAdd() {
        Product p = new Product();
        if (showEditorDialog(p)) {
            productService.save(p);
            refresh();
        }
    }

    private void onEdit() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (showEditorDialog(selected)) {
            productService.update(selected);
            refresh();
        }
    }

    private void onDelete() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le produit?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                productService.delete(selected);
                refresh();
            }
        });
    }

    private boolean showEditorDialog(Product product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product.getId() == null ? "Ajouter un produit" : "Modifier un produit");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField(product.getName());
        name.setPromptText("Nom");
        TextField price = new TextField(String.valueOf(product.getPrice()));
        price.setPromptText("Prix");
        TextField qty = new TextField(String.valueOf(product.getQuantity()));
        qty.setPromptText("Quantité");
        ComboBox<String> cat = new ComboBox<>();
        cat.getItems().addAll(categoryService.findAll().stream().map(Category::getName).collect(Collectors.toList()));
        if (product.getCategory()!=null) cat.setValue(product.getCategory().getName());

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, new Label("Nom:"), name);
        grid.addRow(1, new Label("Prix:"), price);
        grid.addRow(2, new Label("Quantité:"), qty);
        grid.addRow(3, new Label("Catégorie:"), cat);
        pane.setContent(grid);

        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            product.setName(name.getText());
            try { product.setPrice(Double.parseDouble(price.getText())); } catch (Exception e) { product.setPrice(0); }
            try { product.setQuantity(Integer.parseInt(qty.getText())); } catch (Exception e) { product.setQuantity(0); }
            String catName = cat.getValue();
            if (catName != null) {
                Category c = categoryService.findByName(catName);
                if (c == null) { c = new Category(catName); categoryService.save(c); }
                product.setCategory(c);
            }
            return true;
        }
        return false;
    }
}
