package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Response;
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
import java.util.function.Consumer;

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
    private final Consumer<Response<?>> refreshListener = response -> {
        logger.info("Realtime update received in MyBids, refreshing...");
        loadMyBids();
    };

    @FXML
    private void initialize() {
        loadMyBids();
        
        // Listen for bid updates or auction closures to refresh the list
        SocketClient.getInstance().addEventListener(MessageType.BID_UPDATE, refreshListener);
        SocketClient.getInstance().addEventListener(MessageType.AUCTION_CLOSED, refreshListener);
    }

    @FXML
    private void loadMyBids() {
        auctionService.getMyBids().thenAccept(response -> {
            if (response.isSuccess()) {
                List<AuctionSummaryDto> auctions = response.getData();
                Platform.runLater(() -> {
                    populateParticipatingAuctions(auctions);
                });
            } else {
                logger.error("Failed to load my bids: {}", response.getMessage());
            }
        }).exceptionally(ex -> {
            logger.error("Error connecting to server", ex);
            return null;
        });

        auctionService.getUserBidHistory().thenAccept(response -> {
            if (response.isSuccess()) {
                List<com.auction.common.dto.bid.BidHistoryDto> history = response.getData();
                Platform.runLater(() -> {
                    populateBidHistory(history);
                });
            }
        });
    }

    private void populateBidHistory(List<com.auction.common.dto.bid.BidHistoryDto> history) {
        // Clear existing rows (keep header)
        if (bidHistoryContainer.getChildren().size() > 1) {
            bidHistoryContainer.getChildren().remove(1, bidHistoryContainer.getChildren().size());
        }

        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

        for (int i = 0; i < history.size(); i++) {
            com.auction.common.dto.bid.BidHistoryDto entry = history.get(i);
            GridPane row = new GridPane();
            row.setHgap(20);
            row.getStyleClass().add(i % 2 != 0 ? "admin-table-row-alt" : "admin-table-row");
            row.setPadding(new Insets(12, 18, 12, 18));

            ColumnConstraints col0 = new ColumnConstraints(); col0.setPercentWidth(18);
            ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(37);
            ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(20);
            ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(25);
            row.getColumnConstraints().addAll(col0, col1, col2, col3);

            row.add(new Label(entry.timestamp().format(timeFormatter)), 0, 0);
            row.add(new Label(entry.auctionTitle()), 1, 0);
            
            Label amountLabel = new Label("$" + String.format("%,.2f", entry.amount()));
            amountLabel.getStyleClass().add("money");
            row.add(amountLabel, 2, 0);

            Label resultLabel = new Label(entry.result());
            String resultStyle = switch (entry.result()) {
                case "WINNING", "WON" -> "success-text";
                case "OUTBID", "LOST" -> "error-text";
                default -> "admin-table-cell";
            };
            resultLabel.getStyleClass().add(resultStyle);
            row.add(resultLabel, 3, 0);

            bidHistoryContainer.getChildren().add(row);
        }
    }

    private void populateParticipatingAuctions(List<AuctionSummaryDto> auctions) {
        // Clear existing rows (keep header)
        if (participatingAuctionsContainer.getChildren().size() > 1) {
            participatingAuctionsContainer.getChildren().remove(1, participatingAuctionsContainer.getChildren().size());
        }

        int activeCount = 0;
        int winningCount = 0;
        int outbidCount = 0;
        long currentUserId = SceneManager.getCurrentUserId();

        for (int i = 0; i < auctions.size(); i++) {
            AuctionSummaryDto auction = auctions.get(i);
            boolean userIsLeading = auction.highestBidderId() != null && auction.highestBidderId() == currentUserId;
            
            if (auction.status() == com.auction.common.enums.AuctionStatus.RUNNING || 
                auction.status() == com.auction.common.enums.AuctionStatus.OPEN) {
                activeCount++;
                if (auction.status() == com.auction.common.enums.AuctionStatus.RUNNING) {
                    if (userIsLeading) winningCount++;
                    else outbidCount++;
                }
            }

            GridPane row = createAuctionRow(auction, i % 2 != 0, userIsLeading);
            participatingAuctionsContainer.getChildren().add(row);
        }

        activeBidsLabel.setText(String.valueOf(activeCount));
        winningBidsLabel.setText(String.valueOf(winningCount));
        outbidLabel.setText(String.valueOf(outbidCount));
    }

    private GridPane createAuctionRow(AuctionSummaryDto auction, boolean isAlt, boolean userIsLeading) {
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

        // Title
        Label titleLabel = new Label(auction.title());
        titleLabel.getStyleClass().add("admin-table-cell");
        row.add(titleLabel, 0, 0);
        
        // Current Price (using as Your Bid if not tracked)
        Label yourBidLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
        yourBidLabel.getStyleClass().add("money");
        row.add(yourBidLabel, 1, 0);

        Label currentBidLabel = new Label("$" + String.format("%,.2f", auction.currentPrice()));
        currentBidLabel.getStyleClass().add("money");
        row.add(currentBidLabel, 2, 0);

        // Status Label (Winning/Outbid/Finished)
        String statusText;
        String styleClass;
        
        if (auction.status() == com.auction.common.enums.AuctionStatus.FINISHED) {
            statusText = userIsLeading ? "WON" : "ENDED";
            styleClass = userIsLeading ? "success-text" : "admin-table-cell";
        } else {
            statusText = userIsLeading ? "WINNING" : "OUTBID";
            styleClass = userIsLeading ? "success-text" : "error-text";
        }
        
        Label statusLabel = new Label(statusText);
        statusLabel.getStyleClass().add(styleClass);
        row.add(statusLabel, 3, 0);

        row.add(new Label(formatRemainingTime(auction.endTime())), 4, 0);

        Button actionBtn = new Button("View");
        actionBtn.getStyleClass().add("secondary-button");
        actionBtn.setOnAction(e -> SceneManager.showLiveBidding(auction.id()));
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
