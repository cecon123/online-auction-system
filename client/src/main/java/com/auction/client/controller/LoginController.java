package com.auction.client.controller;

import com.auction.client.service.AuthClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auth.LoginResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
  private final AuthClientService authService = new AuthClientService();

  @FXML private TextField usernameField;

  @FXML private PasswordField passwordField;

  @FXML private HBox errorBox;

  @FXML private Label errorLabel;

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

    authService
        .login(username, password)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      LoginResponse data = response.getData();
                      SceneManager.showAppShell(
                          data.userId(),
                          data.role(),
                          data.username(),
                          data.balance(),
                          data.lockedBalance());
                    } else {
                      showError(response.getMessage());
                    }
                  });
            })
        .exceptionally(
            ex -> {
              logger.error("Login request failed", ex);
              Platform.runLater(() -> showError("Connection error. Please try again later."));
              return null;
            });
  }

  @FXML
  private void handleCreateAccount() {
    SceneManager.showRegister();
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
