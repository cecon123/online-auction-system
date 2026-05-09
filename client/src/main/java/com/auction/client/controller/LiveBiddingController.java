package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.dto.bid.PlaceBidResponse;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveBiddingController {

    private static final Logger logger = LoggerFactory.getLogger(
        LiveBiddingController.class
    );

    private static final NumberFormat USD_FORMAT =
        NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private Label countdownHeaderLabel;

    @FXML
    private Label countdownLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label auctionTitleLabel;

    @FXML
    private Label auctionSubtitleLabel;

    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label highestBidderLabel;

    @FXML
    private TextField bidAmountField;

    @FXML
    private Label manualBidMessageLabel;

    @FXML
    private ListView<String> bidHistoryList;

    @FXML
    private LineChart<String, Number> priceChart;

    @FXML
    private CategoryAxis timeAxis;

    @FXML
    private NumberAxis priceAxis;

    @FXML
    private Label autoBidStatusLabel;

    @FXML
    private Label autoBidMessageLabel;

    @FXML
    private Label autoCurrentPriceLabel;

    @FXML
    private Label autoUserPositionLabel;

    @FXML
    private TextField autoMaxBudgetField;

    @FXML
    private ComboBox<String> autoStepComboBox;

    @FXML
    private Label autoMaxBudgetLabel;

    @FXML
    private Label autoNextBidLabel;

    @FXML
    private Label autoRemainingBudgetLabel;

    @FXML
    private Label autoLastActionLabel;

    @FXML
    private Button enableAutoBidButton;

    @FXML
    private Button updateAutoBidButton;

    @FXML
    private Button disableAutoBidButton;

    @FXML
    private Button simulateOutbidButton;

    private final AuctionClientService auctionService =
        new AuctionClientService();
    private final Consumer<Response<?>> bidUpdateListener =
        this::handleBidUpdate;
    private final Consumer<Response<?>> auctionClosedListener =
        this::handleAuctionClosed;
    private final Consumer<Response<?>> timeExtendedListener =
        this::handleTimeExtended;

    private Long auctionId;
    private BigDecimal minimumIncrement = new BigDecimal("1.00");

    private BigDecimal currentPrice = new BigDecimal("0");
    private BigDecimal autoMaxBudget = new BigDecimal("20000");
    private BigDecimal autoStep = new BigDecimal("500");

    private boolean autoBidEnabled = false;
    private XYChart.Series<String, Number> priceSeries;
    
    private LocalDateTime endTime;
    private Timeline countdownTimeline;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        loadAuctionData();
        loadBidHistory();
        subscribeToUpdates();
    }

    private void subscribeToUpdates() {
        if (auctionId == null) return;
        auctionService.subscribeAuction(auctionId);
        
        SocketClient socket = SocketClient.getInstance();
        socket.addEventListener(MessageType.BID_UPDATE, bidUpdateListener);
        socket.addEventListener(MessageType.AUCTION_CLOSED, auctionClosedListener);
        socket.addEventListener(MessageType.TIME_EXTENDED, timeExtendedListener);
    }

    @FXML
    private void initialize() {
        autoMaxBudgetField.setText(autoMaxBudget.toPlainString());

        autoStepComboBox.setItems(
            FXCollections.observableArrayList("500", "1000", "2500", "5000")
        );
        autoStepComboBox.setValue("500");

        setupChart();
        refreshAutoBidPanel();

        manualBidMessageLabel.setText("");
        autoBidMessageLabel.setText("Connect to server...");
        autoLastActionLabel.setText("Waiting for auction data.");

        simulateOutbidButton.setVisible(false);
        simulateOutbidButton.setManaged(false);
        
        setupCountdownTimeline();
    }

    private void setupCountdownTimeline() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdown() {
        if (endTime == null) {
            countdownLabel.setText("--:--:--");
            return;
        }

        String status = statusLabel.getText();
        if ("FINISHED".equals(status)) {
            countdownHeaderLabel.setText("TIME REMAINING");
            countdownLabel.setText("00:00:00");
            return;
        }

        LocalDateTime targetTime = endTime;

        if ("OPEN".equals(status)) {
            countdownHeaderLabel.setText("START IN");
            targetTime = getStartTimeFromDetail(); 
            if (targetTime == null) {
                countdownLabel.setText("Opening...");
                return;
            }
        } else {
            countdownHeaderLabel.setText("TIME REMAINING");
        }

        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), targetTime);
        if (duration.isNegative() || duration.isZero()) {
            if ("OPEN".equals(status)) {
                countdownLabel.setText("Starting...");
            } else {
                countdownLabel.setText("00:00:00");
            }
            return;
        }

        long totalSeconds = duration.toSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        countdownLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private LocalDateTime getStartTimeFromDetail() {
        // We need to store startTime in the controller if we want to use it here
        return this.startTime;
    }

    private LocalDateTime startTime; // Add this field to store start time

    private void loadAuctionData() {
        if (auctionId == null) return;

        auctionService
            .getAuctionDetail(auctionId)
            .thenAccept(response -> {
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
                            "Lot #" +
                                detail.auctionId() +
                                " · " +
                                detail.itemType() +
                                " · " +
                                detail.status()
                        );
                        this.highestBidderLabel.setText(
                            detail.highestBidderUsername() != null
                                ? detail.highestBidderUsername()
                                : "No bids"
                        );
                        refreshCurrentPrice();
                        refreshAutoBidPanel();
                        autoBidMessageLabel.setText("Realtime updates active.");
                        autoLastActionLabel.setText(
                            "Watching: " + detail.title()
                        );
                        addPricePoint("Start", this.currentPrice);
                        updateCountdown();
                    });
                }
            });
    }

    private void loadBidHistory() {
        if (auctionId == null) return;
        
        auctionService.getBidHistory(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                List<PlaceBidResponse> history = response.getData();
                Platform.runLater(() -> {
                    bidHistoryList.getItems().clear();
                    for (PlaceBidResponse bid : history) {
                        bidHistoryList.getItems().add(0, formatHistoryRow("Past", bid.highestBidderUsername(), bid.currentPrice()));
                    }
                });
            }
        });
    }

    private void handleBidUpdate(Response<?> response) {
        BidUpdateEvent event = JsonMapper.getInstance().convertData(
            response.getData(),
            BidUpdateEvent.class
        );
        if (event.auctionId().equals(auctionId)) {
            Platform.runLater(() -> {
                this.currentPrice = event.amount();
                this.highestBidderLabel.setText(event.bidderUsername());
                this.endTime = event.newEndTime();

                bidHistoryList
                    .getItems()
                    .add(
                        0,
                        formatHistoryRow(
                            "Now",
                            event.bidderUsername(),
                            this.currentPrice
                        )
                    );
                addPricePoint("Update", this.currentPrice);

                refreshCurrentPrice();
                refreshAutoBidPanel();
                updateCountdown();

                if (
                    autoBidEnabled &&
                    !event
                        .bidderUsername()
                        .equals(SceneManager.getCurrentUsername())
                ) {
                    triggerAutoBidIfPossible();
                }
            });
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (auctionId == null) {
            showManualMessage(
                "No auction selected. Please go to Auctions list and select one."
            );
            return;
        }

        BigDecimal manualBid = parsePositiveMoney(bidAmountField.getText());

        if (manualBid == null) {
            showManualMessage("Please enter a valid bid amount.");
            return;
        }

        BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);

        if (manualBid.compareTo(minimumNextBid) < 0) {
            showManualMessage(
                "Minimum next bid is " + formatMoney(minimumNextBid) + "."
            );
            return;
        }

        auctionService
            .placeBid(auctionId, manualBid)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showManualMessage("Bid placed successfully!", true);
                        bidAmountField.clear();
                        
                        // Deduct from local balance for immediate feedback 
                        // (Server should eventually send an official balance update)
                        BigDecimal currentBalance = SceneManager.getCurrentBalance();
                        BigDecimal newBalance = currentBalance.subtract(manualBid);
                        SceneManager.setCurrentBalance(newBalance);
                    } else {
                        showManualMessage("Error: " + response.getMessage(), false);
                    }
                });
            });
    }

    @FXML
    private void handleEnableAutoBid() {
        if (auctionId == null) {
            showAutoMessage("No auction selected.");
            return;
        }

        BigDecimal parsedMaxBudget = parsePositiveMoney(
            autoMaxBudgetField.getText()
        );
        BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());

        if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) {
            refreshAutoBidPanel();
            return;
        }

        autoMaxBudget = parsedMaxBudget;
        autoStep = parsedStep;
        autoBidEnabled = true;

        showAutoMessage("Auto bidding is active.");
        autoLastActionLabel.setText("Watching this auction.");
        refreshAutoBidPanel();
        
        // Try to bid immediately if not leading
        if (!SceneManager.getCurrentUsername().equals(highestBidderLabel.getText())) {
            triggerAutoBidIfPossible();
        }
    }

    @FXML
    private void handleUpdateAutoBid() {
        BigDecimal parsedMaxBudget = parsePositiveMoney(
            autoMaxBudgetField.getText()
        );
        BigDecimal parsedStep = parsePositiveMoney(autoStepComboBox.getValue());

        if (!validateAutoBidInput(parsedMaxBudget, parsedStep)) {
            refreshAutoBidPanel();
            return;
        }

        autoMaxBudget = parsedMaxBudget;
        autoStep = parsedStep;
        autoBidEnabled = true;

        showAutoMessage("Auto bidding budget was updated.");
        autoLastActionLabel.setText(
            "Max budget: " + formatMoney(autoMaxBudget) + "."
        );
        refreshAutoBidPanel();
    }

    @FXML
    private void handleDisableAutoBid() {
        autoBidEnabled = false;

        showAutoMessage(
            "Auto bidding disabled. You can enable it again anytime."
        );
        autoLastActionLabel.setText("Auto bidding is off.");
        refreshAutoBidPanel();
    }

    @FXML
    private void handleSimulateOutbid() {
        // Mock removed
    }

    private boolean validateAutoBidInput(
        BigDecimal parsedMaxBudget,
        BigDecimal parsedStep
    ) {
        if (parsedMaxBudget == null) {
            showAutoMessage("Please enter a valid maximum budget.");
            return false;
        }

        if (parsedStep == null) {
            showAutoMessage("Please select a valid bid step.");
            return false;
        }

        BigDecimal minimumNextBid = currentPrice.add(minimumIncrement);

        if (parsedMaxBudget.compareTo(minimumNextBid) < 0) {
            showAutoMessage(
                "Your maximum budget must be at least " +
                    formatMoney(minimumNextBid) +
                    "."
            );
            return false;
        }

        if (parsedMaxBudget.compareTo(SceneManager.getCurrentBalance()) > 0) {
            showAutoMessage(
                "Your maximum budget cannot exceed wallet balance " +
                    formatMoney(SceneManager.getCurrentBalance()) +
                    "."
            );
            return false;
        }

        if (parsedStep.compareTo(minimumIncrement) < 0) {
            showAutoMessage(
                "Bid step must be at least " +
                    formatMoney(minimumIncrement) +
                    "."
            );
            return false;
        }

        return true;
    }

    private void triggerAutoBidIfPossible() {
        BigDecimal nextAutoBid = currentPrice.add(autoStep);

        if (nextAutoBid.compareTo(autoMaxBudget) > 0) {
            autoBidEnabled = false;
            autoLastActionLabel.setText("Maximum budget reached.");
            showAutoMessage(
                "Auto bidding stopped because your maximum budget was reached.",
                false
            );
            refreshAutoBidPanel();
            return;
        }

        logger.info("Auto-bidding placing bid of {} for auction {}", nextAutoBid, auctionId);
        auctionService.placeBid(auctionId, nextAutoBid).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    autoLastActionLabel.setText(
                        "Auto bid placed: " + formatMoney(nextAutoBid) + "."
                    );
                    showAutoMessage("Auto bidding responded successfully.", true);
                } else {
                    showAutoMessage("Auto bid failed: " + response.getMessage(), false);
                    autoLastActionLabel.setText("Auto bid failed.");
                    autoBidEnabled = false;
                }
                refreshAutoBidPanel();
            });
        });
    }

    private void setupChart() {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Current Price");

        timeAxis.setLabel("Time");
        timeAxis.setAnimated(false);

        priceAxis.setLabel("Price");
        priceAxis.setAutoRanging(true);
        priceAxis.setAnimated(false);

        priceChart.getData().setAll(priceSeries);
        priceChart.setLegendVisible(false);
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(true);
    }

    private void addPricePoint(String label, BigDecimal price) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String safeLabel = time + " (" + label + ")";

        priceSeries
            .getData()
            .add(new XYChart.Data<>(safeLabel, price.doubleValue()));

        if (priceSeries.getData().size() > 10) {
            priceSeries.getData().remove(0);
        }
    }

    private void updateStatusStyle(String status) {
        statusLabel
            .getStyleClass()
            .removeAll(
                "live-status-running",
                "live-status-open",
                "live-status-finished"
            );

        switch (status) {
            case "RUNNING" -> statusLabel
                .getStyleClass()
                .add("live-status-running");
            case "OPEN" -> statusLabel.getStyleClass().add("live-status-open");
            default -> statusLabel.getStyleClass().add("live-status-finished");
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
            .add(
                userIsLeading ? "auto-position-leading" : "auto-position-outbid"
            );

        autoMaxBudgetLabel.setText(formatMoney(autoMaxBudget));

        if (autoBidEnabled) {
            setAutoStatus("ON", "auto-status-active");

            BigDecimal nextBid = currentPrice.add(autoStep);
            BigDecimal remaining = autoMaxBudget.subtract(currentPrice);

            autoNextBidLabel.setText(
                nextBid.compareTo(autoMaxBudget) <= 0
                    ? formatMoney(nextBid)
                    : "Max reached"
            );
            autoRemainingBudgetLabel.setText(
                remaining.compareTo(BigDecimal.ZERO) > 0
                    ? formatMoney(remaining)
                    : "$0.00"
            );

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

        autoMaxBudgetField.setDisable(false);
        autoStepComboBox.setDisable(false);
    }

    private void setAutoStatus(String text, String styleClass) {
        autoBidStatusLabel.setText(text);
        autoBidStatusLabel.setMinWidth(72);
        autoBidStatusLabel.setPrefWidth(72);
        autoBidStatusLabel.setMaxWidth(72);

        autoBidStatusLabel
            .getStyleClass()
            .removeAll(
                "auto-status-active",
                "auto-status-off",
                "auto-status-max"
            );
        autoBidStatusLabel.getStyleClass().add(styleClass);
    }

    private BigDecimal parsePositiveMoney(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        try {
            BigDecimal parsed = new BigDecimal(value.trim().replace(",", ""));

            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            return parsed.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatMoney(BigDecimal value) {
        return USD_FORMAT.format(value);
    }

    private String formatMoneyCompact(BigDecimal value) {
        BigDecimal thousand = new BigDecimal("1000");

        if (value.compareTo(thousand) >= 0) {
            BigDecimal compact = value.divide(
                thousand,
                1,
                RoundingMode.HALF_UP
            );
            return "$" + compact.stripTrailingZeros().toPlainString() + "k";
        }

        return formatMoney(value);
    }

    private String formatHistoryRow(
        String time,
        String bidder,
        BigDecimal amount
    ) {
        return String.format(
            "%-5s  %-10s  %7s",
            time,
            bidder,
            formatMoneyCompact(amount)
        );
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
        com.auction.common.dto.auction.AuctionEventDto event = JsonMapper.getInstance().convertData(
            response.getData(),
            com.auction.common.dto.auction.AuctionEventDto.class
        );
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
            response.getData(),
            com.auction.common.dto.auction.AuctionEventDto.class
        );
        if (event.auctionId().equals(auctionId)) {
            Platform.runLater(() -> {
                this.endTime = event.newEndTime();
                showManualMessage("Time extended! More time to bid.", true);
                updateCountdown();
            });
        }
    }
}
