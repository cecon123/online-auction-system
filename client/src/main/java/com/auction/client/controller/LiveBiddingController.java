package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.util.PriceChartManager;

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
    @FXML private Button simulateOutbidButton;

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

        simulateOutbidButton.setVisible(false);
        simulateOutbidButton.setManaged(false);

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
    private void handleQuickBid10() { placeQuickBid(new BigDecimal("10")); }

    @FXML
    private void handleQuickBid50() { placeQuickBid(new BigDecimal("50")); }

    @FXML
    private void handleQuickBid100() { placeQuickBid(new BigDecimal("100")); }

    @FXML
    private void handleQuickBid500() { placeQuickBid(new BigDecimal("500")); }

    private void placeQuickBid(BigDecimal increment) {
        if (auctionId == null) {
            showManualMessage("No auction selected.");
            return;
        }
        BigDecimal quickBid = currentPrice.add(increment);
        auctionService.placeBid(auctionId, quickBid).thenAccept(response -> {
            Platform.runLater(() -> {
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

    private void addBidHistoryCard(String time, String bidder, BigDecimal amount) {
        boolean isSelf = bidder != null && bidder.equals(SceneManager.getCurrentUsername());
        boolean isLatest = bidHistoryContainer.getChildren().isEmpty();

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
        
        // Add to container (at top)
        bidHistoryContainer.getChildren().add(0, card);

        // Performance Optimization: Limit history items to top 50 to avoid UI lag
        if (bidHistoryContainer.getChildren().size() > 50) {
            bidHistoryContainer.getChildren().remove(50, bidHistoryContainer.getChildren().size());
        }

        // Auto-scroll to top
        bidHistoryScrollPane.setVvalue(0);
    }

    // ── Existing Logic (adapted) ─────────────────────────

    private void subscribeToUpdates() {
        if (auctionId == null) return;
        auctionService.subscribeAuction(auctionId);
        SocketClient socket = SocketClient.getInstance();
        socket.addEventListener(MessageType.BID_UPDATE, bidUpdateListener);
        socket.addEventListener(MessageType.AUCTION_CLOSED, auctionClosedListener);
        socket.addEventListener(MessageType.TIME_EXTENDED, timeExtendedListener);
    }

    private void setupCountdownTimeline() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdown() {
        if (endTime == null) { countdownLabel.setText("--:--:--"); return; }
        String status = statusLabel.getText();
        if ("FINISHED".equals(status) || "CANCELED".equals(status)) {
            countdownHeaderLabel.setText("ENDED");
            countdownLabel.setText("--:--:--");
            return;
        }
        LocalDateTime targetTime = endTime;
        if ("OPEN".equals(status)) {
            countdownHeaderLabel.setText("START IN");
            targetTime = startTime;
            if (targetTime == null) { countdownLabel.setText("Opening..."); return; }
        } else {
            countdownHeaderLabel.setText("TIME REMAINING");
        }
        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), targetTime);
        if (duration.isNegative() || duration.isZero()) {
            countdownLabel.setText("OPEN".equals(status) ? "Starting..." : "00:00:00");
            return;
        }
        long s = duration.toSeconds();
        countdownLabel.setText(String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60));
    }

    private void loadAuctionData() {
        if (auctionId == null) return;
        auctionService.getAuctionDetail(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                AuctionDetailDto detail = response.getData();
                Platform.runLater(() -> {
                    this.currentPrice = detail.currentPrice();
                    this.startTime = detail.startTime();
                    this.endTime = detail.endTime();
                    this.statusLabel.setText(detail.status().toString());
                    updateStatusStyle(detail.status().toString());
                    this.auctionTitleLabel.setText(detail.title());
                    this.auctionSubtitleLabel.setText(
                        "Lot #" + detail.auctionId() + " · " + detail.itemType() + " · " + detail.status()
                    );
                    this.highestBidderLabel.setText(
                        detail.highestBidderUsername() != null ? detail.highestBidderUsername() : "No bids"
                    );
                    this.reservePriceLabel.setText(
                        detail.reservePrice() != null ? formatMoney(detail.reservePrice()) : "None"
                    );
                    refreshCurrentPrice();
                    refreshAutoBidPanel();
                    autoBidMessageLabel.setText("Realtime updates active.");
                    autoLastActionLabel.setText("Watching: " + detail.title());
                    updateCountdown();

                    // Disable bidding UI when auction is not active
                    String statusStr = detail.status().toString();
                    if ("CANCELED".equals(statusStr) || "FINISHED".equals(statusStr)) {
                        bidAmountField.setDisable(true);
                        enableAutoBidButton.setDisable(true);
                        updateAutoBidButton.setDisable(true);
                        disableAutoBidButton.setDisable(true);
                        String reason = "CANCELED".equals(statusStr)
                            ? "This auction has been canceled."
                            : "This auction has ended.";
                        showManualMessage(reason, false);
                    }
                });
            }
        });
    }

    private void loadBidHistory() {
        if (auctionId == null) return;
        
        // Show loading state immediately to prevent layout shift and inform user
        Platform.runLater(() -> {
            bidHistoryContainer.getChildren().clear();
            Label loadingLabel = new Label("Loading history...");
            loadingLabel.getStyleClass().add("bid-history-subtitle");
            loadingLabel.setPadding(new Insets(20));
            bidHistoryContainer.setAlignment(Pos.CENTER);
            bidHistoryContainer.getChildren().add(loadingLabel);
        });

        auctionService.getBidHistory(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                List<PlaceBidResponse> history = response.getData();
                Platform.runLater(() -> {
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
                        java.util.List<PlaceBidResponse> chronological = new java.util.ArrayList<>(history);
                        java.util.Collections.reverse(chronological);

                        // Update chart using manager
                        chartManager.setData(chronological);

                        // Populate bid history cards (newest first — original order)
                        for (PlaceBidResponse bid : history) {
                            String time = bid.timestamp().format(TIME_FMT);
                            addBidHistoryCard(time, bid.highestBidderUsername(), bid.currentPrice());
                        }
                    }
                });
            }
        });
    }


    private void handleBidUpdate(Response<?> response) {
        BidUpdateEvent event = JsonMapper.getInstance().convertData(response.getData(), BidUpdateEvent.class);
        if (event.auctionId().equals(auctionId)) {
            Platform.runLater(() -> {
                this.currentPrice = event.amount();
                this.highestBidderLabel.setText(event.bidderUsername());
                this.endTime = event.newEndTime();

                String time = LocalDateTime.now().format(TIME_FMT);
                addBidHistoryCard(time, event.bidderUsername(), this.currentPrice);
                chartManager.addPricePoint(this.currentPrice);
                refreshCurrentPrice();
                triggerColorFlash();
                refreshAutoBidPanel();
                updateCountdown();

                if (autoBidEnabled && !event.bidderUsername().equals(SceneManager.getCurrentUsername())) {
                    triggerAutoBidIfPossible();
                }
            });
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (auctionId == null) { showManualMessage("No auction selected."); return; }
        BigDecimal manualBid = parsePositiveMoney(bidAmountField.getText());
        if (manualBid == null) { showManualMessage("Please enter a valid bid amount."); return; }
        BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);
        if (manualBid.compareTo(minimumNextBid) < 0) {
            showManualMessage("Minimum next bid is " + formatMoney(minimumNextBid) + ".");
            return;
        }
        auctionService.placeBid(auctionId, manualBid).thenAccept(response -> {
            Platform.runLater(() -> {
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

        auctionService.getDashboard().thenAccept(dashResponse -> {
            if (dashResponse.isSuccess()) {
                DashboardDto stats = dashResponse.getData();
                Platform.runLater(() -> SceneManager.setCurrentBalances(stats.balance(), stats.lockedBalance()));
            }
        });
    }

    @FXML
    private void handleEnableAutoBid() {
        if (auctionId == null) { showAutoMessage("No auction selected."); return; }
        BigDecimal parsedMaxBudget = parsePositiveMoney(autoMaxBudgetField.getText());
        BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());
        if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) { refreshAutoBidPanel(); return; }
        autoMaxBudget = parsedMaxBudget;
        autoStep = parsedStep;
        autoBidEnabled = true;
        showAutoMessage("Auto bidding is active.");
        autoLastActionLabel.setText("Watching this auction.");
        refreshAutoBidPanel();
        if (!SceneManager.getCurrentUsername().equals(highestBidderLabel.getText())) {
            triggerAutoBidIfPossible();
        }
    }

    @FXML
    private void handleUpdateAutoBid() {
        BigDecimal parsedMaxBudget = parsePositiveMoney(autoMaxBudgetField.getText());
        BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());
        if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) { refreshAutoBidPanel(); return; }
        autoMaxBudget = parsedMaxBudget;
        autoStep = parsedStep;
        autoBidEnabled = true;
        showAutoMessage("Auto bidding budget was updated.");
        autoLastActionLabel.setText("Max budget: " + formatMoney(autoMaxBudget) + ".");
        refreshAutoBidPanel();
    }

    @FXML
    private void handleDisableAutoBid() {
        autoBidEnabled = false;
        showAutoMessage("Auto bidding disabled. You can enable it again anytime.");
        autoLastActionLabel.setText("Auto bidding is off.");
        refreshAutoBidPanel();
    }

    @FXML
    private void handleSimulateOutbid() { /* Mock removed */ }

    private boolean validateAutoBidInput(BigDecimal parsedMaxBudget, BigDecimal parsedStep) {
        if (parsedMaxBudget == null) { showAutoMessage("Please enter a valid maximum budget."); return false; }
        if (parsedStep == null) { showAutoMessage("Please select a valid bid step."); return false; }
        BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);
        if (parsedMaxBudget.compareTo(minimumNextBid) < 0) {
            showAutoMessage("Your maximum budget must be at least " + formatMoney(minimumNextBid) + ".");
            return false;
        }
        if (parsedMaxBudget.compareTo(SceneManager.getCurrentBalance()) > 0) {
            showAutoMessage("Your maximum budget cannot exceed wallet balance " + formatMoney(SceneManager.getCurrentBalance()) + ".");
            return false;
        }
        if (parsedStep.compareTo(minimumIncrement) < 0) {
            showAutoMessage("Bid step must be at least " + formatMoney(minimumIncrement) + ".");
            return false;
        }
        return true;
    }

    private void triggerAutoBidIfPossible() {
        BigDecimal nextAutoBid = currentPrice.add(autoStep);
        if (nextAutoBid.compareTo(autoMaxBudget) > 0) {
            autoBidEnabled = false;
            autoLastActionLabel.setText("Maximum budget reached.");
            showAutoMessage("Auto bidding stopped because your maximum budget was reached.", false);
            refreshAutoBidPanel();
            return;
        }
        logger.info("Auto-bidding placing bid of {} for auction {}", nextAutoBid, auctionId);
        auctionService.placeBid(auctionId, nextAutoBid).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    autoLastActionLabel.setText("Auto bid placed: " + formatMoney(nextAutoBid) + ".");
                    showAutoMessage("Auto bidding responded successfully.", true);
                } else {
                    autoLastActionLabel.setText("Auto bid failed.");
                    autoBidEnabled = false;
                }
                refreshAutoBidPanel();
            });
        });
    }

    private void setupFlashTimeline() {
        flashTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(currentPriceLabel.textFillProperty(), Color.web("#059669"))),
            new KeyFrame(Duration.millis(500), new javafx.animation.KeyValue(currentPriceLabel.textFillProperty(), Color.web("#3525cd")))
        );
    }

    private void triggerColorFlash() {
        if (flashTimeline != null) {
            flashTimeline.playFromStart();
        }
    }

    // ── UI Helpers ───────────────────────────────────────

    private void updateStatusStyle(String status) {
        statusLabel.getStyleClass().removeAll(
            "status-badge", "status-running", "status-open", "status-finished", "status-cancelled"
        );
        statusLabel.getStyleClass().add("status-badge");
        switch (status) {
            case "RUNNING" -> statusLabel.getStyleClass().add("status-running");
            case "OPEN" -> statusLabel.getStyleClass().add("status-open");
            case "CANCELLED" -> statusLabel.getStyleClass().add("status-cancelled");
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
        autoUserPositionLabel.getStyleClass().removeAll("auto-position-leading", "auto-position-outbid");
        autoUserPositionLabel.getStyleClass().add(userIsLeading ? "auto-position-leading" : "auto-position-outbid");
        autoMaxBudgetLabel.setText(formatMoney(autoMaxBudget));

        if (autoBidEnabled) {
            setAutoStatus("ON", "auto-status-active");
            BigDecimal nextBid = currentPrice.add(autoStep);
            BigDecimal remaining = autoMaxBudget.subtract(currentPrice);
            autoNextBidLabel.setText(nextBid.compareTo(autoMaxBudget) <= 0 ? formatMoney(nextBid) : "Max reached");
            autoRemainingBudgetLabel.setText(remaining.compareTo(BigDecimal.ZERO) > 0 ? formatMoney(remaining) : "$0.00");
            enableAutoBidButton.setVisible(false); enableAutoBidButton.setManaged(false);
            updateAutoBidButton.setVisible(true); updateAutoBidButton.setManaged(true);
            disableAutoBidButton.setVisible(true); disableAutoBidButton.setManaged(true);
        } else {
            setAutoStatus("OFF", "auto-status-off");
            autoNextBidLabel.setText("-");
            autoRemainingBudgetLabel.setText("-");
            enableAutoBidButton.setVisible(true); enableAutoBidButton.setManaged(true);
            updateAutoBidButton.setVisible(false); updateAutoBidButton.setManaged(false);
            disableAutoBidButton.setVisible(false); disableAutoBidButton.setManaged(false);
        }
        autoMaxBudgetField.setDisable(false);
        autoStepComboBox.setDisable(false);
    }

    private void setAutoStatus(String text, String styleClass) {
        autoBidStatusLabel.setText(text);
        autoBidStatusLabel.setMinWidth(72); autoBidStatusLabel.setPrefWidth(72); autoBidStatusLabel.setMaxWidth(72);
        autoBidStatusLabel.getStyleClass().removeAll("auto-status-active", "auto-status-off", "auto-status-max");
        autoBidStatusLabel.getStyleClass().add(styleClass);
    }

    private BigDecimal parsePositiveMoney(String value) {
        if (value == null || value.trim().isBlank()) return null;
        try {
            BigDecimal parsed = new BigDecimal(value.trim().replace(",", ""));
            return parsed.compareTo(BigDecimal.ZERO) <= 0 ? null : parsed.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) { return null; }
    }

    private String formatMoney(BigDecimal value) { return USD_FORMAT.format(value); }

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

    private void showManualMessage(String message) { showManualMessage(message, false); }
    private void showAutoMessage(String message) { showAutoMessage(message, false); }

    private void handleAuctionClosed(Response<?> response) {
        com.auction.common.dto.auction.AuctionEventDto event = JsonMapper.getInstance().convertData(
            response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);
        if (event.auctionId().equals(auctionId)) {
            Platform.runLater(() -> {
                this.statusLabel.setText("FINISHED");
                updateStatusStyle("FINISHED");
                bidAmountField.setDisable(true);
                enableAutoBidButton.setDisable(true);
                updateAutoBidButton.setDisable(true);
                disableAutoBidButton.setDisable(true);
                showManualMessage("This auction has ended.", true);
                if (event.winnerUsername() != null) {
                    highestBidderLabel.setText(event.winnerUsername());
                    if (event.winnerUsername().equals(SceneManager.getCurrentUsername())) {
                        showManualMessage("Congratulations! You won this auction!", true);
                    }
                }
            });
        }
    }

    private void handleTimeExtended(Response<?> response) {
        com.auction.common.dto.auction.AuctionEventDto event = JsonMapper.getInstance().convertData(
            response.getData(), com.auction.common.dto.auction.AuctionEventDto.class);
        if (event.auctionId().equals(auctionId)) {
            Platform.runLater(() -> {
                this.endTime = event.newEndTime();
                showManualMessage("Time extended! More time to bid.", true);
                updateCountdown();
            });
        }
    }
}
