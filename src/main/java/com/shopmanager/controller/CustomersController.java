package com.shopmanager.controller;

import com.shopmanager.model.Customer;
import com.shopmanager.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.shopmanager.core.SceneManager;

import java.util.List;

public class CustomersController {
    @FXML private Button backBtn;
    @FXML private TextField searchField;
    @FXML private TableView<Customer> table;
    @FXML private TableColumn<Customer, Long> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private final CustomerService service = new CustomerService();
    private final ObservableList<Customer> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (backBtn != null) backBtn.setOnAction(e -> SceneManager.navigate("view/dashboard.fxml"));
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));
        table.setItems(items);

        refresh();

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        addBtn.setOnAction(e -> onAdd());
        editBtn.setOnAction(e -> onEdit());
        deleteBtn.setOnAction(e -> onDelete());
    }

    private void refresh() {
        try { items.setAll(service.findAll()); } catch (Exception e) { items.clear(); }
    }

    private void applyFilter() {
        String q = searchField.getText();
        List<Customer> filtered = service.searchByName(q);
        items.setAll(filtered);
    }

    private void onAdd() {
        Customer c = new Customer();
        if (showEditorDialog(c)) { service.save(c); refresh(); }
    }

    private void onEdit() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (showEditorDialog(selected)) { service.update(selected); refresh(); }
    }

    private void onDelete() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le client?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) { service.delete(selected); refresh(); }
        });
    }

    private boolean showEditorDialog(Customer c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(c.getId()==null?"Ajouter un client":"Modifier un client");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField(c.getName()); name.setPromptText("Nom complet");
        TextField phone = new TextField(c.getPhone()); phone.setPromptText("Téléphone");

        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, new Label("Nom:"), name);
        grid.addRow(1, new Label("Téléphone:"), phone);
        pane.setContent(grid);

        dialog.showAndWait();
        if (dialog.getResult() == ButtonType.OK) {
            c.setName(name.getText());
            c.setPhone(phone.getText());
            return true;
        }
        return false;
    }
}
