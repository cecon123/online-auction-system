package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.common.enums.Role;
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
        String username =
            usernameField.getText() == null
                ? ""
                : usernameField.getText().trim();
        String password =
            passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError("Please enter username and password.");
            return;
        }

        Role mockRole = detectMockRole(username);
        SceneManager.showAppShell(mockRole, username);
    }

    @FXML
    private void handleCreateAccount() {
        SceneManager.showRegister();
    }

    private Role detectMockRole(String username) {
        String normalized = username.toLowerCase();

        if (normalized.contains("admin")) {
            return Role.ADMIN;
        }

        if (normalized.contains("seller")) {
            return Role.SELLER;
        }

        return Role.BIDDER;
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
