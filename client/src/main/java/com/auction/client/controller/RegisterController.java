package com.auction.client.controller;

import com.auction.client.service.AuthClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.enums.Role;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
  private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
  private final AuthClientService authService = new AuthClientService();

  @FXML private ToggleButton bidderToggle;

  @FXML private ToggleButton sellerToggle;

  @FXML private TextField fullNameField;

  @FXML private TextField usernameField;

  @FXML private PasswordField passwordField;

  @FXML private PasswordField confirmPasswordField;

  @FXML private Label messageLabel;

  @FXML
  private void initialize() {
    bidderToggle.setSelected(true);
    messageLabel.setText("");
  }

  @FXML
  private void handleRegister() {
    String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
    String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
    String password = passwordField.getText() == null ? "" : passwordField.getText();
    String confirmPassword =
        confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

    if (fullName.isBlank()
        || username.isBlank()
        || password.isBlank()
        || confirmPassword.isBlank()) {
      setErrorMessage("Please fill all fields.");
      return;
    }

    if (!password.equals(confirmPassword)) {
      setErrorMessage("Passwords do not match.");
      return;
    }

    Role role = bidderToggle.isSelected() ? Role.BIDDER : Role.SELLER;
    RegisterRequest request = new RegisterRequest(fullName, username, password, role);

    // Ensure connected before registration
    if (!com.auction.client.socket.SocketClient.getInstance().isConnected()) {
      try {
        com.auction.client.socket.SocketClient.getInstance().connect();
      } catch (Exception e) {
        logger.error("Failed to connect before register", e);
        setErrorMessage("Could not connect to server.");
        return;
      }
    }

    authService
        .register(request)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      setSuccessMessage("Registration successful. Please go back to login.");
                    } else {
                      setErrorMessage(response.getMessage());
                    }
                  });
            })
        .exceptionally(
            ex -> {
              logger.error("Registration request failed", ex);
              Platform.runLater(() -> setErrorMessage("Connection error. Please try again later."));
              return null;
            });
  }

  @FXML
  private void handleBackToLogin() {
    SceneManager.showLogin();
  }

  private void setErrorMessage(String message) {
    messageLabel.setText(message);
    messageLabel.getStyleClass().removeAll("auth-success-message");
    if (!messageLabel.getStyleClass().contains("auth-error-message")) {
      messageLabel.getStyleClass().add("auth-error-message");
    }
  }

  private void setSuccessMessage(String message) {
    messageLabel.setText(message);
    messageLabel.getStyleClass().removeAll("auth-error-message");
    if (!messageLabel.getStyleClass().contains("auth-success-message")) {
      messageLabel.getStyleClass().add("auth-success-message");
    }
  }
}
