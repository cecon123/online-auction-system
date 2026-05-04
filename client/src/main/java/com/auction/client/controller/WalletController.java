package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import java.math.BigDecimal;
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

        balance = balance.add(amount);
        SceneManager.setCurrentBalance(balance);
        refreshBalance();
        messageLabel.setText(
            "Mock deposit successful. Reopen dashboard/topbar later to refresh header."
        );
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

        balance = balance.subtract(amount);
        SceneManager.setCurrentBalance(balance);
        refreshBalance();
        messageLabel.setText(
            "Mock withdraw successful. Reopen dashboard/topbar later to refresh header."
        );
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
