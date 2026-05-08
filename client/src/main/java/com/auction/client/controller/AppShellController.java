package com.auction.client.controller;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.NotificationManager;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.protocol.MessageType;
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
        
        // Register global listener for bid updates
        SocketClient.getInstance().addEventListener(MessageType.BID_UPDATE, response -> {
            try {
                BidUpdateEvent event = JsonMapper.getInstance().convertData(response.getData(), BidUpdateEvent.class);
                if (event != null) {
                    // Don't show toast for own bids
                    if (event.bidderUsername().equals(com.auction.client.util.SceneManager.getCurrentUsername())) {
                        return;
                    }

                    String message = String.format("%s placed a bid of $%,.2f", 
                            event.bidderUsername(), event.amount().doubleValue());
                    
                    NotificationManager.showToast("New Bid Received!", message);
                }
            } catch (Exception e) {
                logger.error("Error processing global BID_UPDATE notification", e);
            }
        });
    }
}
