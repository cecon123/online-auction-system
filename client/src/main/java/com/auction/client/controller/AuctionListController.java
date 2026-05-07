package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
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
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuctionListController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListController.class);

    @FXML
    private TilePane auctionContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> statusFilter;

    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        loadAuctions();
        setupFilters();
    }

    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All Categories", "ELECTRONICS", "ART", "VEHICLE");
            categoryFilter.setValue("All Categories");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Any Status", "OPEN", "RUNNING", "FINISHED");
            statusFilter.setValue("Any Status");
        }
    }

    private void loadAuctions() {
        auctionService.getAuctions().thenAccept(response -> {
            if (response.isSuccess()) {
                List<AuctionSummaryDto> auctions = response.getData();
                Platform.runLater(() -> populateAuctions(auctions));
            } else {
                logger.error("Failed to load auctions: {}", response.getMessage());
            }
        }).exceptionally(ex -> {
            logger.error("Error connecting to server", ex);
            return null;
        });
    }

    private void populateAuctions(List<AuctionSummaryDto> auctions) {
        auctionContainer.getChildren().clear();
        for (AuctionSummaryDto auction : auctions) {
            VBox card = createAuctionCard(auction);
            auctionContainer.getChildren().add(card);
        }
    }

    private VBox createAuctionCard(AuctionSummaryDto auction) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(310);
        card.setPadding(new Insets(0, 0, 20, 0)); // Bottom padding only, others handled by spacing/elements

        // Image Placeholder with Status Badge
        VBox imageArea = new VBox();
        imageArea.getStyleClass().add("image-placeholder");
        imageArea.setPrefHeight(160);
        imageArea.setPadding(new Insets(12));
        
        HBox badgeContainer = new HBox();
        badgeContainer.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        Label statusBadge = new Label(auction.status().toString());
        statusBadge.getStyleClass().add("status-badge");
        // Apply status-specific style
        switch (auction.status()) {
            case RUNNING -> statusBadge.setStyle("-fx-background-color: #e1f5fe; -fx-text-fill: #01579b;");
            case OPEN -> statusBadge.setStyle("-fx-background-color: #f1f8e9; -fx-text-fill: #33691e;");
            case FINISHED -> statusBadge.setStyle("-fx-background-color: #efebe9; -fx-text-fill: #4e342e;");
            default -> {}
        }
        badgeContainer.getChildren().add(statusBadge);

        VBox centerLabel = new VBox();
        centerLabel.setAlignment(javafx.geometry.Pos.CENTER);
        VBox.setVgrow(centerLabel, javafx.scene.layout.Priority.ALWAYS);
        Label imageLabel = new Label(auction.itemType().toString());
        imageLabel.getStyleClass().add("image-placeholder-text");
        centerLabel.getChildren().add(imageLabel);
        
        imageArea.getChildren().addAll(badgeContainer, centerLabel);

        // Content Area
        VBox content = new VBox(6);
        content.setPadding(new Insets(0, 20, 0, 20));

        Label categoryLabel = new Label(auction.itemType().toString());
        categoryLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-transform: uppercase;");

        Label titleLabel = new Label(auction.title());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(48);
        titleLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        HBox priceInfo = new HBox(10);
        priceInfo.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        VBox priceBox = new VBox(2);
        Label bidLabel = new Label("Current Bid");
        bidLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 12px;");
        Label priceLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
        priceLabel.getStyleClass().add("money-large");
        priceBox.getChildren().addAll(bidLabel, priceLabel);
        priceInfo.getChildren().add(priceBox);

        HBox actions = new HBox(12);
        actions.setPadding(new Insets(10, 20, 0, 20));
        
        Button viewDetailBtn = new Button("View Detail");
        viewDetailBtn.getStyleClass().add("secondary-button");
        HBox.setHgrow(viewDetailBtn, javafx.scene.layout.Priority.ALWAYS);
        viewDetailBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailBtn.setOnAction(e -> SceneManager.showAuctionDetail(auction.id()));

        Button liveBidBtn = new Button("Live Bid");
        liveBidBtn.getStyleClass().add("primary-button");
        HBox.setHgrow(liveBidBtn, javafx.scene.layout.Priority.ALWAYS);
        liveBidBtn.setMaxWidth(Double.MAX_VALUE);
        liveBidBtn.setOnAction(e -> SceneManager.showLiveBidding(auction.id()));

        content.getChildren().addAll(categoryLabel, titleLabel, priceInfo);
        actions.getChildren().addAll(viewDetailBtn, liveBidBtn);
        
        card.getChildren().addAll(imageArea, content, actions);
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
