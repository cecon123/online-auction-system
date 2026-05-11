package com.auction.client.util;

import com.auction.client.socket.SocketClient;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.protocol.MessageType;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for showing toast-like notifications in the application. */
public final class NotificationManager {

  private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);
  private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
  private static final double TOAST_WIDTH = 300;
  private static final double TOAST_HEIGHT = 80;
  private static final Duration DISPLAY_TIME = Duration.seconds(3);

  private NotificationManager() {}

  /** Initializes global event listeners for notifications. */
  public static void initialize() {
    JsonMapper mapper = JsonMapper.getInstance();
    SocketClient socket = SocketClient.getInstance();

    socket.addEventListener(
        MessageType.BID_UPDATE,
        response -> {
          try {
            BidUpdateEvent event = mapper.convertData(response.getData(), BidUpdateEvent.class);

            // Don't show toast for own bids to avoid cluttering the UI
            String currentUsername = SceneManager.getCurrentUsername();
            if (event.bidderUsername().equals(currentUsername)) {
              return;
            }

            String title = "New Bid Placed!";
            String message =
                String.format(
                    "%s bid %s on Auction #%d",
                    event.bidderUsername(),
                    CURRENCY_FORMAT.format(event.amount()),
                    event.auctionId());
            showToast(title, message);
          } catch (Exception e) {
            logger.error("Error processing BID_UPDATE for notification", e);
          }
        });

    socket.addEventListener(
        MessageType.AUCTION_CLOSED,
        response -> {
          try {
            com.auction.common.dto.auction.AuctionEventDto event =
                mapper.convertData(
                    response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);

            String title = "Auction Ended!";
            String message;
            if (event.winnerUsername() != null) {
              message =
                  String.format(
                      "Auction #%d closed. Winner: %s with %s",
                      event.auctionId(),
                      event.winnerUsername(),
                      CURRENCY_FORMAT.format(event.finalPrice()));
            } else {
              message = String.format("Auction #%d closed with no winner.", event.auctionId());
            }
            showToast(title, message);
          } catch (Exception e) {
            logger.error("Error processing AUCTION_CLOSED for notification", e);
          }
        });

    socket.addEventListener(
        MessageType.TIME_EXTENDED,
        response -> {
          try {
            com.auction.common.dto.auction.AuctionEventDto event =
                mapper.convertData(
                    response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);

            String title = "Time Extended!";
            String message =
                String.format(
                    "Auction #%d time has been extended due to last-minute bidding!",
                    event.auctionId());
            showToast(title, message);
          } catch (Exception e) {
            logger.error("Error processing TIME_EXTENDED for notification", e);
          }
        });

    socket.addEventListener(
        MessageType.SYSTEM_NOTIFICATION,
        response -> {
          try {
            com.auction.common.dto.notification.SystemNotificationDto event =
                mapper.convertData(
                    response.getData(),
                    com.auction.common.dto.notification.SystemNotificationDto.class);
            showToast(event.title(), event.message(), event.type());
          } catch (Exception e) {
            logger.error("Error processing SYSTEM_NOTIFICATION", e);
          }
        });

    logger.info("NotificationManager initialized with global listeners.");
  }

  /**
   * Shows a toast notification.
   *
   * @param title The notification title.
   * @param message The notification message.
   * @param type The notification type (e.g. INFO, SUCCESS, WARNING)
   */
  public static void showToast(String title, String message, String type) {
    Platform.runLater(() -> createAndShowToast(title, message, type));
  }

  public static void showToast(String title, String message) {
    showToast(title, message, "INFO");
  }

  private static void createAndShowToast(String title, String message, String type) {
    Stage toastStage = new Stage();
    toastStage.initStyle(StageStyle.TRANSPARENT);
    toastStage.setAlwaysOnTop(true);

    VBox root = new VBox(5);
    root.setAlignment(Pos.CENTER_LEFT);
    root.setPadding(new Insets(15));

    String bgColor = "#3525cd"; // Default INFO (purple)
    if ("SUCCESS".equalsIgnoreCase(type)) {
      bgColor = "#2e7d32"; // Green
    } else if ("WARNING".equalsIgnoreCase(type) || "ERROR".equalsIgnoreCase(type)) {
      bgColor = "#d32f2f"; // Red
    }

    root.setStyle(
        "-fx-background-color: "
            + bgColor
            + "; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

    Label messageLabel = new Label(message);
    messageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
    messageLabel.setWrapText(true);

    root.getChildren().addAll(titleLabel, messageLabel);

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    toastStage.setScene(scene);

    // Position at bottom-right of primary screen
    javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
    toastStage.setX(screenBounds.getMaxX() - TOAST_WIDTH - 20);
    toastStage.setY(screenBounds.getMaxY() - TOAST_HEIGHT - 20);
    toastStage.setWidth(TOAST_WIDTH);

    // Fade in
    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);

    // Fade out
    FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
    fadeOut.setFromValue(1);
    fadeOut.setToValue(0);
    fadeOut.setDelay(DISPLAY_TIME);
    fadeOut.setOnFinished(e -> toastStage.close());

    toastStage.show();
    fadeIn.play();
    fadeOut.play();

    logger.info("Toast notification shown: {} - {}", title, message);
  }
}
