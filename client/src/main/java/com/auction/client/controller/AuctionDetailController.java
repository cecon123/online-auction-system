package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;

public class AuctionDetailController {
    @FXML
    private void handlePlaceBid() {
        SceneManager.showLiveBidding();
    }
}
