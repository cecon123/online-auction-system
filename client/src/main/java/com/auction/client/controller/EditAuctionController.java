package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.UpdateAuctionRequest;
import com.auction.common.enums.ItemType;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class EditAuctionController {

  private final AuctionClientService auctionService;
  private Long currentAuctionId;

  public EditAuctionController() {
    this.auctionService = new AuctionClientService();
  }

  public void initData(Long auctionId) {
    this.currentAuctionId = auctionId;
    auctionService
        .getAuctionDetail(auctionId)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess() && response.getData() != null) {
                      com.auction.common.dto.auction.AuctionDetailDto detail = response.getData();
                      productNameField.setText(detail.title());
                      categoryComboBox.setValue(detail.itemType());
                      conditionComboBox.setValue(detail.condition());
                      descriptionArea.setText(detail.description());
                      startingPriceField.setText(
                          detail.startingPrice() != null ? detail.startingPrice().toString() : "");
                      reservePriceField.setText(
                          detail.reservePrice() != null ? detail.reservePrice().toString() : "");

                      if (detail.startTime() != null) {
                        startDatePicker.setValue(detail.startTime().toLocalDate());
                        startHourSpinner.getValueFactory().setValue(detail.startTime().getHour());
                        startMinuteSpinner
                            .getValueFactory()
                            .setValue(detail.startTime().getMinute());
                      }
                      if (detail.endTime() != null) {
                        endDatePicker.setValue(detail.endTime().toLocalDate());
                        endHourSpinner.getValueFactory().setValue(detail.endTime().getHour());
                        endMinuteSpinner.getValueFactory().setValue(detail.endTime().getMinute());
                      }

                      if (detail.imagePath() != null) {
                        selectedImageLabel.setText(detail.imagePath());
                        // We don't set selectedImageFile here because it's already on the server
                      }
                    } else {
                      showError("Failed to load auction details.");
                    }
                  });
            });
  }

  @FXML private TextField productNameField;

  @FXML private ComboBox<ItemType> categoryComboBox;

  @FXML private ComboBox<String> conditionComboBox;

  @FXML private TextArea descriptionArea;

  @FXML private TextField startingPriceField;

  @FXML private TextField reservePriceField;

  @FXML private DatePicker startDatePicker;

  @FXML private Spinner<Integer> startHourSpinner;

  @FXML private Spinner<Integer> startMinuteSpinner;

  @FXML private DatePicker endDatePicker;

  @FXML private Spinner<Integer> endHourSpinner;

  @FXML private Spinner<Integer> endMinuteSpinner;

  @FXML private Label selectedImageLabel;

  @FXML private Label messageLabel;

  private File selectedImageFile;

  @FXML
  private void initialize() {
    categoryComboBox.setItems(FXCollections.observableArrayList(ItemType.values()));
    categoryComboBox.setValue(ItemType.ELECTRONICS);

    conditionComboBox
        .getItems()
        .setAll("Brand New", "Used - Excellent", "Used - Good", "Used - Fair");
    conditionComboBox.setValue("Brand New");

    // Setup hour spinners (0-23)
    startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
    endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));

    // Setup minute spinners (0-59)
    startMinuteSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

    // Default dates: tomorrow and day after
    startDatePicker.setValue(LocalDate.now().plusDays(1));
    endDatePicker.setValue(LocalDate.now().plusDays(2));

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
            new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("All files", "*.*"));

    Window window = productNameField.getScene().getWindow();
    File file = fileChooser.showOpenDialog(window);
    processSelectedFile(file);
  }

  @FXML
  private void handleDragOver(DragEvent event) {
    if (event.getDragboard().hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    event.consume();
  }

  @FXML
  private void handleDragDropped(DragEvent event) {
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (db.hasFiles()) {
      File file = db.getFiles().get(0);
      processSelectedFile(file);
      success = true;
    }
    event.setDropCompleted(success);
    event.consume();
  }

  private void processSelectedFile(File file) {
    if (file != null) {
      // Basic extension check
      String name = file.getName().toLowerCase();
      if (!(name.endsWith(".png")
          || name.endsWith(".jpg")
          || name.endsWith(".jpeg")
          || name.endsWith(".gif"))) {
        showError("Invalid file type. Please select an image (PNG, JPG, GIF).");
        return;
      }

      if (file.length() > 2 * 1024 * 1024) {
        showError("Image size too large (max 2MB)");
        selectedImageFile = null;
        selectedImageLabel.setText("No image selected");
        return;
      }
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

    BigDecimal reservePrice = parsePositiveMoney(getText(reservePriceField));
    if (reservePrice != null && reservePrice.compareTo(startingPrice) < 0) {
      showError("Reserve price must be greater than or equal to starting price.");
      return;
    }

    // Build start/end times from DatePicker + Spinners
    LocalDate startDate = startDatePicker.getValue();
    LocalDate endDate = endDatePicker.getValue();

    if (startDate == null) {
      showError("Start date is required.");
      return;
    }
    if (endDate == null) {
      showError("End date is required.");
      return;
    }

    LocalDateTime startTime =
        LocalDateTime.of(
            startDate, LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue()));
    LocalDateTime endTime =
        LocalDateTime.of(
            endDate, LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue()));

    if (!endTime.isAfter(startTime)) {
      showError("End time must be after start time.");
      return;
    }

    if (selectedImageFile == null
        && (selectedImageLabel.getText() == null
            || selectedImageLabel.getText().equals("No image selected"))) {
      showError("Please choose an item image.");
      return;
    }

    if (currentAuctionId == null) {
      showError("Auction ID is missing.");
      return;
    }

    String imageBase64 = null;
    String imagePath = selectedImageLabel.getText();

    if (selectedImageFile != null) {
      try {
        imageBase64 = com.auction.client.util.FileUtil.toBase64(selectedImageFile);
        imagePath = selectedImageFile.getName();
      } catch (Exception e) {
        showError("Failed to process image: " + e.getMessage());
        return;
      }
    }

    UpdateAuctionRequest request =
        new UpdateAuctionRequest(
            currentAuctionId,
            productName,
            categoryComboBox.getValue(),
            conditionComboBox.getValue(),
            description,
            startingPrice,
            reservePrice,
            startTime,
            endTime,
            imagePath,
            imageBase64);

    messageLabel.setText("Updating auction...");

    auctionService
        .updateAuction(request)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      showSuccess("Auction updated successfully!");
                      // SceneManager.showSellerCenter(); // Stay on current screen
                    } else {
                      showError("Failed to update auction: " + response.getMessage());
                    }
                  });
            })
        .exceptionally(
            ex -> {
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
