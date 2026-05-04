package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateAuctionController {
    @FXML
    private TextField productNameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startingPriceField;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        categoryComboBox.getItems().addAll("ELECTRONICS", "ART", "VEHICLE");
        messageLabel.setText("");
    }

    @FXML
    private void handleSaveAuction() {
        if (productNameField.getText().isBlank()
            || categoryComboBox.getValue() == null
            || startingPriceField.getText().isBlank()
            || startTimeField.getText().isBlank()
            || endTimeField.getText().isBlank()) {
            messageLabel.setText("Please fill all required fields.");
            return;
        }

        messageLabel.setText("Mock auction saved. Real server integration will be added later.");
    }

    @FXML
    private void handleCancel() {
        SceneManager.showSellerCenter();
    }
}
