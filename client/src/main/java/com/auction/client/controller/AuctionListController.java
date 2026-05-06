package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuctionListController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListController.class);

    @FXML
    private TilePane auctionContainer;

    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        loadAuctions();
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
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(290);
        card.setPadding(new Insets(0, 18, 18, 18));

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefHeight(140);
        Label imageLabel = new Label(auction.itemType() + " IMAGE");
        imageLabel.getStyleClass().add("image-placeholder-text");
        imagePlaceholder.getChildren().add(imageLabel);

        Label categoryLabel = new Label(auction.itemType().toString());
        categoryLabel.getStyleClass().add("form-label");

        Label titleLabel = new Label(auction.title());
        titleLabel.getStyleClass().add("card-title");

        Label bidLabel = new Label("Current Bid");
        bidLabel.getStyleClass().add("page-subtitle");

        Label priceLabel = new Label("$" + String.format("%.2f", auction.currentPrice()));
        priceLabel.getStyleClass().add("money-large");

        Button viewDetailBtn = new Button("View Detail");
        viewDetailBtn.getStyleClass().add("secondary-button");
        viewDetailBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailBtn.setOnAction(e -> SceneManager.showAuctionDetail(auction.id()));

        Button liveBidBtn = new Button("Live Bid");
        liveBidBtn.getStyleClass().add("primary-button");
        liveBidBtn.setMaxWidth(Double.MAX_VALUE);
        liveBidBtn.setOnAction(e -> SceneManager.showLiveBidding(auction.id()));

        card.getChildren().addAll(imagePlaceholder, categoryLabel, titleLabel, bidLabel, priceLabel, viewDetailBtn, liveBidBtn);
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
