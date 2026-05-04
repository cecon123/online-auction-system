package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.common.enums.Role;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class TopBarController {

    @FXML
    private Button placeBidButton;

    @FXML
    private Button walletButton;

    @FXML
    private Label avatarLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label balanceCaptionLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private void initialize() {
        Role role = SceneManager.getCurrentRole();
        String username = SceneManager.getCurrentUsername();

        avatarLabel.setText(createInitials(username));
        usernameLabel.setText(username);
        roleLabel.setText(role.name());
        balanceLabel.setText(SceneManager.getCurrentBalanceText());

        if (role == Role.BIDDER) {
            show(placeBidButton);
            show(walletButton);
            show(balanceCaptionLabel);
            show(balanceLabel);
        } else if (role == Role.SELLER) {
            hide(placeBidButton);
            show(walletButton);
            show(balanceCaptionLabel);
            show(balanceLabel);
        } else if (role == Role.ADMIN) {
            hide(placeBidButton);
            hide(walletButton);
            show(balanceCaptionLabel);
            show(balanceLabel);
        }
    }

    @FXML
    private void handlePlaceBidShortcut() {
        SceneManager.showAuctionList();
    }

    @FXML
    private void handleWalletShortcut() {
        SceneManager.showWallet();
    }

    private String createInitials(String username) {
        if (username == null || username.isBlank()) {
            return "U";
        }

        return username.trim().substring(0, 1).toUpperCase();
    }

    private void show(Button button) {
        button.setVisible(true);
        button.setManaged(true);
    }

    private void hide(Button button) {
        button.setVisible(false);
        button.setManaged(false);
    }

    private void show(Label label) {
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hide(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
