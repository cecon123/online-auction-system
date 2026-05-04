package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private HBox errorBox;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        hideError();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError("Please enter username and password.");
            return;
        }

        showError("Mock login only. Socket integration will be added in W9.");
    }

    @FXML
    private void handleCreateAccount() {
        showError("Register screen will be connected next.");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }
}
