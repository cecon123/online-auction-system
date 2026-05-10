package com.auction.client.controller;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.NotificationManager;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.protocol.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main authenticated application shell.
 *
 * The shell contains:
 * - Sidebar on the left
 * - TopBar on the top
 * - Dynamic center content
 */
public class AppShellController {
    private static final Logger logger = LoggerFactory.getLogger(AppShellController.class);

    @FXML
    public void initialize() {
        logger.info("Initializing AppShellController and registering global bid listeners.");
        
        // Register global listener for auction closed
        SocketClient.getInstance().addEventListener(MessageType.AUCTION_CLOSED, response -> {
            try {
                com.auction.common.dto.auction.AuctionEventDto event = JsonMapper.getInstance().convertData(response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);
                if (event != null) {
                    boolean isWinner = event.winnerUsername() != null && event.winnerUsername().equals(com.auction.client.util.SceneManager.getCurrentUsername());
                    
                    String title = "Auction Ended";
                    String message = String.format("Auction #%d has ended. Status: %s", event.auctionId(), event.status());

                    if (event.status() == com.auction.common.enums.AuctionStatus.FINISHED) {
                        if (isWinner) {
                            title = "Congratulations!";
                            message = String.format("You won Auction #%d with a bid of $%,.2f!", event.auctionId(), event.finalPrice().doubleValue());
                        } else {
                            // Check if current user is seller (we don't have sellerId in event, but we can infer from dashboard refresh later)
                            // For now, general sold message
                            title = "Item Sold!";
                            message = String.format("Auction #%d finished at $%,.2f.", event.auctionId(), event.finalPrice().doubleValue());
                        }
                    }
                    
                    NotificationManager.showToast(title, message);
                    
                    // Trigger a dashboard refresh to update balance/stats for everyone (Buyer gets deducted, Seller gets paid)
                    new com.auction.client.service.AuctionClientService().getDashboard().thenAccept(dashResponse -> {
                        if (dashResponse.isSuccess()) {
                            com.auction.common.dto.dashboard.DashboardDto stats = dashResponse.getData();
                            Platform.runLater(() -> {
                                com.auction.client.util.SceneManager.setCurrentBalances(stats.balance(), stats.lockedBalance());
                            });
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("Error processing global AUCTION_CLOSED notification", e);
            }
        });

        // Register global listener for system notifications (e.g., First Bid)
        SocketClient.getInstance().addEventListener(MessageType.SYSTEM_NOTIFICATION, response -> {
            try {
                com.auction.common.dto.notification.SystemNotificationDto event = JsonMapper.getInstance().convertData(response.getData(), com.auction.common.dto.notification.SystemNotificationDto.class);
                if (event != null) {
                    NotificationManager.showToast(event.title(), event.message(), event.type());
                }
            } catch (Exception e) {
                logger.error("Error processing global SYSTEM_NOTIFICATION", e);
            }
        });
    }
}
