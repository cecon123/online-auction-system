package com.auction.client;

import com.auction.client.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.initialize(stage);
        SceneManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
