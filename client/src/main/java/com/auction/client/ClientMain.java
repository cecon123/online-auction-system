package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("/fxml/LoginView.fxml"));

        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(ClientMain.class.getResource("/css/app.css").toExternalForm());

        stage.setTitle("AuctionPro - Online Auction System");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
