package com.auction.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private final int port;
    private final ExecutorService clientPool;

    public SocketServer(int port) {
        this.port = port;
        this.clientPool = Executors.newCachedThreadPool();
    }

    public void start() {
        logger.info("Auction server starting on port {}", port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Auction server is running.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
                clientPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Server error: {}", e.getMessage());
        } finally {
            clientPool.shutdown();
        }
    }
}
