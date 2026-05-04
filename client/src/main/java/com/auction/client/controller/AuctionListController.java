package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;

public class AuctionListController {
    @FXML
    private void openAuctionDetail() {
        SceneManager.showAuctionDetail();
    }

    @FXML
    private void openLiveBidding() {
        SceneManager.showLiveBidding();
    }
}
