package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.dashboard.DashboardDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class BidderDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(BidderDashboardController.class);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private Label balanceLabel;
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
        refreshDashboard();
        
        // Listen for balance changes
        SceneManager.setBalanceListener(() -> {
            Platform.runLater(this::updateBalanceDisplay);
        });
    }

    private void refreshDashboard() {
        auctionService.getDashboard().thenAccept(response -> {
            if (response.isSuccess()) {
                DashboardDto stats = response.getData();
                Platform.runLater(() -> {
                    activeBidsLabel.setText(String.valueOf(stats.participatingAuctionsCount()));
                    winningBidsLabel.setText(String.valueOf(stats.winningAuctionsCount()));
                    // Outbid is participating - winning
                    int outbid = stats.participatingAuctionsCount() - stats.winningAuctionsCount();
                    outbidLabel.setText(String.valueOf(Math.max(0, outbid)));
                    
                    // Update user balance globally if it changed
                    if (stats.balance() != null) {
                        SceneManager.setCurrentBalance(stats.balance());
                    }
                    updateBalanceDisplay();
                });
            }
        });
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText(CURRENCY_FORMAT.format(SceneManager.getCurrentBalance()));
    }

    @FXML
    private void showAllAuctions() {
        SceneManager.showAuctionList();
    }
}
