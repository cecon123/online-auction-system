package com.auction.client.util;

import com.auction.common.enums.Role;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class SceneManager {

  private static final String APP_CSS = "/css/app.css";

  private static final double AUTH_WIDTH = 820;
  private static final double AUTH_HEIGHT = 720;

  private static final double APP_WIDTH = 1280;
  private static final double APP_HEIGHT = 800;

  private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

  private static Stage primaryStage;
  private static BorderPane contentRoot;

  private static Role currentRole = Role.BIDDER;
  private static String currentUsername = "guest";
  private static long currentUserId = -1;
  private static BigDecimal currentBalance = new BigDecimal("45000");
  private static BigDecimal currentLockedBalance = BigDecimal.ZERO;

  private static Long lastSelectedAuctionId;
  private static final java.util.List<Runnable> balanceListeners = new java.util.ArrayList<>();

  private SceneManager() {}

  public static long getCurrentUserId() {
    return currentUserId;
  }

  public static void addBalanceListener(Runnable listener) {
    if (listener != null) {
      balanceListeners.add(listener);
    }
  }

  public static void initialize(Stage stage) {
    primaryStage = stage;
    primaryStage.setTitle("AuctionPro - Online Auction System");
  }

  public static Role getCurrentRole() {
    return currentRole;
  }

  public static String getCurrentUsername() {
    return currentUsername;
  }

  public static BigDecimal getCurrentBalance() {
    return currentBalance;
  }

  public static BigDecimal getCurrentLockedBalance() {
    return currentLockedBalance;
  }

  public static void setCurrentBalance(BigDecimal balance) {
    if (balance != null) {
      currentBalance = balance;
      notifyBalanceListeners();
    }
  }

  public static void setCurrentBalances(BigDecimal balance, BigDecimal lockedBalance) {
    if (balance != null) currentBalance = balance;
    if (lockedBalance != null) currentLockedBalance = lockedBalance;
    notifyBalanceListeners();
  }

  private static void notifyBalanceListeners() {
    for (Runnable listener : balanceListeners) {
      listener.run();
    }
  }

  public static String getCurrentBalanceText() {
    if (currentRole == Role.ADMIN) {
      return "System";
    }

    return CURRENCY_FORMAT.format(currentBalance);
  }

  // ── Auth Screens ──────────────────────────────────────

  public static void showLogin() {
    contentRoot = null;
    Parent root = loadFxml("/fxml/LoginView.fxml");
    setAuthScene(root);
  }

  public static void showRegister() {
    contentRoot = null;
    Parent root = loadFxml("/fxml/RegisterView.fxml");
    setAuthScene(root);
  }

  // ── App Shell ─────────────────────────────────────────

  public static void showAppShell(
      long userId, Role role, String username, BigDecimal balance, BigDecimal lockedBalance) {
    currentUserId = userId;
    currentRole = role == null ? Role.BIDDER : role;
    currentUsername = username == null || username.isBlank() ? "guest" : username.trim();
    currentBalance = balance == null ? BigDecimal.ZERO : balance;
    currentLockedBalance = lockedBalance == null ? BigDecimal.ZERO : lockedBalance;

    Parent root = loadFxml("/fxml/AppShell.fxml");
    BorderPane foundContentRoot = (BorderPane) root.lookup("#contentRoot");

    if (foundContentRoot == null) {
      throw new IllegalStateException(
          "AppShell.fxml must contain BorderPane with id='contentRoot'");
    }

    contentRoot = foundContentRoot;
    setAppScene(root);

    // Navigate directly to the role-specific main page
    showDefaultPage();
  }

  // ── Role-based Default Page ───────────────────────────

  /**
   * Navigate to the default page based on the current user role. Replaces the old showDashboard()
   * method.
   */
  private static void showDefaultPage() {
    switch (currentRole) {
      case BIDDER -> showAuctionList();
      case SELLER -> showSellerCenter();
      case ADMIN -> showAdminPanel();
    }
  }

  // ── Content Pages ─────────────────────────────────────

  public static void showAuctionList() {
    if (currentRole != Role.BIDDER) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/AuctionListView.fxml");
  }

  public static void showAuctionDetail() {
    showAuctionDetail(null);
  }

  public static void showAuctionDetail(Long auctionId) {
    if (currentRole != Role.BIDDER) {
      showDefaultPage();
      return;
    }

    try {
      URL resource = SceneManager.class.getResource("/fxml/AuctionDetailView.fxml");
      FXMLLoader loader = new FXMLLoader(resource);
      Parent content = loader.load();

      if (auctionId != null) {
        com.auction.client.controller.AuctionDetailController controller = loader.getController();
        controller.setAuctionId(auctionId);
      }

      contentRoot.setCenter(content);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load AuctionDetailView", e);
    }
  }

  public static void showLiveBidding() {
    showLiveBidding(lastSelectedAuctionId);
  }

  public static void showLiveBidding(Long auctionId) {
    if (currentRole != Role.BIDDER) {
      showDefaultPage();
      return;
    }

    if (auctionId != null) {
      lastSelectedAuctionId = auctionId;
    }

    try {
      URL resource = SceneManager.class.getResource("/fxml/LiveBiddingView.fxml");
      FXMLLoader loader = new FXMLLoader(resource);
      Parent content = loader.load();

      if (lastSelectedAuctionId != null) {
        com.auction.client.controller.LiveBiddingController controller = loader.getController();
        controller.setAuctionId(lastSelectedAuctionId);
      }

      contentRoot.setCenter(content);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load LiveBiddingView", e);
    }
  }

  public static void showMyBids() {
    if (currentRole != Role.BIDDER) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/MyBidsView.fxml");
  }

  public static void showSellerCenter() {
    if (currentRole != Role.SELLER) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/SellerCenterView.fxml");
  }

  public static void showCreateAuction() {
    if (currentRole != Role.SELLER) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/CreateAuctionView.fxml");
  }

  public static void showEditAuction(Long auctionId) {
    if (currentRole != Role.SELLER) {
      showDefaultPage();
      return;
    }

    try {
      URL resource = SceneManager.class.getResource("/fxml/EditAuctionView.fxml");
      FXMLLoader loader = new FXMLLoader(resource);
      Parent content = loader.load();

      com.auction.client.controller.EditAuctionController controller = loader.getController();
      controller.initData(auctionId);

      contentRoot.setCenter(content);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load EditAuctionView", e);
    }
  }

  public static void showWallet() {
    if (currentRole == Role.ADMIN) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/WalletView.fxml");
  }

  public static void showAdminPanel() {
    if (currentRole != Role.ADMIN) {
      showDefaultPage();
      return;
    }

    showCenter("/fxml/AdminPanelView.fxml");
  }

  // ── Internal Helpers ──────────────────────────────────

  private static void showCenter(String fxmlPath) {
    if (contentRoot == null) {
      throw new IllegalStateException("AppShell has not been loaded yet");
    }

    Parent content = loadFxml(fxmlPath);
    contentRoot.setCenter(content);
  }

  private static void setAuthScene(Parent root) {
    Scene scene = createScene(root, AUTH_WIDTH, AUTH_HEIGHT);

    primaryStage.hide();

    primaryStage.setResizable(false);
    primaryStage.setMinWidth(AUTH_WIDTH);
    primaryStage.setMinHeight(AUTH_HEIGHT);
    primaryStage.setMaxWidth(AUTH_WIDTH);
    primaryStage.setMaxHeight(AUTH_HEIGHT);

    primaryStage.setScene(scene);
    primaryStage.setWidth(AUTH_WIDTH);
    primaryStage.setHeight(AUTH_HEIGHT);

    centerStage(AUTH_WIDTH, AUTH_HEIGHT);
    primaryStage.show();
  }

  private static void setAppScene(Parent root) {
    Scene scene = createScene(root, APP_WIDTH, APP_HEIGHT);

    primaryStage.hide();

    primaryStage.setMaxWidth(Double.MAX_VALUE);
    primaryStage.setMaxHeight(Double.MAX_VALUE);

    primaryStage.setResizable(true);
    primaryStage.setMinWidth(1100);
    primaryStage.setMinHeight(720);

    primaryStage.setScene(scene);
    primaryStage.setWidth(APP_WIDTH);
    primaryStage.setHeight(APP_HEIGHT);

    centerStage(APP_WIDTH, APP_HEIGHT);
    primaryStage.show();
  }

  private static Scene createScene(Parent root, double width, double height) {
    Scene scene = new Scene(root, width, height);

    URL cssUrl = SceneManager.class.getResource(APP_CSS);
    if (cssUrl != null) {
      scene.getStylesheets().add(cssUrl.toExternalForm());
    }

    return scene;
  }

  private static void centerStage(double width, double height) {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

    primaryStage.setX(bounds.getMinX() + (bounds.getWidth() - width) / 2);
    primaryStage.setY(bounds.getMinY() + (bounds.getHeight() - height) / 2);
  }

  private static Parent loadFxml(String path) {
    try {
      URL resource = SceneManager.class.getResource(path);

      if (resource == null) {
        throw new IllegalStateException("FXML not found: " + path);
      }

      return FXMLLoader.load(resource);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load FXML: " + path, e);
    }
  }
}
