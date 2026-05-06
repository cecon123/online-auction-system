package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveBiddingController {
    private static final Logger logger = LoggerFactory.getLogger(LiveBiddingController.class);

    private static final NumberFormat USD_FORMAT =
        NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private Label countdownLabel;

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

    private final AuctionClientService auctionService = new AuctionClientService();
    private final Consumer<Response<?>> bidUpdateListener = this::handleBidUpdate;
    
    private Long auctionId;
    private final BigDecimal minimumIncrement = new BigDecimal("500");
    private final BigDecimal walletBalance = SceneManager.getCurrentBalance();

    private BigDecimal currentPrice = new BigDecimal("0");
    private BigDecimal autoMaxBudget = new BigDecimal("20000");
    private BigDecimal autoStep = new BigDecimal("500");

    private boolean autoBidEnabled = false;
    private XYChart.Series<String, Number> priceSeries;

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
        loadAuctionData();
        subscribeToUpdates();
    }

    private void subscribeToUpdates() {
        if (auctionId == null) return;
        auctionService.subscribeAuction(auctionId);
        SocketClient.getInstance().addEventListener(MessageType.BID_UPDATE, bidUpdateListener);
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
    }

    private void loadAuctionData() {
        if (auctionId == null) return;

        auctionService.getAuctionDetail(auctionId).thenAccept(response -> {
            if (response.isSuccess()) {
                AuctionDetailDto detail = response.getData();
                Platform.runLater(() -> {
                    this.currentPrice = detail.currentPrice();
                    this.highestBidderLabel.setText(detail.highestBidderUsername() != null ? detail.highestBidderUsername() : "No bids");
                    refreshCurrentPrice();
                    refreshAutoBidPanel();
                    autoBidMessageLabel.setText("Realtime updates active.");
                    autoLastActionLabel.setText("Watching: " + detail.title());
                });
            }
        });
    }

    private void handleBidUpdate(Response<?> response) {
        BidUpdateEvent event = JsonMapper.getInstance().convertData(response.getData(), BidUpdateEvent.class);
        if (event.auctionId() == auctionId) {
            Platform.runLater(() -> {
                this.currentPrice = event.amount();
                this.highestBidderLabel.setText(event.bidderUsername());
                
                bidHistoryList.getItems().add(0, formatHistoryRow("Now", event.bidderUsername(), this.currentPrice));
                addPricePoint("Now", this.currentPrice);
                
                refreshCurrentPrice();
                refreshAutoBidPanel();
                
                if (autoBidEnabled && !event.bidderUsername().equals(SceneManager.getCurrentUsername())) {
                    triggerAutoBidIfPossible();
                }
            });
        }
    }

    @FXML
    private void handlePlaceBid() {
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

        auctionService.placeBid(auctionId, manualBid).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    showManualMessage("Bid placed successfully!");
                    bidAmountField.clear();
                } else {
                    showManualMessage("Error: " + response.getMessage());
                }
            });
        });
    }

    @FXML
    private void handleEnableAutoBid() {
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
        BigDecimal competitorBid = currentPrice.add(minimumIncrement);

        currentPrice = competitorBid;
        highestBidderLabel.setText("Bidder_5555");

        bidHistoryList
            .getItems()
            .add(0, formatHistoryRow("Now", "Bidder_5555", competitorBid));
        addPricePoint("Outbid", currentPrice);
        refreshCurrentPrice();

        if (autoBidEnabled) {
            triggerAutoBidIfPossible();
        } else {
            showAutoMessage("You were outbid. Auto bidding is off.");
            autoLastActionLabel.setText(
                "Competitor placed " + formatMoney(competitorBid) + "."
            );
        }

        refreshAutoBidPanel();
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

        if (parsedMaxBudget.compareTo(walletBalance) > 0) {
            showAutoMessage(
                "Your maximum budget cannot exceed wallet balance " +
                    formatMoney(walletBalance) +
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
            highestBidderLabel.setText("Bidder_5555");

            autoLastActionLabel.setText("Maximum budget reached.");
            showAutoMessage(
                "Auto bidding stopped because your maximum budget was reached."
            );

            refreshAutoBidPanel();
            return;
        }

        currentPrice = nextAutoBid;
        highestBidderLabel.setText("You");

        bidHistoryList
            .getItems()
            .add(0, formatHistoryRow("Now", "Auto Bid", nextAutoBid));
        addPricePoint("Auto", currentPrice);
        refreshCurrentPrice();

        autoLastActionLabel.setText(
            "Auto bid placed: " + formatMoney(nextAutoBid) + "."
        );
        showAutoMessage("Auto bidding responded successfully.");
        refreshAutoBidPanel();
    }

    private void setupChart() {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Current Price");

        List<XYChart.Data<String, Number>> initialPoints = List.of(
            new XYChart.Data<>("10:00", 12000),
            new XYChart.Data<>("10:10", 12500),
            new XYChart.Data<>("10:20", 13750),
            new XYChart.Data<>("10:24", currentPrice)
        );

        priceSeries.getData().setAll(initialPoints);

        timeAxis.setLabel("Time");
        timeAxis.setCategories(
            FXCollections.observableArrayList(
                "10:00",
                "10:10",
                "10:20",
                "10:24"
            )
        );
        timeAxis.setTickLabelRotation(0);
        timeAxis.setAnimated(false);

        priceAxis.setLabel("Price");
        priceAxis.setAutoRanging(false);
        priceAxis.setLowerBound(0);
        priceAxis.setUpperBound(15000);
        priceAxis.setTickUnit(5000);
        priceAxis.setMinorTickVisible(false);
        priceAxis.setAnimated(false);

        priceChart.getData().setAll(priceSeries);
        priceChart.setLegendVisible(false);
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(true);
        priceChart.setHorizontalGridLinesVisible(true);
        priceChart.setVerticalGridLinesVisible(true);
    }

    private void addPricePoint(String label, BigDecimal price) {
        String safeLabel = createUniqueChartLabel(label);

        if (!timeAxis.getCategories().contains(safeLabel)) {
            timeAxis.getCategories().add(safeLabel);
        }

        priceSeries
            .getData()
            .add(new XYChart.Data<>(safeLabel, price.doubleValue()));

        if (priceSeries.getData().size() > 8) {
            String removedCategory = priceSeries.getData().get(0).getXValue();
            priceSeries.getData().remove(0);
            timeAxis.getCategories().remove(removedCategory);
        }

        updatePriceAxisRange(price);
    }

    private String createUniqueChartLabel(String label) {
        int duplicateCount = 0;
        String candidate = label;

        while (timeAxis.getCategories().contains(candidate)) {
            duplicateCount++;
            candidate = label + " " + duplicateCount;
        }

        return candidate;
    }

    private void updatePriceAxisRange(BigDecimal latestPrice) {
        double latest = latestPrice.doubleValue();
        double upper = priceAxis.getUpperBound();

        if (latest >= upper * 0.9) {
            double newUpper = Math.ceil((latest + 3000) / 5000.0) * 5000.0;
            priceAxis.setUpperBound(newUpper);
            priceAxis.setTickUnit(newUpper / 3);
        }
    }

    private void refreshCurrentPrice() {
        currentPriceLabel.setText(formatMoney(currentPrice));
        autoCurrentPriceLabel.setText(formatMoney(currentPrice));
    }

    private void refreshAutoBidPanel() {
        refreshCurrentPrice();

        boolean userIsLeading = "You".equalsIgnoreCase(
            highestBidderLabel.getText()
        );

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

    private void showManualMessage(String message) {
        manualBidMessageLabel.setText(message);
    }

    private void showAutoMessage(String message) {
        autoBidMessageLabel.setText(message);
    }
}
