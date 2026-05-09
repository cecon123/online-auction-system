package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionSummaryDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

public class SellerCenterController {
    @FXML
    private VBox auctionListContainer;

    @FXML
    private Label emptyLabel;

    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        loadSellerAuctions();
    }

    private void loadSellerAuctions() {
        auctionService.getSellerAuctions().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    List<AuctionSummaryDto> auctions = response.getData();
                    displayAuctions(auctions);
                } else {
                    // Handle error
                }
            });
        });
    }

    private void displayAuctions(List<AuctionSummaryDto> auctions) {
        // Clear previous items except the title and empty label
        auctionListContainer.getChildren().removeIf(node -> node instanceof HBox);

        if (auctions == null || auctions.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (AuctionSummaryDto auction : auctions) {
            HBox row = new HBox(24);
            row.getStyleClass().add("auction-row");
            
            Label title = new Label(auction.title());
            title.setPrefWidth(260);
            title.getStyleClass().add("text-bold");

            Label startPrice = new Label("$" + auction.startingPrice().toPlainString());
            startPrice.setPrefWidth(140);

            Label currentPrice = new Label("$" + auction.currentPrice().toPlainString());
            currentPrice.setPrefWidth(140);

            Label status = new Label(auction.status().name());
            status.getStyleClass().add("status-" + auction.status().name().toLowerCase());

            row.getChildren().addAll(title, startPrice, currentPrice, status);
            auctionListContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleCreateAuction() {
        SceneManager.showCreateAuction();
    }
}
