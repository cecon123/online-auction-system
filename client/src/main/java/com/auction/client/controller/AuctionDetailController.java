package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionDetailController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);
    private final AuctionClientService auctionService = new AuctionClientService();
    private Long auctionId;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        loadAuctionDetail();
    }

    private void loadAuctionDetail() {
        if (auctionId == null) return;
        
        auctionService.getAuctionDetail(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                AuctionDetailDto detail = response.getData();
                Platform.runLater(() -> {
                    // TODO: Update UI labels with detail data
                    logger.info("Loaded auction detail: {}", detail.title());
                });
            } else {
                logger.error("Failed to load auction detail: {}", response.getMessage());
            }
        });
    }

    @FXML
    private void handlePlaceBid() {
        SceneManager.showLiveBidding(auctionId);
    }
}
