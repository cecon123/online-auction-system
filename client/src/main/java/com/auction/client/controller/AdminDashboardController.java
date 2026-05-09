package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.common.dto.dashboard.DashboardDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalAuctionsLabel;
    @FXML
    private Label runningAuctionsLabel;
    @FXML
    private Label finishedAuctionsLabel;

    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        auctionService.getDashboard().thenAccept(response -> {
            if (response.isSuccess()) {
                DashboardDto stats = response.getData();
                Platform.runLater(() -> {
                    totalUsersLabel.setText(String.format("%,d", stats.totalUsersCount()));
                    totalAuctionsLabel.setText(String.format("%,d", stats.totalAuctionsCount()));
                    runningAuctionsLabel.setText(String.format("%,d", stats.activeAuctionsCount()));
                    finishedAuctionsLabel.setText(String.format("%,d", stats.totalAuctionsCount() - stats.activeAuctionsCount()));
                });
            }
        });
    }
}
