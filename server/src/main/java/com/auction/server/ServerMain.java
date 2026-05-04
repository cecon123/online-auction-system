package com.auction.server;

import com.auction.server.socket.SocketServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;
        SocketServer server = new SocketServer(port);
        server.start();
    }
}
