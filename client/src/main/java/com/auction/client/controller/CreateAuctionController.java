package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class CreateAuctionController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField productNameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> conditionComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField startingPriceField;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private Label selectedImageLabel;

    @FXML
    private Label messageLabel;

    private File selectedImageFile;

    @FXML
    private void initialize() {
        categoryComboBox
            .getItems()
            .setAll(
                "ELECTRONICS",
                "ART",
                "VEHICLE",
                "WATCHES",
                "JEWELRY",
                "INSTRUMENTS",
                "ANTIQUES"
            );

        conditionComboBox
            .getItems()
            .setAll(
                "Brand New",
                "Used - Excellent",
                "Used - Good",
                "Used - Fair"
            );

        conditionComboBox.setValue("Brand New");

        startTimeField.setPromptText("2026-05-04 09:30");
        endTimeField.setPromptText("2026-05-05 17:00");

        selectedImageLabel.setText("No image selected");
        messageLabel.setText("");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose item image");
        fileChooser
            .getExtensionFilters()
            .addAll(
                new FileChooser.ExtensionFilter(
                    "Image files",
                    "*.png",
                    "*.jpg",
                    "*.jpeg",
                    "*.gif"
                ),
                new FileChooser.ExtensionFilter("All files", "*.*")
            );

        Window window = productNameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
            showSuccess("Selected image: " + file.getName());
        }
    }

    @FXML
    private void handleSaveAuction() {
        String productName = getText(productNameField);
        String description = getText(descriptionArea);
        String startingPrice = getText(startingPriceField);
        String startTimeText = getText(startTimeField);
        String endTimeText = getText(endTimeField);

        if (productName.isBlank()) {
            showError("Product name is required.");
            return;
        }

        if (categoryComboBox.getValue() == null) {
            showError("Category is required.");
            return;
        }

        if (conditionComboBox.getValue() == null) {
            showError("Condition is required.");
            return;
        }

        if (description.isBlank()) {
            showError("Description is required.");
            return;
        }

        if (!isPositiveNumber(startingPrice)) {
            showError("Starting price must be a positive number.");
            return;
        }

        LocalDateTime startTime = parseDateTime(startTimeText, "Start time");
        if (startTime == null) {
            return;
        }

        LocalDateTime endTime = parseDateTime(endTimeText, "End time");
        if (endTime == null) {
            return;
        }

        if (!endTime.isAfter(startTime)) {
            showError("End time must be after start time.");
            return;
        }

        if (selectedImageFile == null) {
            showError("Please choose an item image.");
            return;
        }

        showSuccess(
            "Mock auction saved successfully: " +
                startTime.format(DATE_TIME_FORMATTER) +
                " -> " +
                endTime.format(DATE_TIME_FORMATTER)
        );
    }

    @FXML
    private void handleCancel() {
        SceneManager.showSellerCenter();
    }

    private String getText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String getText(TextArea area) {
        return area.getText() == null ? "" : area.getText().trim();
    }

    private boolean isPositiveNumber(String value) {
        try {
            return Double.parseDouble(value.replace(",", "")) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        if (value.isBlank()) {
            showError(fieldName + " is required. Example: 2026-05-04 09:30");
            return null;
        }

        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            showError(
                fieldName +
                    " must use format yyyy-MM-dd HH:mm. Example: 2026-05-04 09:30"
            );
            return null;
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("success-text");
        if (!messageLabel.getStyleClass().contains("error-text")) {
            messageLabel.getStyleClass().add("error-text");
        }
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-text");
        if (!messageLabel.getStyleClass().contains("success-text")) {
            messageLabel.getStyleClass().add("success-text");
        }
    }
}
