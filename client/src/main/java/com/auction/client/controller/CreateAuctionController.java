package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.enums.ItemType;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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

    private final AuctionClientService auctionService;

    public CreateAuctionController() {
        this.auctionService = new AuctionClientService();
    }

    @FXML
    private TextField productNameField;

    @FXML
    private ComboBox<ItemType> categoryComboBox;

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
        categoryComboBox.setItems(
            FXCollections.observableArrayList(ItemType.values())
        );
        categoryComboBox.setValue(ItemType.ELECTRONICS);

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
        String startingPriceText = getText(startingPriceField);
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

        BigDecimal startingPrice = parsePositiveMoney(startingPriceText);
        if (startingPrice == null) {
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

        CreateAuctionRequest request = new CreateAuctionRequest(
            productName,
            categoryComboBox.getValue(),
            conditionComboBox.getValue(),
            description,
            startingPrice,
            startTime,
            endTime,
            selectedImageFile.getAbsolutePath()
        );

        messageLabel.setText("Saving auction...");

        auctionService.createAuction(request).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    showSuccess("Auction created successfully!");
                    // Briefly wait then return to seller center
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(SceneManager::showSellerCenter);
                        } catch (InterruptedException ignored) {}
                    }).start();
                } else {
                    showError("Failed to create auction: " + response.getMessage());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Network error: " + ex.getMessage()));
            return null;
        });
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

    private BigDecimal parsePositiveMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            BigDecimal parsed = new BigDecimal(value.replace(",", ""));

            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            return parsed;
        } catch (NumberFormatException e) {
            return null;
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
