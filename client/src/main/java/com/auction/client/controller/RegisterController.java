package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class RegisterController {

    @FXML
    private ToggleButton bidderToggle;

    @FXML
    private ToggleButton sellerToggle;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        bidderToggle.setSelected(true);
        messageLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String fullName =
            fullNameField.getText() == null
                ? ""
                : fullNameField.getText().trim();
        String username =
            usernameField.getText() == null
                ? ""
                : usernameField.getText().trim();
        String password =
            passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword =
            confirmPasswordField.getText() == null
                ? ""
                : confirmPasswordField.getText();

        if (
            fullName.isBlank() ||
            username.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            messageLabel.setText("Please fill all fields.");
            messageLabel.getStyleClass().removeAll("auth-success-message");
            if (!messageLabel.getStyleClass().contains("auth-error-message")) {
                messageLabel.getStyleClass().add("auth-error-message");
            }
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.getStyleClass().removeAll("auth-success-message");
            if (!messageLabel.getStyleClass().contains("auth-error-message")) {
                messageLabel.getStyleClass().add("auth-error-message");
            }
            return;
        }

        String role = bidderToggle.isSelected() ? "BIDDER" : "SELLER";

        messageLabel.setText(
            "Mock register successful as " + role + ". Please go back to login."
        );
        messageLabel.getStyleClass().removeAll("auth-error-message");
        if (!messageLabel.getStyleClass().contains("auth-success-message")) {
            messageLabel.getStyleClass().add("auth-success-message");
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.showLogin();
    }
}
