package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;

public class SellerCenterController {
    @FXML
    private void handleCreateAuction() {
        SceneManager.showCreateAuction();
    }
}
