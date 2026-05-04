package com.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class LiveBiddingController {
    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label highestBidderLabel;

    @FXML
    private Label countdownLabel;

    @FXML
    private TextField bidAmountField;

    @FXML
    private ListView<String> bidHistoryList;

    @FXML
    private LineChart<String, Number> priceChart;

    private final ObservableList<String> bidHistory = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        currentPriceLabel.setText("$14,500.00");
        highestBidderLabel.setText("USR-982");
        countdownLabel.setText("00:04:12");

        bidHistory.add("10:24:12 | USR-982 | $14,500");
        bidHistory.add("10:23:45 | USR-104 | $14,000");
        bidHistory.add("10:21:10 | USR-552 | $13,500");
        bidHistoryList.setItems(bidHistory);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Current Price");
        series.getData().add(new XYChart.Data<>("10:00", 12000));
        series.getData().add(new XYChart.Data<>("10:10", 12500));
        series.getData().add(new XYChart.Data<>("10:20", 13500));
        series.getData().add(new XYChart.Data<>("10:24", 14500));
        priceChart.getData().add(series);
    }

    @FXML
    private void handlePlaceBid() {
        String amount = bidAmountField.getText();

        if (amount == null || amount.isBlank()) {
            return;
        }

        String normalizedAmount = amount.trim();

        currentPriceLabel.setText("$" + normalizedAmount);
        highestBidderLabel.setText("YOU");
        bidHistory.add(0, "Now | YOU | $" + normalizedAmount);

        if (!priceChart.getData().isEmpty()) {
            Number parsedAmount = parseAmount(normalizedAmount);
            priceChart.getData().get(0).getData().add(new XYChart.Data<>("Now", parsedAmount));
        }

        bidAmountField.clear();
    }

    private Number parseAmount(String amount) {
        try {
            return Double.parseDouble(amount.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
