package com.auction.server.socket;

import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.service.NotificationService;
import com.auction.server.util.JsonMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles a single connected client.
 *
 * The server uses newline-delimited JSON:
 * each request must be sent as one JSON line.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final JsonMapper jsonMapper;
    private RequestRouter requestRouter;
    private final NotificationService notificationService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.jsonMapper = JsonMapper.getInstance();
        this.notificationService = NotificationService.getInstance();
    }

    @Override
    public void run() {
        PrintWriter writer = null;
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            )
        ) {
            writer = new PrintWriter(socket.getOutputStream(), true);
            this.requestRouter = new RequestRouter(writer);
            String line;

            while ((line = reader.readLine()) != null) {
                Response<?> response = handleJsonLine(line);
                writer.println(jsonMapper.toJson(response));
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Client timed out due to inactivity: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            if (writer != null) {
                notificationService.unsubscribeFromAll(writer);
            }
        }
    }

    private Response<?> handleJsonLine(String line) {
        try {
            Request<?> request = jsonMapper.fromJson(line, Request.class);
            return requestRouter.route(request);
        } catch (RuntimeException e) {
            return Response.fail(
                null,
                null,
                "Invalid JSON request: " + e.getMessage()
            );
        }
    }
}
