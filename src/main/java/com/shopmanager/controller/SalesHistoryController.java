package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import com.shopmanager.model.Sale;
import com.shopmanager.service.CustomerService;
import com.shopmanager.service.SaleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class SalesHistoryController {
    @FXML private Button backBtn;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> customerFilter;
    @FXML private Button resetFiltersBtn;

    @FXML private TableView<Sale> table;
    @FXML private TableColumn<Sale, Number> colId;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, String> colCustomer;
    @FXML private TableColumn<Sale, Number> colItems;
    @FXML private TableColumn<Sale, Number> colVat;
    @FXML private TableColumn<Sale, Number> colTotal;

    private final SaleService saleService = new SaleService();
    private final CustomerService customerService = new CustomerService();

    private final ObservableList<Sale> sales = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        if (backBtn != null) backBtn.setOnAction(e -> SceneManager.navigate("view/dashboard.fxml"));

        // Table columns
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(dtf.format(c.getValue().getDateTime())));
        colCustomer.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCustomer() != null ? c.getValue().getCustomer().getName() : "Client comptoir"
        ));
        colItems.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getItems() != null ? c.getValue().getItems().size() : 0
        ));
        colVat.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getVat()));
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotal()));

        table.setItems(sales);

        // Filters setup
        customerFilter.getItems().clear();
        customerFilter.getItems().add("");
        customerFilter.getItems().addAll(
                customerService.findAll().stream().map(c -> c.getName()).collect(Collectors.toList())
        );
        customerFilter.setEditable(true);

        // Listeners
        fromDate.valueProperty().addListener((obs, o, n) -> reload());
        toDate.valueProperty().addListener((obs, o, n) -> reload());
        customerFilter.valueProperty().addListener((obs, o, n) -> reload());
        resetFiltersBtn.setOnAction(e -> {
            fromDate.setValue(null);
            toDate.setValue(null);
            customerFilter.setValue("");
            reload();
        });

        // Initial load
        reload();
    }

    private void reload() {
        LocalDate from = fromDate != null ? fromDate.getValue() : null;
        LocalDate to = toDate != null ? toDate.getValue() : null;
        String customer = customerFilter != null ? customerFilter.getValue() : null;
        if (customer != null && customer.isBlank()) customer = null;
        try {
            if (from == null && to == null && (customer == null || customer.isBlank())) {
                sales.setAll(saleService.findAll());
            } else {
                sales.setAll(saleService.findByFilters(from, to, customer));
            }
        } catch (Exception e) {
            sales.clear();
        }
    }
}
