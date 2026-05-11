package com.auction.client.service;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for authentication-related actions using Socket communication.
 */
public class AuthClientService {

    private static final Logger logger = LoggerFactory.getLogger(AuthClientService.class);
    private final SocketClient socketClient;
    private final JsonMapper jsonMapper;

    public AuthClientService() {
        this.socketClient = SocketClient.getInstance();
        this.jsonMapper = JsonMapper.getInstance();
        
        // Register silent re-authentication callback
        this.socketClient.setOnReconnect(() -> {
            String username = socketClient.getLastUsername();
            String password = socketClient.getLastPassword();
            if (username != null && password != null) {
                logger.info("Performing silent re-authentication for user: {}", username);
                login(username, password).thenAccept(response -> {
                    if (response.isSuccess()) {
                        logger.info("Silent re-authentication successful.");
                    } else {
                        logger.warn("Silent re-authentication failed: {}", response.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Authenticates a user.
     */
    public CompletableFuture<Response<LoginResponse>> login(String username, String password) {
        LoginRequest loginData = new LoginRequest(username, password);
        Request<LoginRequest> request = new Request<>(MessageType.LOGIN, null, null, loginData);

        return socketClient.<LoginRequest, LoginResponse>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    LoginResponse data = jsonMapper.convertData(response.getData(), LoginResponse.class);
                    response.setData(data);
                    // Store token and credentials in SocketClient
                    socketClient.setToken(data.token());
                    socketClient.setCredentials(username, password);
                    logger.info("Login successful for user: {}", username);
                }
                return response;
            });
    }

    /**
     * Registers a new user.
     */
    public CompletableFuture<Response<RegisterResponse>> register(RegisterRequest registerData) {
        Request<RegisterRequest> request = new Request<>(MessageType.REGISTER, null, null, registerData);

        return socketClient.<RegisterRequest, RegisterResponse>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    RegisterResponse data = jsonMapper.convertData(response.getData(), RegisterResponse.class);
                    response.setData(data);
                    logger.info("Registration successful for user: {}", registerData.username());
                }
                return response;
            });
    }
}
