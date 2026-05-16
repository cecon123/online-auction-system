package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.NotificationManager;
import com.auction.client.util.PriceChartManager;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.dto.dashboard.DashboardDto;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveBiddingController {

  private static final Logger logger = LoggerFactory.getLogger(LiveBiddingController.class);
  private static final NumberFormat USD_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

  // ── Empty State / Content Toggle ──
  @FXML private VBox emptyStatePane;
  @FXML private VBox biddingContentPane;

  // ── Header ──
  @FXML private Label countdownHeaderLabel;
  @FXML private Label countdownLabel;
  @FXML private Label statusLabel;
  @FXML private Label auctionTitleLabel;
  @FXML private Label auctionSubtitleLabel;

  // ── Price / Bid ──
  @FXML private Label currentPriceLabel;
  @FXML private Label highestBidderLabel;
  @FXML private Label reservePriceLabel;
  @FXML private TextField bidAmountField;
  @FXML private Label manualBidMessageLabel;
  @FXML private Button confirmBidButton;
  @FXML private Button quickBid10Button;
  @FXML private Button quickBid50Button;
  @FXML private Button quickBid100Button;
  @FXML private Button quickBid500Button;

  // ── Bid History ──
  @FXML private ScrollPane bidHistoryScrollPane;
  @FXML private VBox bidHistoryContainer;

  // ── Area Chart ──
  @FXML private AreaChart<String, Number> priceChart;
  @FXML private CategoryAxis timeAxis;
  @FXML private NumberAxis priceAxis;

  // ── Auto Bid ──
  @FXML private Label autoBidStatusLabel;
  @FXML private Label autoBidMessageLabel;
  @FXML private Label autoCurrentPriceLabel;
  @FXML private Label autoUserPositionLabel;
  @FXML private TextField autoMaxBudgetField;
  @FXML private ComboBox<String> autoStepComboBox;
  @FXML private Label autoMaxBudgetLabel;
  @FXML private Label autoNextBidLabel;
  @FXML private Label autoRemainingBudgetLabel;
  @FXML private Label autoLastActionLabel;
  @FXML private Button enableAutoBidButton;
  @FXML private Button updateAutoBidButton;
  @FXML private Button disableAutoBidButton;

  private final AuctionClientService auctionService = new AuctionClientService();
  private final Consumer<Response<?>> bidUpdateListener = this::handleBidUpdate;
  private final Consumer<Response<?>> auctionClosedListener = this::handleAuctionClosed;
  private final Consumer<Response<?>> timeExtendedListener = this::handleTimeExtended;

  private Long auctionId;
  private BigDecimal minimumIncrement = new BigDecimal("1.00");
  private BigDecimal currentPrice = BigDecimal.ZERO;
  private BigDecimal autoMaxBudget = new BigDecimal("20000");
  private BigDecimal autoStep = new BigDecimal("500");
  private boolean autoBidEnabled = false;
  private XYChart.Series<String, Number> priceSeries;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Timeline countdownTimeline;
  private PriceChartManager chartManager;
  private Timeline flashTimeline;

  public void setAuctionId(Long auctionId) {
    this.auctionId = auctionId;
    showBiddingContent();
    loadAuctionData();
    loadBidHistory();
    subscribeToUpdates();
  }

  @FXML
  private void initialize() {
    autoMaxBudgetField.setText(autoMaxBudget.toPlainString());
    autoStepComboBox.setItems(FXCollections.observableArrayList("500", "1000", "2500", "5000"));
    autoStepComboBox.setValue("500");

    this.chartManager = new PriceChartManager(priceChart);
    refreshAutoBidPanel();

    manualBidMessageLabel.setText("");
    autoBidMessageLabel.setText("Connect to server...");
    autoLastActionLabel.setText("Waiting for auction data.");

    // Show empty state by default
    showEmptyState();
    setupCountdownTimeline();
    setupFlashTimeline();
  }

  // ── Empty State Toggle ───────────────────────────────

  private void showEmptyState() {
    emptyStatePane.setVisible(true);
    emptyStatePane.setManaged(true);
    biddingContentPane.setVisible(false);
    biddingContentPane.setManaged(false);
  }

  private void showBiddingContent() {
    emptyStatePane.setVisible(false);
    emptyStatePane.setManaged(false);
    biddingContentPane.setVisible(true);
    biddingContentPane.setManaged(true);
  }

  // ── Quick Bid Handlers ───────────────────────────────

  @FXML
  private void handleQuickBid10() {
    placeQuickBid(new BigDecimal("10"));
  }

  @FXML
  private void handleQuickBid50() {
    placeQuickBid(new BigDecimal("50"));
  }

  @FXML
  private void handleQuickBid100() {
    placeQuickBid(new BigDecimal("100"));
  }

  @FXML
  private void handleQuickBid500() {
    placeQuickBid(new BigDecimal("500"));
  }

  private void placeQuickBid(BigDecimal increment) {
    if (auctionId == null) {
      showManualMessage("No auction selected.");
      return;
    }
    if (!guardBiddingAllowed()) {
      return;
    }
    BigDecimal quickBid = currentPrice.add(increment);
    auctionService
        .placeBid(auctionId, quickBid)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      showManualMessage("Quick bid " + formatMoney(quickBid) + " placed!", true);
                      refreshBalanceFromServer();
                    } else {
                      showManualMessage("Error: " + response.getMessage(), false);
                    }
                  });
            });
  }

  // ── Bid History Cards ────────────────────────────────

  private void addBidHistoryCard(String time, String bidder, BigDecimal amount, boolean isLatest) {
    boolean isSelf = bidder != null && bidder.equals(SceneManager.getCurrentUsername());

    HBox card = new HBox(12);
    card.setAlignment(Pos.CENTER_LEFT);
    card.getStyleClass().add(isSelf ? "bid-card-self" : "bid-card");
    if (isLatest) {
      card.getStyleClass().add("bid-card-latest");
    }

    // Left Status Icon (Modern circle with icon)
    StackPane iconPane = new StackPane();
    iconPane.getStyleClass().add("bid-card-icon-pane");
    if (isLatest) iconPane.getStyleClass().add("bid-card-icon-latest");

    FontIcon icon = new FontIcon(isLatest ? "mdi2g-gavel" : "mdi2c-check-circle-outline");
    icon.setIconSize(16);
    iconPane.getChildren().add(icon);

    // Main Content (Name, You tag, Time)
    VBox contentBox = new VBox(2);
    HBox.setHgrow(contentBox, Priority.ALWAYS);

    HBox topRow = new HBox(8);
    topRow.setAlignment(Pos.CENTER_LEFT);

    Label nameLabel = new Label(bidder != null ? bidder : "Unknown");
    nameLabel.getStyleClass().add("bid-card-name");
    topRow.getChildren().add(nameLabel);

    if (isSelf) {
      Label youTag = new Label("YOU");
      youTag.getStyleClass().add("bid-card-you-tag");
      topRow.getChildren().add(youTag);
    }

    Label timeLabel = new Label(time);
    timeLabel.getStyleClass().add("bid-card-time");

    contentBox.getChildren().addAll(topRow, timeLabel);

    // Amount (Highlight if latest)
    Label amountLabel = new Label(formatMoney(amount));
    amountLabel.getStyleClass().add(isLatest ? "bid-card-amount-latest" : "bid-card-amount");

    card.getChildren().addAll(iconPane, contentBox, amountLabel);

    // Add to container
    if (isLatest) {
      bidHistoryContainer.getChildren().add(0, card);
    } else {
      bidHistoryContainer.getChildren().add(card);
    }

    // Performance Optimization: Limit history items to top 50 to avoid UI lag
    if (bidHistoryContainer.getChildren().size() > 50) {
      bidHistoryContainer.getChildren().remove(50, bidHistoryContainer.getChildren().size());
    }

    // Auto-scroll to top
    bidHistoryScrollPane.setVvalue(0);
  }

  private void demoteLatestCard() {
    if (bidHistoryContainer.getChildren().isEmpty()) return;

    // The newest bid is always at index 0
    javafx.scene.Node node = bidHistoryContainer.getChildren().get(0);
    if (node instanceof HBox card) {
      card.getStyleClass().remove("bid-card-latest");

      // Icon pane is the first child
      if (!card.getChildren().isEmpty()
          && card.getChildren().get(0) instanceof StackPane iconPane) {
        iconPane.getStyleClass().remove("bid-card-icon-latest");
        if (!iconPane.getChildren().isEmpty()
            && iconPane.getChildren().get(0) instanceof FontIcon icon) {
          icon.setIconLiteral("mdi2c-check-circle-outline");
        }
      }

      // Amount label is the third child (index 2)
      if (card.getChildren().size() > 2 && card.getChildren().get(2) instanceof Label amountLabel) {
        amountLabel.getStyleClass().remove("bid-card-amount-latest");
        if (!amountLabel.getStyleClass().contains("bid-card-amount")) {
          amountLabel.getStyleClass().add("bid-card-amount");
        }
      }
    }
  }

  // ── Existing Logic (adapted) ─────────────────────────

  private void subscribeToUpdates() {
    if (auctionId == null) return;
    auctionService.subscribeAuction(auctionId);
    SocketClient socket = SocketClient.getInstance();
    socket.addEventListener(MessageType.BID_UPDATE, bidUpdateListener);
    socket.addEventListener(MessageType.AUCTION_CLOSED, auctionClosedListener);
    socket.addEventListener(MessageType.AUCTION_CANCELED, auctionClosedListener);
    socket.addEventListener(MessageType.TIME_EXTENDED, timeExtendedListener);
  }

  private String lastCountdownText = "";

  private void setupCountdownTimeline() {
    countdownTimeline = new Timeline(new KeyFrame(Duration.millis(250), e -> updateCountdown()));
    countdownTimeline.setCycleCount(Animation.INDEFINITE);
    countdownTimeline.play();
  }

  private void updateCountdown() {
    if (endTime == null) {
      updateCountdownUI("--:--:--");
      return;
    }
    String status = statusLabel.getText();
    if ("FINISHED".equals(status) || "PAID".equals(status) || "CANCELED".equals(status)) {
      countdownHeaderLabel.setText("ENDED");
      updateCountdownUI("--:--:--");
      return;
    }
    LocalDateTime targetTime = endTime;
    if ("OPEN".equals(status)) {
      countdownHeaderLabel.setText("START IN");
      targetTime = startTime;
      if (targetTime == null) {
        updateCountdownUI("Opening...");
        return;
      }
    } else {
      countdownHeaderLabel.setText("TIME REMAINING");
    }
    java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), targetTime);
    if (duration.isNegative() || duration.isZero()) {
      updateCountdownUI("OPEN".equals(status) ? "Starting..." : "00:00:00");
      updateBiddingControlsState();
      return;
    }
    long s = duration.toSeconds();
    String timeText = String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    updateCountdownUI(timeText);
  }

  private void updateCountdownUI(String text) {
    if (!text.equals(lastCountdownText)) {
      countdownLabel.setText(text);
      lastCountdownText = text;
    }
  }

  private void loadAuctionData() {
    if (auctionId == null) return;
    auctionService
        .getAuctionDetail(auctionId)
        .thenAccept(
            response -> {
              if (response.isSuccess()) {
                AuctionDetailDto detail = response.getData();
                Platform.runLater(
                    () -> {
                      this.currentPrice = detail.currentPrice();
                      this.startTime = detail.startTime();
                      this.endTime = detail.endTime();
                      this.statusLabel.setText(detail.status().toString());
                      updateStatusStyle(detail.status().toString());
                      this.auctionTitleLabel.setText(detail.title());
                      this.auctionSubtitleLabel.setText(
                          "Lot #"
                              + detail.auctionId()
                              + " · "
                              + detail.itemType()
                              + " · "
                              + detail.status());
                      this.highestBidderLabel.setText(
                          detail.highestBidderUsername() != null
                              ? detail.highestBidderUsername()
                              : "No bids");
                      this.reservePriceLabel.setText(
                          detail.reservePrice() != null
                              ? formatMoney(detail.reservePrice())
                              : "None");
                      refreshCurrentPrice();
                      refreshAutoBidPanel();
                      autoBidMessageLabel.setText("Realtime updates active.");
                      autoLastActionLabel.setText("Watching: " + detail.title());
                      updateCountdown();
                      updateBiddingControlsState();
                      if (!isAuctionAcceptingBids()) {
                        showManualMessage(getInactiveAuctionMessage(), false);
                      }
                    });
              }
            });
  }

  private void loadBidHistory() {
    if (auctionId == null) return;

    // Show loading state immediately to prevent layout shift and inform user
    Platform.runLater(
        () -> {
          bidHistoryContainer.getChildren().clear();
          Label loadingLabel = new Label("Loading history...");
          loadingLabel.getStyleClass().add("bid-history-subtitle");
          loadingLabel.setPadding(new Insets(20));
          bidHistoryContainer.setAlignment(Pos.CENTER);
          bidHistoryContainer.getChildren().add(loadingLabel);
        });

    auctionService
        .getBidHistory(auctionId)
        .thenAccept(
            response -> {
              if (response.isSuccess()) {
                List<PlaceBidResponse> history = response.getData();
                Platform.runLater(
                    () -> {
                      bidHistoryContainer.getChildren().clear();

                      if (history.isEmpty()) {
                        Label emptyLabel = new Label("No bids yet.");
                        emptyLabel.getStyleClass().add("bid-history-subtitle");
                        emptyLabel.setPadding(new Insets(20));
                        bidHistoryContainer.setAlignment(Pos.CENTER);
                        bidHistoryContainer.getChildren().add(emptyLabel);
                      } else {
                        bidHistoryContainer.setAlignment(Pos.TOP_LEFT);
                        // History comes newest-first; reverse for chronological chart
                        java.util.List<PlaceBidResponse> chronological =
                            new java.util.ArrayList<>(history);
                        java.util.Collections.reverse(chronological);

                        // Update chart using manager
                        chartManager.setData(chronological);

                        // Populate bid history cards (newest first)
                        boolean first = true;
                        for (PlaceBidResponse bid : history) {
                          String time = bid.timestamp().format(TIME_FMT);
                          addBidHistoryCard(
                              time, bid.highestBidderUsername(), bid.currentPrice(), first);
                          first = false;
                        }
                      }
                    });
              }
            });
  }

  private void handleBidUpdate(Response<?> response) {
    BidUpdateEvent event =
        JsonMapper.getInstance().convertData(response.getData(), BidUpdateEvent.class);
    if (event.auctionId().equals(auctionId)) {
      Platform.runLater(
          () -> {
            this.currentPrice = event.amount();
            this.highestBidderLabel.setText(event.bidderUsername());
            this.endTime = event.newEndTime();

            // 1. Remove "No bids yet" label if it exists
            bidHistoryContainer
                .getChildren()
                .removeIf(
                    node ->
                        node instanceof Label && ((Label) node).getText().contains("No bids yet"));
            bidHistoryContainer.setAlignment(Pos.TOP_LEFT);

            // 2. Demote current latest card
            demoteLatestCard();

            // 3. Add new latest card
            String time = LocalDateTime.now().format(TIME_FMT);
            addBidHistoryCard(time, event.bidderUsername(), this.currentPrice, true);

            chartManager.addPricePoint(this.currentPrice);
            refreshCurrentPrice();
            triggerColorFlash();
            refreshAutoBidPanel();
            updateCountdown();

            if (autoBidEnabled
                && !event.bidderUsername().equals(SceneManager.getCurrentUsername())) {
              triggerAutoBidIfPossible();
            }
          });
    }
  }

  @FXML
  private void handlePlaceBid() {
    if (auctionId == null) {
      showManualMessage("No auction selected.");
      return;
    }
    if (!guardBiddingAllowed()) {
      return;
    }
    BigDecimal manualBid = parsePositiveMoney(bidAmountField.getText());
    if (manualBid == null) {
      showManualMessage("Please enter a valid bid amount.");
      return;
    }
    BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);
    if (manualBid.compareTo(minimumNextBid) < 0) {
      showManualMessage("Minimum next bid is " + formatMoney(minimumNextBid) + ".");
      return;
    }
    auctionService
        .placeBid(auctionId, manualBid)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      showManualMessage("Bid placed successfully!", true);
                      bidAmountField.clear();
                      refreshBalanceFromServer();
                    } else {
                      showManualMessage("Error: " + response.getMessage(), false);
                    }
                  });
            });
  }

  private long lastBalanceRefresh = 0;

  private void refreshBalanceFromServer() {
    long now = System.currentTimeMillis();
    if (now - lastBalanceRefresh < 2000) return; // Debounce 2s
    lastBalanceRefresh = now;

    auctionService
        .getDashboard()
        .thenAccept(
            dashResponse -> {
              if (dashResponse.isSuccess()) {
                DashboardDto stats = dashResponse.getData();
                Platform.runLater(
                    () -> SceneManager.setCurrentBalances(stats.balance(), stats.lockedBalance()));
              }
            });
  }

  @FXML
  private void handleEnableAutoBid() {
    if (auctionId == null) {
      NotificationManager.showToast("Warning", "No auction selected.", "WARNING");
      return;
    }
    if (!guardBiddingAllowed()) {
      return;
    }
    BigDecimal parsedMaxBudget = parsePositiveMoney(autoMaxBudgetField.getText());
    BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());
    if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) {
      refreshAutoBidPanel();
      return;
    }
    autoMaxBudget = parsedMaxBudget;
    autoStep = parsedStep;
    autoBidEnabled = true;
    NotificationManager.showToast("Success", "Auto bidding is active.", "SUCCESS");
    autoLastActionLabel.setText("Watching this auction.");
    refreshAutoBidPanel();
    if (!SceneManager.getCurrentUsername().equals(highestBidderLabel.getText())) {
      triggerAutoBidIfPossible();
    }
  }

  @FXML
  private void handleUpdateAutoBid() {
    if (!guardBiddingAllowed()) {
      return;
    }
    BigDecimal parsedMaxBudget = parsePositiveMoney(autoMaxBudgetField.getText());
    BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());
    if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) {
      refreshAutoBidPanel();
      return;
    }
    autoMaxBudget = parsedMaxBudget;
    autoStep = parsedStep;
    autoBidEnabled = true;
    NotificationManager.showToast("Success", "Auto bidding budget was updated.", "SUCCESS");
    autoLastActionLabel.setText("Max budget: " + formatMoney(autoMaxBudget) + ".");
    refreshAutoBidPanel();
  }

  @FXML
  private void handleDisableAutoBid() {
    autoBidEnabled = false;
    NotificationManager.showToast("Info", "Auto bidding disabled. You can enable it again anytime.");
    autoLastActionLabel.setText("Auto bidding is off.");
    refreshAutoBidPanel();
  }

  private boolean validateAutoBidInput(BigDecimal parsedMaxBudget, BigDecimal parsedStep) {
    if (parsedMaxBudget == null) {
      NotificationManager.showToast("Error", "Please enter a valid maximum budget.", "ERROR");
      return false;
    }
    if (parsedStep == null) {
      NotificationManager.showToast("Error", "Please select a valid bid step.", "ERROR");
      return false;
    }
    BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);
    if (parsedMaxBudget.compareTo(minimumNextBid) < 0) {
      NotificationManager.showToast("Error", "Your maximum budget must be at least " + formatMoney(minimumNextBid) + ".", "ERROR");
      return false;
    }
    if (parsedMaxBudget.compareTo(SceneManager.getCurrentBalance()) > 0) {
      NotificationManager.showToast("Error",
          "Your maximum budget cannot exceed wallet balance "
              + formatMoney(SceneManager.getCurrentBalance())
              + ".", "ERROR");
      return false;
    }
    if (parsedStep.compareTo(minimumIncrement) < 0) {
      NotificationManager.showToast("Error", "Bid step must be at least " + formatMoney(minimumIncrement) + ".", "ERROR");
      return false;
    }
    return true;
  }

  private void triggerAutoBidIfPossible() {
    if (!isAuctionAcceptingBids()) {
      autoBidEnabled = false;
      autoLastActionLabel.setText(getInactiveAuctionMessage());
      refreshAutoBidPanel();
      return;
    }
    BigDecimal nextAutoBid = currentPrice.add(autoStep);
    if (nextAutoBid.compareTo(autoMaxBudget) > 0) {
      autoBidEnabled = false;
      autoLastActionLabel.setText("Maximum budget reached.");
      NotificationManager.showToast("Info", "Auto bidding stopped because your maximum budget was reached.");
      refreshAutoBidPanel();
      return;
    }
    logger.info("Auto-bidding placing bid of {} for auction {}", nextAutoBid, auctionId);
    auctionService
        .placeBid(auctionId, nextAutoBid)
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.isSuccess()) {
                      autoLastActionLabel.setText(
                          "Auto bid placed: " + formatMoney(nextAutoBid) + ".");
                      NotificationManager.showToast("Success", "Auto bidding responded successfully.", "SUCCESS");
                    } else {
                      autoLastActionLabel.setText("Auto bid failed.");
                      autoBidEnabled = false;
                      NotificationManager.showToast("Error", "Auto bid failed.", "ERROR");
                    }
                    refreshAutoBidPanel();
                  });
            });
  }

  private void setupFlashTimeline() {
    flashTimeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new javafx.animation.KeyValue(
                    currentPriceLabel.textFillProperty(), Color.web("#059669"))),
            new KeyFrame(
                Duration.millis(500),
                new javafx.animation.KeyValue(
                    currentPriceLabel.textFillProperty(), Color.web("#3525cd"))));
  }

  private void triggerColorFlash() {
    if (flashTimeline != null) {
      flashTimeline.playFromStart();
    }
  }

  // ── UI Helpers ───────────────────────────────────────

  private void updateStatusStyle(String status) {
    statusLabel
        .getStyleClass()
        .removeAll(
            "status-badge",
            "status-running",
            "status-open",
            "status-finished",
            "status-paid",
            "status-cancelled",
            "status-canceled");
    statusLabel.getStyleClass().add("status-badge");
    switch (status) {
      case "RUNNING" -> statusLabel.getStyleClass().add("status-running");
      case "OPEN" -> statusLabel.getStyleClass().add("status-open");
      case "PAID" -> statusLabel.getStyleClass().add("status-paid");
      case "CANCELED", "CANCELLED" -> statusLabel.getStyleClass().add("status-cancelled");
      default -> statusLabel.getStyleClass().add("status-finished");
    }
  }

  private void refreshCurrentPrice() {
    currentPriceLabel.setText(formatMoney(currentPrice));
    autoCurrentPriceLabel.setText(formatMoney(currentPrice));
  }

  private void refreshAutoBidPanel() {
    refreshCurrentPrice();
    boolean userIsLeading = SceneManager.getCurrentUsername().equals(highestBidderLabel.getText());
    autoUserPositionLabel.setText(userIsLeading ? "Leading" : "Outbid");
    autoUserPositionLabel
        .getStyleClass()
        .removeAll("auto-position-leading", "auto-position-outbid");
    autoUserPositionLabel
        .getStyleClass()
        .add(userIsLeading ? "auto-position-leading" : "auto-position-outbid");
    autoMaxBudgetLabel.setText(formatMoney(autoMaxBudget));

    if (autoBidEnabled) {
      setAutoStatus("ON", "auto-status-active");
      BigDecimal nextBid = currentPrice.add(autoStep);
      BigDecimal remaining = autoMaxBudget.subtract(currentPrice);
      autoNextBidLabel.setText(
          nextBid.compareTo(autoMaxBudget) <= 0 ? formatMoney(nextBid) : "Max reached");
      autoRemainingBudgetLabel.setText(
          remaining.compareTo(BigDecimal.ZERO) > 0 ? formatMoney(remaining) : "$0.00");
      enableAutoBidButton.setVisible(false);
      enableAutoBidButton.setManaged(false);
      updateAutoBidButton.setVisible(true);
      updateAutoBidButton.setManaged(true);
      disableAutoBidButton.setVisible(true);
      disableAutoBidButton.setManaged(true);
    } else {
      setAutoStatus("OFF", "auto-status-off");
      autoNextBidLabel.setText("-");
      autoRemainingBudgetLabel.setText("-");
      enableAutoBidButton.setVisible(true);
      enableAutoBidButton.setManaged(true);
      updateAutoBidButton.setVisible(false);
      updateAutoBidButton.setManaged(false);
      disableAutoBidButton.setVisible(false);
      disableAutoBidButton.setManaged(false);
    }
    updateBiddingControlsState();
  }

  private boolean guardBiddingAllowed() {
    updateBiddingControlsState();
    if (isAuctionAcceptingBids()) {
      return true;
    }
    String message = getInactiveAuctionMessage();
    showManualMessage(message, false);
    showAutoMessage(message, false);
    return false;
  }

  private boolean isAuctionAcceptingBids() {
    return "RUNNING".equals(statusLabel.getText())
        && endTime != null
        && LocalDateTime.now().isBefore(endTime);
  }

  private String getInactiveAuctionMessage() {
    String status = statusLabel.getText();
    if ("OPEN".equals(status)) {
      return "This auction has not started yet.";
    }
    if ("RUNNING".equals(status)) {
      return "This auction has ended. Waiting for settlement.";
    }
    if ("FINISHED".equals(status)) {
      return "This auction has ended and payment is being settled.";
    }
    if ("PAID".equals(status)) {
      return "This auction has been paid.";
    }
    if ("CANCELED".equals(status) || "CANCELLED".equals(status)) {
      return "This auction has been canceled.";
    }
    return "Bidding is not available for this auction.";
  }

  private void updateBiddingControlsState() {
    boolean disabled = !isAuctionAcceptingBids();
    bidAmountField.setDisable(disabled);
    confirmBidButton.setDisable(disabled);
    quickBid10Button.setDisable(disabled);
    quickBid50Button.setDisable(disabled);
    quickBid100Button.setDisable(disabled);
    quickBid500Button.setDisable(disabled);
    enableAutoBidButton.setDisable(disabled);
    updateAutoBidButton.setDisable(disabled);
    disableAutoBidButton.setDisable(disabled);
    autoMaxBudgetField.setDisable(disabled);
    autoStepComboBox.setDisable(disabled);
    if (disabled) {
      autoBidEnabled = false;
      setAutoStatus("OFF", "auto-status-off");
      autoNextBidLabel.setText("-");
      autoRemainingBudgetLabel.setText("-");
      autoLastActionLabel.setText(getInactiveAuctionMessage());
    }
  }

  private void setAutoStatus(String text, String styleClass) {
    autoBidStatusLabel.setText(text);
    autoBidStatusLabel.setMinWidth(72);
    autoBidStatusLabel.setPrefWidth(72);
    autoBidStatusLabel.setMaxWidth(72);
    autoBidStatusLabel
        .getStyleClass()
        .removeAll("auto-status-active", "auto-status-off", "auto-status-max");
    autoBidStatusLabel.getStyleClass().add(styleClass);
  }

  private BigDecimal parsePositiveMoney(String value) {
    if (value == null || value.trim().isBlank()) return null;
    try {
      BigDecimal parsed = new BigDecimal(value.trim().replace(",", ""));
      return parsed.compareTo(BigDecimal.ZERO) <= 0
          ? null
          : parsed.setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String formatMoney(BigDecimal value) {
    return USD_FORMAT.format(value);
  }

  private void showManualMessage(String message, boolean isSuccess) {
    manualBidMessageLabel.setText(message);
    manualBidMessageLabel.getStyleClass().removeAll("msg-success", "msg-error");
    manualBidMessageLabel.getStyleClass().add(isSuccess ? "msg-success" : "msg-error");
  }

  private void showAutoMessage(String message, boolean isSuccess) {
    autoBidMessageLabel.setText(message);
    autoBidMessageLabel.getStyleClass().removeAll("msg-success", "msg-error");
    autoBidMessageLabel.getStyleClass().add(isSuccess ? "msg-success" : "msg-error");
  }

  private void showManualMessage(String message) {
    showManualMessage(message, false);
  }

  private void showAutoMessage(String message) {
    showAutoMessage(message, false);
  }

  private void handleAuctionClosed(Response<?> response) {
    com.auction.common.dto.auction.AuctionEventDto event =
        JsonMapper.getInstance()
            .convertData(response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);
    if (event.auctionId().equals(auctionId)) {
      Platform.runLater(
          () -> {
            String finalStatus = event.status().name();
            this.statusLabel.setText(finalStatus);
            updateStatusStyle(finalStatus);

            updateBiddingControlsState();

            boolean isPaid = "PAID".equals(finalStatus);
            boolean isFinished = "FINISHED".equals(finalStatus);
            boolean isSuccessful = isPaid || isFinished;

            if (!isSuccessful) {
              showManualMessage("This auction was canceled (e.g. reserve not met).", false);
            } else if (isPaid) {
              showManualMessage("This auction has ended and payment is complete.", true);
            } else {
              showManualMessage("This auction has ended and payment is being settled.", true);
            }

            if (event.winnerUsername() != null) {
              highestBidderLabel.setText(event.winnerUsername());
              boolean currentUserWon =
                  event.winnerUsername().equals(SceneManager.getCurrentUsername());
              if (isPaid && currentUserWon) {
                showManualMessage("Congratulations! You won and paid for this auction.", true);
              } else if (isFinished && currentUserWon) {
                showManualMessage("Congratulations! You won. Payment is being settled.", true);
              } else if (!isSuccessful && currentUserWon) {
                showManualMessage(
                    "Auction ended. Reserve price wasn't met. You will be refunded.", false);
              }
            }
          });
    }
  }

  private void handleTimeExtended(Response<?> response) {
    com.auction.common.dto.auction.AuctionEventDto event =
        JsonMapper.getInstance()
            .convertData(response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);
    if (event.auctionId().equals(auctionId)) {
      Platform.runLater(
          () -> {
            this.endTime = event.newEndTime();
            showManualMessage("Time extended! More time to bid.", true);
            updateCountdown();
          });
    }
  }
}
