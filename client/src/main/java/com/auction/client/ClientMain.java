package com.auction.client;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.NotificationManager;
import com.auction.client.util.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMain extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    @Override
    public void start(Stage stage) {
        // Connect to server in a background thread to avoid blocking UI
        new Thread(() -> {
            try {
                SocketClient.getInstance().connect();
            } catch (Exception e) {
                logger.error("Failed to connect to server", e);
                // Optionally show error to user
            }
        }).start();

        NotificationManager.initialize();
        SceneManager.initialize(stage);
        SceneManager.showLogin();
    }

    @Override
    public void stop() {
        SocketClient.getInstance().disconnect();
        logger.info("Application stopping, socket disconnected.");
    }

    public static void main(String[] args) {
        // Set default timezone to Vietnam
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        
        // Register Global Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new com.auction.client.util.GlobalExceptionHandler());
        
        launch(args);
    }
}
