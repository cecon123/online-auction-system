package com.auction.client.controller;

import com.auction.client.service.WalletClientService;
import com.auction.client.util.JsonMapper;
import com.auction.client.util.SceneManager;
import java.math.BigDecimal;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class WalletController {

    @FXML
    private Label balanceLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Label messageLabel;

    private BigDecimal balance;
    private final WalletClientService walletService = new WalletClientService();
    private final JsonMapper jsonMapper = JsonMapper.getInstance();

    @FXML
    private void initialize() {
        balance = SceneManager.getCurrentBalance();
        refreshBalance();
        messageLabel.setText("");
    }

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
                            "Deposit successful! New balance: $" +
                                balance.toPlainString()
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

        if (amount.compareTo(balance) > 0) {
            messageLabel.setText("Cannot withdraw more than current balance.");
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
                            "Withdraw successful! New balance: $" +
                                balance.toPlainString()
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
        balanceLabel.setText("$" + balance.toPlainString());
    }
}
