package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionDetailController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private VBox imageContainer;
    @FXML
    private Label imagePlaceholderLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label highestBidderLabel;
    @FXML
    private Label itemTypeLabel;
    @FXML
    private Label conditionLabel;
    @FXML
    private Label startingPriceLabel;
    @FXML
    private Label reservePriceLabel;
    @FXML
    private Label sellerLabel;
    @FXML
    private Label startTimeLabel;
    @FXML
    private Label endTimeLabel;
    @FXML
    private Label descriptionTitleLabel;
    @FXML
    private Label descriptionLabel;

    private final AuctionClientService auctionService = new AuctionClientService();
    private Long auctionId;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        loadAuctionDetail();
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

    private void updateUI(AuctionDetailDto detail) {
        if (detail == null) return;

        statusLabel.setText(detail.status().name());
        // Update status style based on status if needed
        if (detail.status().name().equals("RUNNING")) {
            statusLabel.getStyleClass().setAll("status-running");
            statusLabel.setText("LIVE AUCTION");
        } else {
            statusLabel.getStyleClass().setAll("page-subtitle");
            statusLabel.setText(detail.status().name());
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
