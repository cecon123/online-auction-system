package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.bid.PlaceBidResponse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionDetailController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private VBox imageContainer;
    @FXML private Label imagePlaceholderLabel;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label highestBidderLabel;
    @FXML private Label itemTypeLabel;
    @FXML private Label conditionLabel;
    @FXML private Label startingPriceLabel;
    @FXML private Label reservePriceLabel;
    @FXML private Label sellerLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionLabel;

    // Bid history section
    @FXML private ScrollPane bidHistoryScrollPane;
    @FXML private VBox bidHistoryContainer;
    @FXML private Label noBidsLabel;

    private final AuctionClientService auctionService = new AuctionClientService();
    private Long auctionId;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        loadAuctionDetail();
        loadBidHistory();
    }

    private void loadAuctionDetail() {
        if (auctionId == null) return;

        auctionService.getAuctionDetail(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                AuctionDetailDto detail = response.getData();
                Platform.runLater(() -> updateUI(detail));
            } else {
                logger.error("Failed to load auction detail: {}", response.getMessage());
            }
        });
    }

    private void loadBidHistory() {
        if (auctionId == null) return;

        auctionService.getBidHistory(auctionId).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess() && response.getData() != null) {
                    displayBidHistory(response.getData());
                }
            });
        });
    }

    private void displayBidHistory(List<PlaceBidResponse> bids) {
        // Clear existing rows except the noBidsLabel
        bidHistoryContainer.getChildren().removeIf(node -> node instanceof HBox);

        if (bids == null || bids.isEmpty()) {
            noBidsLabel.setVisible(true);
            noBidsLabel.setManaged(true);
            return;
        }

        noBidsLabel.setVisible(false);
        noBidsLabel.setManaged(false);

        // Add column header row
        HBox header = new HBox(0);
        header.getStyleClass().add("bid-row");
        header.setStyle("-fx-background-color: #f8fafc; -fx-border-width: 0 0 1 0; -fx-border-color: #e2e8f0;");

        Label hBidder = new Label("Bidder");
        hBidder.setPrefWidth(180);
        hBidder.getStyleClass().add("form-label");

        Label hAmount = new Label("Amount");
        hAmount.setPrefWidth(180);
        hAmount.getStyleClass().add("form-label");

        Label hTime = new Label("Time");
        hTime.setPrefWidth(200);
        hTime.getStyleClass().add("form-label");

        header.getChildren().addAll(hBidder, hAmount, hTime);
        bidHistoryContainer.getChildren().add(0, header);

        // Render each bid (newest first – reverse the list)
        List<PlaceBidResponse> reversed = bids.reversed();
        for (PlaceBidResponse bid : reversed) {
            HBox row = new HBox(0);
            row.getStyleClass().add("bid-row");

            Label bidderLbl = new Label(bid.highestBidderUsername() != null ? bid.highestBidderUsername() : "Unknown");
            bidderLbl.setPrefWidth(180);
            bidderLbl.getStyleClass().add("page-subtitle");

            Label amountLbl = new Label(CURRENCY_FORMAT.format(bid.currentPrice() != null ? bid.currentPrice() : BigDecimal.ZERO));
            amountLbl.setPrefWidth(180);
            amountLbl.getStyleClass().add("bid-amount");

            Label timeLbl = new Label(bid.timestamp() != null ? DATE_FORMAT.format(bid.timestamp()) : "—");
            timeLbl.setPrefWidth(200);
            timeLbl.getStyleClass().add("bid-timestamp");

            row.getChildren().addAll(bidderLbl, amountLbl, timeLbl);
            bidHistoryContainer.getChildren().add(row);
        }
    }

    private void updateUI(AuctionDetailDto detail) {
        if (detail == null) return;

        // Update status badge with unified system
        String statusName = detail.status().name();
        statusLabel.getStyleClass().setAll("status-badge");
        switch (statusName) {
            case "RUNNING" -> {
                statusLabel.setText("LIVE AUCTION");
                statusLabel.getStyleClass().add("status-running");
            }
            case "OPEN" -> {
                statusLabel.setText("OPEN");
                statusLabel.getStyleClass().add("status-open");
            }
            case "FINISHED" -> {
                statusLabel.setText("FINISHED");
                statusLabel.getStyleClass().add("status-finished");
            }
            default -> {
                statusLabel.setText(statusName);
                statusLabel.getStyleClass().add("status-cancelled");
            }
        }

        titleLabel.setText(detail.title());
        currentPriceLabel.setText(CURRENCY_FORMAT.format(detail.currentPrice()));

        String bidder = detail.highestBidderUsername();
        highestBidderLabel.setText("Highest Bidder: " + (bidder != null ? bidder : "No bids yet"));

        itemTypeLabel.setText(detail.itemType().name());
        conditionLabel.setText(detail.condition());
        startingPriceLabel.setText(CURRENCY_FORMAT.format(detail.startingPrice()));
        if (detail.reservePrice() != null) {
            reservePriceLabel.setText(CURRENCY_FORMAT.format(detail.reservePrice()));
        } else {
            reservePriceLabel.setText("None");
        }
        sellerLabel.setText(detail.sellerUsername());

        if (detail.startTime() != null) {
            startTimeLabel.setText(DATE_FORMAT.format(detail.startTime()));
        }
        if (detail.endTime() != null) {
            endTimeLabel.setText(DATE_FORMAT.format(detail.endTime()));
        }

        descriptionTitleLabel.setText(detail.title());
        descriptionLabel.setText(detail.description());

        if (detail.imagePath() != null && !detail.imagePath().isEmpty()) {
            imagePlaceholderLabel.setText("Image: " + detail.imagePath());
        }

        logger.info("UI updated for auction: {}", detail.title());
    }

    @FXML
    private void handlePlaceBid() {
        SceneManager.showLiveBidding(auctionId);
    }
}
