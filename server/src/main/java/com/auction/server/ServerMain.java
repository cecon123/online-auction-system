package com.auction.server;

import com.auction.server.config.AppProperties;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.socket.SocketServer;

public class ServerMain {
    public static void main(String[] args) {
        AppProperties appProperties = AppProperties.getInstance();

        SchemaInitializer.initialize();

        int port = appProperties.getServerPort();
        SocketServer server = new SocketServer(port);
        server.start();
    }
}
