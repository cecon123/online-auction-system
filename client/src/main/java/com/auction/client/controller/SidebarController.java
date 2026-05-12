package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.common.enums.Role;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {

  @FXML private Button auctionsButton;

  @FXML private Button liveBiddingButton;

  @FXML private Button myBidsButton;

  @FXML private Button sellerCenterButton;

  @FXML private Button walletButton;

  @FXML private Button adminPanelButton;

  @FXML
  private void initialize() {
    Role role = SceneManager.getCurrentRole();

    if (role == Role.BIDDER) {
      show(auctionsButton);
      show(liveBiddingButton);
      show(myBidsButton);
      hide(sellerCenterButton);
      show(walletButton);
      hide(adminPanelButton);
    } else if (role == Role.SELLER) {
      hide(auctionsButton);
      hide(liveBiddingButton);
      hide(myBidsButton);
      show(sellerCenterButton);
      show(walletButton);
      hide(adminPanelButton);
    } else if (role == Role.ADMIN) {
      hide(auctionsButton);
      hide(liveBiddingButton);
      hide(myBidsButton);
      hide(sellerCenterButton);
      hide(walletButton);
      show(adminPanelButton);
    }
  }

  @FXML
  private void showAuctions() {
    SceneManager.showAuctionList();
  }

  @FXML
  private void showLiveBidding() {
    SceneManager.showLiveBidding();
  }

  @FXML
  private void showMyBids() {
    SceneManager.showMyBids();
  }

  @FXML
  private void showSellerCenter() {
    SceneManager.showSellerCenter();
  }

  @FXML
  private void showWallet() {
    SceneManager.showWallet();
  }

  @FXML
  private void showAdminPanel() {
    SceneManager.showAdminPanel();
  }

  @FXML
  private void logout() {
    com.auction.client.socket.SocketClient.getInstance().setCredentials(null, null);
    com.auction.client.socket.SocketClient.getInstance().setToken(null);
    javafx.application.Platform.runLater(SceneManager::showLogin);
  }

  private void show(Button button) {
    button.setVisible(true);
    button.setManaged(true);
  }

  private void hide(Button button) {
    button.setVisible(false);
    button.setManaged(false);
  }
}
