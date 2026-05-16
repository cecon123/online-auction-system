package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionListController {
  private static final Logger logger = LoggerFactory.getLogger(AuctionListController.class);

  @FXML private TilePane auctionContainer;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private ComboBox<String> statusFilter;
  @FXML private VBox emptyState;

  private final AuctionClientService auctionService = new AuctionClientService();
  private List<AuctionSummaryDto> allAuctions = List.of();

  @FXML
  private void initialize() {
    setupFilters();
    loadAuctions();

    // Auto-wrap TilePane based on container width
    auctionContainer
        .widthProperty()
        .addListener(
            (obs, oldW, newW) -> {
              double cardWidth = 260;
              double gap = 16;
              int cols = Math.max(1, (int) (newW.doubleValue() / (cardWidth + gap)));
              auctionContainer.setPrefColumns(cols);
            });

    com.auction.client.socket.SocketClient.getInstance()
        .addEventListener(
            com.auction.common.protocol.MessageType.AUCTION_LIST_UPDATED,
            response -> javafx.application.Platform.runLater(this::loadAuctions));
  }

  private void setupFilters() {
    if (categoryFilter != null) {
      categoryFilter.getItems().clear();
      categoryFilter.getItems().addAll("All Categories", "ELECTRONICS", "ART", "VEHICLE");
      categoryFilter.setValue("All Categories");
      categoryFilter.setOnAction(e -> applyFilters());
    }
    if (statusFilter != null) {
      statusFilter.getItems().clear();
      statusFilter
          .getItems()
          .addAll("Any Status", "OPEN", "RUNNING", "FINISHED", "PAID", "CANCELED");
      statusFilter.setValue("Any Status");
      statusFilter.setOnAction(e -> applyFilters());
    }
    if (searchField != null) {
      searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
  }

  private void loadAuctions() {
    auctionService
        .getAuctions()
        .thenAccept(
            response -> {
              if (response.isSuccess()) {
                allAuctions = response.getData();
                Platform.runLater(this::applyFilters);
              } else {
                logger.error("Failed to load auctions: {}", response.getMessage());
              }
            })
        .exceptionally(
            ex -> {
              logger.error("Error connecting to server", ex);
              return null;
            });
  }

  private void applyFilters() {
    String searchText = searchField.getText().toLowerCase().trim();
    String category = categoryFilter.getValue();
    String status = statusFilter.getValue();

    List<AuctionSummaryDto> filtered =
        allAuctions.stream()
            .filter(a -> searchText.isEmpty() || a.title().toLowerCase().contains(searchText))
            .filter(
                a ->
                    category == null
                        || category.equals("All Categories")
                        || a.itemType().toString().equalsIgnoreCase(category))
            .filter(
                a ->
                    status == null
                        || status.equals("Any Status")
                        || a.status().toString().equalsIgnoreCase(status))
            .toList();

    populateAuctions(filtered);
  }

  private void populateAuctions(List<AuctionSummaryDto> auctions) {
    auctionContainer.getChildren().clear();
    boolean isEmpty = auctions.isEmpty();
    emptyState.setVisible(isEmpty);
    emptyState.setManaged(isEmpty);
    auctionContainer.setVisible(!isEmpty);
    auctionContainer.setManaged(!isEmpty);

    for (AuctionSummaryDto auction : auctions) {
      VBox card = createAuctionCard(auction);
      auctionContainer.getChildren().add(card);
    }
  }

  private VBox createAuctionCard(AuctionSummaryDto auction) {
    VBox card = new VBox(10);
    card.getStyleClass().add("card");
    card.setPrefWidth(260);
    card.setMaxWidth(260);
    card.setPrefHeight(340);
    card.setMaxHeight(340);
    card.setPadding(new Insets(0, 0, 16, 0));

    // Image Area with Status Badge overlay
    javafx.scene.layout.StackPane imageArea = new javafx.scene.layout.StackPane();
    imageArea.getStyleClass().add("image-placeholder");
    imageArea.setPrefHeight(130);
    imageArea.setMinHeight(130);
    imageArea.setMaxHeight(130);

    String fullUrl = com.auction.client.util.ImageUrlUtil.getImageUrl(auction.imagePath());
    if (fullUrl != null) {
      javafx.scene.layout.Region imagePreview = new javafx.scene.layout.Region();
      imagePreview.setPrefSize(260, 130);

      javafx.scene.image.Image img = new javafx.scene.image.Image(fullUrl, true);
      img.progressProperty()
          .addListener(
              (obs, old, progress) -> {
                if (progress.doubleValue() == 1.0) {
                  javafx.application.Platform.runLater(
                      () -> {
                        imagePreview.setBackground(
                            new javafx.scene.layout.Background(
                                new javafx.scene.layout.BackgroundImage(
                                    img,
                                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                                    javafx.scene.layout.BackgroundPosition.CENTER,
                                    new javafx.scene.layout.BackgroundSize(
                                        javafx.scene.layout.BackgroundSize.AUTO,
                                        javafx.scene.layout.BackgroundSize.AUTO,
                                        false,
                                        false,
                                        false,
                                        true))));
                      });
                }
              });

      javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 130);
      clip.setArcWidth(16);
      clip.setArcHeight(16);
      imagePreview.setClip(clip);

      imageArea.getChildren().add(imagePreview);
    } else {
      Label imageLabel = new Label(auction.itemType().toString());
      imageLabel.getStyleClass().add("image-placeholder-text");
      imageArea.getChildren().add(imageLabel);
    }

    // Status Badge Overlay
    Label statusBadge = new Label(auction.status().toString());
    statusBadge.getStyleClass().add("status-badge");
    switch (auction.status()) {
      case RUNNING -> statusBadge.getStyleClass().add("status-running");
      case OPEN -> statusBadge.getStyleClass().add("status-open");
      case FINISHED -> statusBadge.getStyleClass().add("status-finished");
      case PAID -> statusBadge.getStyleClass().add("status-paid");
      default -> statusBadge.getStyleClass().add("status-cancelled");
    }

    javafx.scene.layout.StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
    javafx.scene.layout.StackPane.setMargin(statusBadge, new Insets(10));
    imageArea.getChildren().add(statusBadge);

    // Content Area
    VBox content = new VBox(4);
    content.setPadding(new Insets(0, 16, 0, 16));

    Label categoryLabel = new Label(auction.itemType().toString());
    categoryLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 10px; -fx-font-weight: bold;");

    Label titleLabel = new Label(auction.title());
    titleLabel.getStyleClass().add("card-title");
    titleLabel.setWrapText(true);
    titleLabel.setMinHeight(36);
    titleLabel.setAlignment(Pos.TOP_LEFT);

    HBox priceInfo = new HBox(8);
    priceInfo.setAlignment(Pos.BOTTOM_LEFT);
    VBox priceBox = new VBox(2);
    Label bidLabel = new Label("Current Bid");
    bidLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11px;");
    Label priceLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
    priceLabel.getStyleClass().add("money-large");
    priceBox.getChildren().addAll(bidLabel, priceLabel);
    priceInfo.getChildren().add(priceBox);

    content.getChildren().addAll(categoryLabel, titleLabel, priceInfo);

    // Spacer to push buttons to bottom
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    // Actions at bottom
    HBox actions = new HBox(8);
    actions.setPadding(new Insets(0, 16, 0, 16));

    Button viewDetailBtn = new Button("Detail");
    viewDetailBtn.getStyleClass().add("secondary-button");
    HBox.setHgrow(viewDetailBtn, Priority.ALWAYS);
    viewDetailBtn.setMaxWidth(Double.MAX_VALUE);
    viewDetailBtn.setOnAction(e -> SceneManager.showAuctionDetail(auction.id()));

    Button liveBidBtn = new Button("Bid");
    liveBidBtn.getStyleClass().add("primary-button");
    HBox.setHgrow(liveBidBtn, Priority.ALWAYS);
    liveBidBtn.setMaxWidth(Double.MAX_VALUE);
    liveBidBtn.setOnAction(e -> SceneManager.showLiveBidding(auction.id()));

    actions.getChildren().addAll(viewDetailBtn, liveBidBtn);

    card.getChildren().addAll(imageArea, content, spacer, actions);
    return card;
  }

  @FXML
  private void openAuctionDetail() {
    SceneManager.showAuctionDetail();
  }

  @FXML
  private void openLiveBidding() {
    SceneManager.showLiveBidding();
  }
}
