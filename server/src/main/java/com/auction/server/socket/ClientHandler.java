package com.auction.server.socket;

import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Response;
import com.auction.server.util.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final JsonMapper jsonMapper;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.jsonMapper = JsonMapper.getInstance();
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);

                Response<String> response = Response.ok(
                        MessageType.GET_DASHBOARD,
                        "server-echo",
                        "Server received your message",
                        line
                );

                writer.println(jsonMapper.toJson(response));
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        }
    }
}
