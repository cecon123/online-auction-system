package com.auction.server;

import com.auction.server.config.AppProperties;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.service.AuctionManagerService;
import com.auction.server.socket.SocketServer;

public class ServerMain {
  public static void main(String[] args) {
    // Set default timezone to Vietnam
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

    // Register Global Exception Handler
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) -> {
          System.err.println("CRITICAL: Uncaught exception in thread " + t.getName());
          e.printStackTrace();
          // In a real app, we might want to restart the service or send an alert
        });

    AppProperties appProperties = AppProperties.getInstance();

    SchemaInitializer.initialize();

    // Start background status manager
    com.auction.server.dao.UserDao userDao = new com.auction.server.dao.sqlite.SQLiteUserDao();
    AuctionManagerService auctionManager =
        new AuctionManagerService(
            new SQLiteAuctionDao(), userDao, new com.auction.server.service.WalletService(userDao));
    auctionManager.start();

    int port = appProperties.getServerPort();

    // Start asset server for images
    String assetDir = appProperties.getAssetDir();
    java.io.File dir = new java.io.File(assetDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    com.auction.server.socket.AssetServer assetServer =
        new com.auction.server.socket.AssetServer(appProperties.getAssetPort(), assetDir);
    assetServer.start();

    SocketServer server = new SocketServer(port);
    server.start();
  }
}
