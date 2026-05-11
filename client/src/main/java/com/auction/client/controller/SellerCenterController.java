package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SellerCenterController {
  @FXML private VBox auctionListContainer;

  @FXML private VBox emptyPlaceholder;
  @FXML private Label expectedRevenueLabel;
  @FXML private Label totalRevenueLabel;
  @FXML private Label totalBidsLabel;
  @FXML private Label successRateLabel;
  @FXML private Label successRateSubtitleLabel;

  private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

  private static com.auction.common.dto.dashboard.SellerStatsDto cachedStats = null;
  private final AuctionClientService auctionService = new AuctionClientService();

  @FXML
  private void initialize() {
    if (cachedStats != null) {
      updateStatsUI(cachedStats);
    } else {
      expectedRevenueLabel.setText("...");
      totalRevenueLabel.setText("...");
      totalBidsLabel.setText("...");
      successRateLabel.setText("...");
      successRateSubtitleLabel.setText("Loading...");
    }

    loadSellerAuctions();
    loadSellerStats();

    // Register for real-time updates to refresh seller dashboard
    com.auction.client.socket.SocketClient.getInstance()
        .addEventListener(
            com.auction.common.protocol.MessageType.AUCTION_LIST_UPDATED,
            response -> Platform.runLater(this::handleRefresh));

    com.auction.client.socket.SocketClient.getInstance()
        .addEventListener(
            com.auction.common.protocol.MessageType.AUCTION_CLOSED,
            response -> Platform.runLater(this::handleRefresh));
  }

  @FXML
  private void handleRefresh() {
    // Clear caches to force fresh load
    cachedStats = null;
    cachedAuctions = null;

    loadSellerAuctions();
    loadSellerStats();
  }

  private void loadSellerStats() {
    auctionService
        .getSellerStats()
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess() && response.getData() != null) {
                      cachedStats = response.getData();
                      updateStatsUI(cachedStats);
                    }
                  });
            });
  }

  private void updateStatsUI(com.auction.common.dto.dashboard.SellerStatsDto stats) {
    expectedRevenueLabel.setText(CURRENCY.format(stats.expectedRevenue()));
    totalRevenueLabel.setText(CURRENCY.format(stats.totalRevenue()));
    totalBidsLabel.setText(String.valueOf(stats.totalBidsReceived()));
    successRateLabel.setText(stats.successRate() + "%");

    successRateSubtitleLabel.setText(
        stats.activeAuctionsCount() + " active / " + stats.totalAuctionsCount() + " total");
  }

  private static List<AuctionSummaryDto> cachedAuctions = null;

  private void loadSellerAuctions() {
    if (cachedAuctions != null) {
      displayAuctions(cachedAuctions);
    }

    auctionService
        .getSellerAuctions()
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      cachedAuctions = response.getData();
                      displayAuctions(cachedAuctions);
                    } else {
                      // Handle error
                    }
                  });
            });
  }

  private void displayAuctions(List<AuctionSummaryDto> auctions) {
    // Clear previous data rows (keep headers and empty label)
    auctionListContainer
        .getChildren()
        .removeIf(node -> node.getStyleClass().contains("auction-row"));

    if (auctions == null || auctions.isEmpty()) {
      emptyPlaceholder.setVisible(true);
      emptyPlaceholder.setManaged(true);
      return;
    }

    emptyPlaceholder.setVisible(false);
    emptyPlaceholder.setManaged(false);

    for (AuctionSummaryDto auction : auctions) {
      HBox row = new HBox(16);
      row.getStyleClass().add("auction-row");
      row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

      Label title = new Label(auction.title());
      title.getStyleClass().add("text-bold");
      HBox.setHgrow(title, javafx.scene.layout.Priority.ALWAYS);
      title.setMaxWidth(Double.MAX_VALUE);

      Label startPrice = new Label(CURRENCY.format(auction.startingPrice()));
      startPrice.setPrefWidth(100);
      startPrice.setMinWidth(100);

      Label currentPrice = new Label(CURRENCY.format(auction.currentPrice()));
      currentPrice.setPrefWidth(100);
      currentPrice.setMinWidth(100);

      Label status = new Label(auction.status().name());
      status.getStyleClass().addAll("status-badge", getStatusStyleClass(auction.status().name()));
      status.setPrefWidth(80);
      status.setMinWidth(80);

      HBox actions = new HBox(8);
      actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
      actions.setPrefWidth(130);
      actions.setMinWidth(130);

      if (auction.status() == com.auction.common.enums.AuctionStatus.OPEN) {

        javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Edit");
        btnEdit.getStyleClass().addAll("btn", "btn-sm", "btn-secondary");
        btnEdit.setOnAction(e -> handleEditAuction(auction.id()));

        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Cancel");
        btnCancel.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
        btnCancel.setOnAction(e -> handleCancelAuction(auction.id()));

        actions.getChildren().addAll(btnEdit, btnCancel);
      }

      row.getChildren().addAll(title, startPrice, currentPrice, status, actions);
      auctionListContainer.getChildren().add(row);
    }
  }

  private void handleCancelAuction(long auctionId) {
    javafx.scene.control.Alert alert =
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
    alert.setTitle("Cancel Auction");
    alert.setHeaderText("Cancel Auction #" + auctionId);
    alert.setContentText("Are you sure you want to cancel this auction? This cannot be undone.");

    if (alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
        == javafx.scene.control.ButtonType.OK) {
      auctionService
          .cancelAuction(auctionId)
          .thenAccept(
              response -> {
                Platform.runLater(
                    () -> {
                      if (response.isSuccess()) {
                        com.auction.client.util.NotificationManager.showToast(
                            "Auction canceled successfully", "SUCCESS");
                      } else {
                        com.auction.client.util.NotificationManager.showToast(
                            response.getMessage(), "ERROR");
                      }
                    });
              });
    }
  }

  private void handleEditAuction(long auctionId) {
    SceneManager.showEditAuction(auctionId);
  }

  @FXML
  private void handleCreateAuction() {
    SceneManager.showCreateAuction();
  }

  /**
   * Maps AuctionStatus enum name to the corresponding CSS class. Handles the mismatch between
   * CANCELED (enum) and status-cancelled (legacy CSS).
   */
  private String getStatusStyleClass(String statusName) {
    return switch (statusName) {
      case "RUNNING" -> "status-running";
      case "OPEN" -> "status-open";
      case "FINISHED" -> "status-finished";
      case "CANCELED" -> "status-canceled";
      default -> "status-finished";
    };
  }
}
