package com.auction.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Central navigation helper for JavaFX screens.
 *
 * W6 goal:
 * - Load Login/Register screens.
 * - Load AppShell after mock login.
 * - Replace AppShell center content with Dashboard/AuctionList/etc.
 */
public final class SceneManager {
    private static final String APP_CSS = "/css/app.css";

    private static Stage primaryStage;
    private static BorderPane appShellRoot;

    private SceneManager() {
    }

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("AuctionPro - Online Auction System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
    }

    public static void showLogin() {
        Parent root = loadFxml("/fxml/LoginView.fxml");
        setScene(root);
    }

    public static void showRegister() {
        Parent root = loadFxml("/fxml/RegisterView.fxml");
        setScene(root);
    }

    public static void showAppShell() {
        Parent root = loadFxml("/fxml/AppShell.fxml");

        if (!(root instanceof BorderPane borderPane)) {
            throw new IllegalStateException("AppShell.fxml root must be BorderPane");
        }

        appShellRoot = borderPane;
        setScene(root);
        showDashboard();
    }

    public static void showDashboard() {
        showCenter("/fxml/DashboardView.fxml");
    }

    public static void showAuctionList() {
        showCenter("/fxml/AuctionListView.fxml");
    }

    public static void showAuctionDetail() {
        showCenter("/fxml/AuctionDetailView.fxml");
    }

    public static void showLiveBidding() {
        showCenter("/fxml/LiveBiddingView.fxml");
    }

    public static void showSellerCenter() {
        showCenter("/fxml/SellerCenterView.fxml");
    }

    public static void showCreateAuction() {
        showCenter("/fxml/CreateAuctionView.fxml");
    }

    private static void showCenter(String fxmlPath) {
        if (appShellRoot == null) {
            throw new IllegalStateException("AppShell has not been loaded yet");
        }

        Parent content = loadFxml(fxmlPath);
        appShellRoot.setCenter(content);
    }

    private static void setScene(Parent root) {
        Scene scene = new Scene(root, 1280, 800);

        URL cssUrl = SceneManager.class.getResource(APP_CSS);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
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
