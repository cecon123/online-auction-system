package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;

public class SidebarController {
    @FXML
    private void showDashboard() {
        SceneManager.showDashboard();
    }

    @FXML
    private void showAuctions() {
        SceneManager.showAuctionList();
    }

    @FXML
    private void showSellerCenter() {
        SceneManager.showSellerCenter();
    }

    @FXML
    private void showLiveBidding() {
        SceneManager.showLiveBidding();
    }

    @FXML
    private void logout() {
        SceneManager.showLogin();
    }
}
