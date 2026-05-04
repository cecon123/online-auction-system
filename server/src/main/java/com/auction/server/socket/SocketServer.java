package com.auction.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final ExecutorService clientPool;

    public SocketServer(int port) {
        this.port = port;
        this.clientPool = Executors.newCachedThreadPool();
    }

    public void start() {
        System.out.println("Auction server starting on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Auction server is running.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                clientPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            clientPool.shutdown();
        }
    }
}
