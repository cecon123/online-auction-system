package com.auction.client.util;

import com.auction.common.enums.Role;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
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

    private static Stage primaryStage;
    private static BorderPane contentRoot;

    private static Role currentRole = Role.BIDDER;
    private static String currentUsername = "guest";
    private static BigDecimal currentBalance = new BigDecimal("45000");

    private SceneManager() {}

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

    public static void setCurrentBalance(BigDecimal balance) {
        if (balance != null) {
            currentBalance = balance;
        }
    }

    public static String getCurrentBalanceText() {
        if (currentRole == Role.ADMIN) {
            return "System";
        }

        return "$" + currentBalance.toPlainString();
    }

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

    public static void showAppShell(Role role, String username) {
        currentRole = role == null ? Role.BIDDER : role;
        currentUsername =
            username == null || username.isBlank() ? "guest" : username.trim();
        currentBalance = createMockBalanceForRole(currentRole);

        Parent root = loadFxml("/fxml/AppShell.fxml");
        BorderPane foundContentRoot = (BorderPane) root.lookup("#contentRoot");

        if (foundContentRoot == null) {
            throw new IllegalStateException(
                "AppShell.fxml must contain BorderPane with id='contentRoot'"
            );
        }

        contentRoot = foundContentRoot;
        setAppScene(root);
        showDashboard();
    }

    public static void showDashboard() {
        if (currentRole == Role.ADMIN) {
            showCenter("/fxml/AdminDashboardView.fxml");
        } else if (currentRole == Role.SELLER) {
            showCenter("/fxml/SellerDashboardView.fxml");
        } else {
            showCenter("/fxml/BidderDashboardView.fxml");
        }
    }

    public static void showAuctionList() {
        if (currentRole != Role.BIDDER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/AuctionListView.fxml");
    }

    public static void showAuctionDetail() {
        if (currentRole != Role.BIDDER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/AuctionDetailView.fxml");
    }

    public static void showLiveBidding() {
        if (currentRole != Role.BIDDER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/LiveBiddingView.fxml");
    }

    public static void showMyBids() {
        if (currentRole != Role.BIDDER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/MyBidsView.fxml");
    }

    public static void showSellerCenter() {
        if (currentRole != Role.SELLER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/SellerCenterView.fxml");
    }

    public static void showCreateAuction() {
        if (currentRole != Role.SELLER) {
            showDashboard();
            return;
        }

        showCenter("/fxml/CreateAuctionView.fxml");
    }

    public static void showWallet() {
        if (currentRole == Role.ADMIN) {
            showDashboard();
            return;
        }

        showCenter("/fxml/WalletView.fxml");
    }

    public static void showAdminPanel() {
        if (currentRole != Role.ADMIN) {
            showDashboard();
            return;
        }

        showCenter("/fxml/AdminPanelView.fxml");
    }

    private static BigDecimal createMockBalanceForRole(Role role) {
        if (role == Role.SELLER) {
            return new BigDecimal("12800");
        }

        if (role == Role.ADMIN) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal("45000");
    }

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
