package com.auction.server;

import com.auction.server.config.AppProperties;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.service.AuctionManagerService;
import com.auction.server.socket.SocketServer;

public class ServerMain {
    public static void main(String[] args) {
        AppProperties appProperties = AppProperties.getInstance();

        SchemaInitializer.initialize();

        // Start background status manager
        AuctionManagerService auctionManager = new AuctionManagerService(new SQLiteAuctionDao());
        auctionManager.start();

        int port = appProperties.getServerPort();
        SocketServer server = new SocketServer(port);
        server.start();
    }
}
