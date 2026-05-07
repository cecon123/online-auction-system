package com.auction.client.service;

import com.auction.client.socket.SocketClient;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service for wallet-related actions using Socket communication.
 */
public class WalletClientService {
    private final SocketClient socketClient;

    public WalletClientService() {
        this.socketClient = SocketClient.getInstance();
    }

    public CompletableFuture<Response<BigDecimal>> deposit(BigDecimal amount) {
        Request<BigDecimal> request = new Request<>(MessageType.DEPOSIT, null, null, amount);
        return socketClient.sendRequest(request);
    }

    public CompletableFuture<Response<BigDecimal>> withdraw(BigDecimal amount) {
        Request<BigDecimal> request = new Request<>(MessageType.WITHDRAW, null, null, amount);
        return socketClient.sendRequest(request);
    }
}
