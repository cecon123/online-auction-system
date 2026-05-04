package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminPanelController {

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void handleExportReport() {
        messageLabel.setText("Mock report exported successfully.");
    }

    @FXML
    private void handleViewUser() {
        messageLabel.setText("Mock action: viewing user detail.");
    }

    @FXML
    private void handleDisableUser() {
        messageLabel.setText("Mock action: user disabled.");
    }

    @FXML
    private void handleViewAuction() {
        messageLabel.setText("Mock action: viewing auction detail.");
    }

    @FXML
    private void handleCancelAuction() {
        messageLabel.setText("Mock action: auction canceled.");
    }
}
