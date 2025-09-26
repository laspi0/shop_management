package com.shopmanager.controller;

import com.shopmanager.core.SceneManager;
import com.shopmanager.model.User;
import com.shopmanager.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        // Ensure default users exist
        try {
            authService.ensureDefaultUsers();
        } catch (Exception e) {
            errorLabel.setText("Init utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onLogin(ActionEvent ignored) {
        String email = emailField.getText();
        String pass = passwordField.getText();
        try {
            User user = authService.login(email, pass);
            if (user == null) {
                // Retry after ensuring defaults (useful for first run)
                authService.ensureDefaultUsers();
                user = authService.login(email, pass);
            }
            if (user != null) {
                errorLabel.setText("");
                SceneManager.navigate("view/dashboard.fxml");
            } else {
                errorLabel.setText("Email ou mot de passe invalide");
            }
        } catch (Exception e) {
            errorLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
