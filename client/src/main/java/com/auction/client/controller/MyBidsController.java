package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.common.dto.auction.AuctionSummaryDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MyBidsController {
    private static final Logger logger = LoggerFactory.getLogger(MyBidsController.class);

    @FXML
    private VBox participatingAuctionsContainer;
    @FXML
    private VBox bidHistoryContainer;
    @FXML
    private Label activeBidsLabel;
    @FXML
    private Label winningBidsLabel;
    @FXML
    private Label outbidLabel;
    @FXML
    private Label watchlistLabel;

    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        loadMyBids();
    }

    @FXML
    private void loadMyBids() {
        auctionService.getMyBids().thenAccept(response -> {
            if (response.isSuccess()) {
                List<AuctionSummaryDto> auctions = response.getData();
                Platform.runLater(() -> {
                    populateParticipatingAuctions(auctions);
                    populateMockHistory(auctions);
                });
            } else {
                logger.error("Failed to load my bids: {}", response.getMessage());
            }
        }).exceptionally(ex -> {
            logger.error("Error connecting to server", ex);
            return null;
        });
    }

    private void populateMockHistory(List<AuctionSummaryDto> auctions) {
        if (bidHistoryContainer.getChildren().size() > 1) {
            bidHistoryContainer.getChildren().remove(1, bidHistoryContainer.getChildren().size());
        }

        for (int i = 0; i < Math.min(auctions.size(), 5); i++) {
            AuctionSummaryDto auction = auctions.get(i);
            GridPane row = createHistoryRow(auction, i % 2 != 0);
            bidHistoryContainer.getChildren().add(row);
        }
    }

    private GridPane createHistoryRow(AuctionSummaryDto auction, boolean isAlt) {
        GridPane row = new GridPane();
        row.setHgap(20);
        row.getStyleClass().add(isAlt ? "admin-table-row-alt" : "admin-table-row");
        row.setPadding(new Insets(14, 18, 14, 18));

        ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(18);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(37);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(20);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(25);
        row.getColumnConstraints().addAll(col0, col1, col2, col3);

        row.add(new Label("10:45 AM"), 0, 0);
        row.add(new Label(auction.title()), 1, 0);
        
        Label amountLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
        amountLabel.getStyleClass().add("money");
        row.add(amountLabel, 2, 0);

        Label resultLabel = new Label(isAlt ? "Outbid later" : "Highest bid");
        resultLabel.getStyleClass().add(isAlt ? "error-text" : "success-text");
        row.add(resultLabel, 3, 0);

        return row;
    }

    private void populateParticipatingAuctions(List<AuctionSummaryDto> auctions) {
        // Clear existing rows (keep header)
        if (participatingAuctionsContainer.getChildren().size() > 1) {
            participatingAuctionsContainer.getChildren().remove(1, participatingAuctionsContainer.getChildren().size());
        }

        activeBidsLabel.setText(String.valueOf(auctions.size()));
        
        // Mocking winning/outbid counts since DTO doesn't have it
        winningBidsLabel.setText("0");
        outbidLabel.setText("0");

        for (int i = 0; i < auctions.size(); i++) {
            AuctionSummaryDto auction = auctions.get(i);
            GridPane row = createAuctionRow(auction, i % 2 != 0);
            participatingAuctionsContainer.getChildren().add(row);
        }
    }

    private GridPane createAuctionRow(AuctionSummaryDto auction, boolean isAlt) {
        GridPane row = new GridPane();
        row.setHgap(20);
        row.getStyleClass().add(isAlt ? "admin-table-row-alt" : "admin-table-row");
        row.setPadding(new Insets(14, 18, 14, 18));

        ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(28);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(15);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(15);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(15);
        ColumnConstraints col5 = new ColumnConstraints(); col5.setPercentWidth(12);
        row.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5);

        row.add(new Label(auction.title()), 0, 0);
        
        // Mocking "Your Bid" - normally would come from a different DTO
        Label yourBidLabel = new Label("$" + String.format("%,.2f", auction.currentPrice().multiply(new java.math.BigDecimal("0.9"))));
        yourBidLabel.getStyleClass().add("money");
        row.add(yourBidLabel, 1, 0);

        Label currentBidLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
        currentBidLabel.getStyleClass().add("money");
        row.add(currentBidLabel, 2, 0);

        Label statusLabel = new Label(auction.status().toString());
        statusLabel.getStyleClass().add(isAlt ? "success-text" : "error-text"); // Using isAlt as mock indicator
        row.add(statusLabel, 3, 0);

        row.add(new Label(formatRemainingTime(auction.endTime())), 4, 0);

        Button actionBtn = new Button("View");
        actionBtn.getStyleClass().add("secondary-button");
        actionBtn.setOnAction(e -> com.auction.client.util.SceneManager.showAuctionDetail(auction.id()));
        row.add(actionBtn, 5, 0);

        return row;
    }

    private String formatRemainingTime(LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (endTime.isBefore(now)) return "Ended";
        Duration duration = Duration.between(now, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02dh %02dm", hours, minutes);
    }
}
