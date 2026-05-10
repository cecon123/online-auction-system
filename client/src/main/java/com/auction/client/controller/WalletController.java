package com.auction.client.controller;

import com.auction.client.service.WalletClientService;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.SceneManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class WalletController {

    @FXML
    private Label balanceLabel;

    @FXML
    private Label availableLabel;

    @FXML
    private Label lockedLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Label messageLabel;

    private BigDecimal balance;
    private BigDecimal lockedBalance;
    private final WalletClientService walletService = new WalletClientService();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private void initialize() {
        balance = SceneManager.getCurrentBalance();
        lockedBalance = SceneManager.getCurrentLockedBalance();
        refreshBalance();
        messageLabel.setText("");
    }

    @FXML
    private void handleQuickDeposit50() { amountField.setText("50"); handleDeposit(); }

    @FXML
    private void handleQuickDeposit100() { amountField.setText("100"); handleDeposit(); }

    @FXML
    private void handleQuickDeposit500() { amountField.setText("500"); handleDeposit(); }

    @FXML
    private void handleDeposit() {
        BigDecimal amount = parseAmount();

        if (amount == null) {
            return;
        }

        walletService
            .deposit(amount)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        BigDecimal newBalance = jsonMapper.convertData(
                            response.getData(),
                            BigDecimal.class
                        );
                        balance = newBalance;
                        SceneManager.setCurrentBalance(balance);
                        refreshBalance();
                        messageLabel.setText(
                            "Deposit successful! New balance: " +
                                CURRENCY.format(balance)
                        );
                        amountField.clear();
                    } else {
                        messageLabel.setText(
                            "Deposit failed: " + response.getMessage()
                        );
                    }
                });
            });
    }

    @FXML
    private void handleWithdraw() {
        BigDecimal amount = parseAmount();

        if (amount == null) {
            return;
        }

        BigDecimal available = balance.subtract(lockedBalance);
        if (amount.compareTo(available) > 0) {
            messageLabel.setText("Cannot withdraw more than available balance.");
            return;
        }

        walletService
            .withdraw(amount)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        BigDecimal newBalance = jsonMapper.convertData(
                            response.getData(),
                            BigDecimal.class
                        );
                        balance = newBalance;
                        SceneManager.setCurrentBalance(balance);
                        refreshBalance();
                        messageLabel.setText(
                            "Withdraw successful! New balance: " +
                                CURRENCY.format(balance)
                        );
                        amountField.clear();
                    } else {
                        messageLabel.setText(
                            "Withdraw failed: " + response.getMessage()
                        );
                    }
                });
            });
    }

    private BigDecimal parseAmount() {
        String text =
            amountField.getText() == null ? "" : amountField.getText().trim();

        if (text.isBlank()) {
            messageLabel.setText("Please enter amount.");
            return null;
        }

        try {
            BigDecimal amount = new BigDecimal(text.replace(",", ""));

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                messageLabel.setText("Amount must be positive.");
                return null;
            }

            return amount;
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid amount.");
            return null;
        }
    }

    private void refreshBalance() {
        balanceLabel.setText(CURRENCY.format(balance));
        availableLabel.setText(CURRENCY.format(balance.subtract(lockedBalance)));
        lockedLabel.setText(CURRENCY.format(lockedBalance));
    }
}
