package com.auction.client.controller;

import com.auction.client.socket.ConnectionState;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.NotificationManager;
import com.auction.client.util.SceneManager;
import com.auction.common.protocol.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main authenticated application shell.
 *
 * <p>The shell contains: - Sidebar on the left - TopBar on the top - Dynamic center content
 */
public class AppShellController {
  private static final Logger logger = LoggerFactory.getLogger(AppShellController.class);

  @FXML private HBox disconnectedBanner;

  @FXML
  public void initialize() {
    logger.info("Initializing AppShellController and registering global bid listeners.");

    // Monitor connection state
    SocketClient.getInstance()
        .connectionStateProperty()
        .addListener(
            (obs, oldState, newState) -> {
              Platform.runLater(
                  () -> {
                    boolean isDisconnected = newState == ConnectionState.DISCONNECTED;
                    disconnectedBanner.setVisible(isDisconnected);
                    disconnectedBanner.setManaged(isDisconnected);
                  });
            });

    // Register global listener for auction closed
    SocketClient.getInstance()
        .addEventListener(
            MessageType.AUCTION_CLOSED,
            response -> {
              // Refresh a dashboard to update balance/stats for everyone (Buyer gets deducted, Seller gets paid)
              // Notification is handled globally by NotificationManager
              new com.auction.client.service.AuctionClientService()
                  .getDashboard()
                  .thenAccept(
                      dashResponse -> {
                        if (dashResponse.isSuccess()) {
                          com.auction.common.dto.dashboard.DashboardDto stats =
                              dashResponse.getData();
                          Platform.runLater(
                              () -> {
                                com.auction.client.util.SceneManager.setCurrentBalances(
                                    stats.balance(), stats.lockedBalance());
                              });
                        }
                      });
            });

    // Register global listener for system notifications (e.g., First Bid)
    SocketClient.getInstance()
        .addEventListener(
            MessageType.SYSTEM_NOTIFICATION,
            response -> {
              // Refresh balance/stats for any system notification (often involves financial changes)
              // Notification is handled globally by NotificationManager
              new com.auction.client.service.AuctionClientService()
                  .getDashboard()
                  .thenAccept(
                      dashResponse -> {
                        if (dashResponse.isSuccess()) {
                          com.auction.common.dto.dashboard.DashboardDto stats =
                              dashResponse.getData();
                          Platform.runLater(
                              () -> {
                                com.auction.client.util.SceneManager.setCurrentBalances(
                                    stats.balance(), stats.lockedBalance());
                              });
                        }
                      });
            });
  }

  @FXML
  private void handleDisconnectedLogout() {
    SocketClient.getInstance().disconnect();
    Platform.runLater(SceneManager::showLogin);
  }
}
