package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;

public class TopBarController {
    @FXML
    private void handlePlaceBidShortcut() {
        SceneManager.showAuctionList();
    }
}
