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
  private static final long BID_TOAST_THROTTLE_MS = 2500;
  private static long lastBidToastAt = 0;

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

            long now = System.currentTimeMillis();
            if (now - lastBidToastAt < BID_TOAST_THROTTLE_MS) {
              return;
            }
            lastBidToastAt = now;

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

            String currentUsername = SceneManager.getCurrentUsername();
            boolean isWinner =
                event.winnerUsername() != null && event.winnerUsername().equals(currentUsername);

            String title = "Auction Ended!";
            String message;
            String type = "INFO";
            Duration duration = DISPLAY_TIME;

            if (!isWinner) {
              return;
            }

            if (event.status() == com.auction.common.enums.AuctionStatus.PAID
                || event.status() == com.auction.common.enums.AuctionStatus.FINISHED) {
              title =
                  event.status() == com.auction.common.enums.AuctionStatus.PAID
                      ? "Payment Complete!"
                      : "Auction Won!";
              message =
                  String.format(
                      event.status() == com.auction.common.enums.AuctionStatus.PAID
                          ? "You won and paid for Auction #%d with a bid of %s."
                          : "You won Auction #%d with a bid of %s. Payment is being settled.",
                      event.auctionId(), CURRENCY_FORMAT.format(event.finalPrice()));
              type = "SUCCESS";
              duration = Duration.seconds(5); // 5 seconds for winner as requested
            } else {
              message =
                  String.format(
                      "Auction #%d was canceled. Any locked funds will be released.",
                      event.auctionId());
              type = "WARNING";
            }

            showToast(title, message, type, duration);
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
   * @param type The notification type (e.g. INFO, SUCCESS, WARNING, ERROR)
   */
  public static void showToast(String title, String message, String type) {
    showToast(title, message, type, DISPLAY_TIME);
  }

  /**
   * Shows a toast notification with a custom duration.
   *
   * @param title The notification title.
   * @param message The notification message.
   * @param type The notification type.
   * @param duration The display duration.
   */
  public static void showToast(String title, String message, String type, Duration duration) {
    Platform.runLater(() -> createAndShowToast(title, message, type, duration));
  }

  public static void showToast(String title, String message) {
    showToast(title, message, "INFO");
  }

  private static void createAndShowToast(
      String title, String message, String type, Duration duration) {
    Stage toastStage = new Stage();
    // Set the main application stage as owner if available
    javafx.stage.Stage owner = (javafx.stage.Stage) javafx.stage.Window.getWindows().stream()
        .filter(w -> w instanceof javafx.stage.Stage)
        .findFirst()
        .orElse(null);
    if (owner != null) {
      toastStage.initOwner(owner);
    }
    toastStage.initStyle(StageStyle.TRANSPARENT);
    toastStage.setAlwaysOnTop(true);
    // Prevents the toast from taking focus from the main application window
    toastStage.initModality(javafx.stage.Modality.NONE); 

    VBox root = new VBox(5);
    root.setAlignment(Pos.CENTER_LEFT);
    root.setPadding(new Insets(15));
    // Set mouse transparency to prevent interaction and accidental focus
    root.setMouseTransparent(true);

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
    fadeOut.setDelay(duration != null ? duration : DISPLAY_TIME);
    fadeOut.setOnFinished(e -> toastStage.close());

    toastStage.show();
    fadeIn.play();
    fadeOut.play();

    logger.info("Toast notification shown: {} - {}", title, message);
  }
}
