package com.auction.client.socket;

import com.auction.client.util.JsonMapper;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton SocketClient for managing TCP connection with the server.
 * Handles asynchronous communication using CompletableFuture.
 */
public final class SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private static final SocketClient INSTANCE = new SocketClient();

    private String host = "localhost";
    private int port = 8080;
    private String token;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean running;

    // Maps requestId to its pending CompletableFuture
    private final Map<String, CompletableFuture<Response<?>>> pendingRequests = new ConcurrentHashMap<>();

    private SocketClient() {}

    public static SocketClient getInstance() {
        return INSTANCE;
    }

    /**
     * Connects to the server and starts the listener thread.
     */
    public synchronized void connect() throws IOException {
        if (running) return;

        logger.info("Connecting to server at {}:{}", host, port);
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        running = true;
        listenerThread = new Thread(this::listen, "SocketListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        logger.info("Connected and listener thread started.");
    }

    /**
     * Closes the connection.
     */
    public synchronized void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (listenerThread != null) listenerThread.interrupt();
        } catch (IOException e) {
            logger.error("Error while disconnecting", e);
        }
        logger.info("Disconnected from server.");
    }

    /**
     * Sends a request and returns a CompletableFuture for the response.
     */
    public <T, R> CompletableFuture<Response<R>> sendRequest(Request<T> request) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new IOException("Not connected to server"));
        }

        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);
        request.setToken(this.token);

        CompletableFuture<Response<?>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        String json = JsonMapper.getInstance().toJson(request);
        logger.debug("Sending request: {}", json);
        out.println(json);

        // Cast to specific return type for the caller
        return future.thenApply(response -> (Response<R>) response);
    }

    public boolean isConnected() {
        return running && socket != null && socket.isConnected() && out != null;
    }

    /**
     * Listener loop that reads messages from the server.
     */
    private void listen() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                logger.debug("Received message: {}", line);
                handleMessage(line);
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Connection lost", e);
                // In a real app, we might trigger a reconnection logic here
            }
        } finally {
            disconnect();
        }
    }

    private void handleMessage(String json) {
        try {
            Response<?> response = JsonMapper.getInstance().fromJson(json, Response.class);
            String requestId = response.getRequestId();

            if (requestId != null) {
                CompletableFuture<Response<?>> future = pendingRequests.remove(requestId);
                if (future != null) {
                    future.complete(response);
                } else {
                    logger.warn("Received response for unknown requestId: {}", requestId);
                }
            } else {
                // Handle realtime events (server pushes)
                handleEvent(response);
            }
        } catch (Exception e) {
            logger.error("Failed to parse or handle message: {}", json, e);
        }
    }

    private void handleEvent(Response<?> event) {
        logger.info("Received realtime event: {}", event.getType());
        // TODO: Implement an event bus or observer pattern for UI updates
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
