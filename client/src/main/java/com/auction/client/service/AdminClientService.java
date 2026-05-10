package com.auction.client.service;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.common.dto.auth.UpdateUserStatusRequest;
import com.auction.common.dto.auth.UserDto;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for administrative actions.
 */
public class AdminClientService {

    private static final Logger logger = LoggerFactory.getLogger(AdminClientService.class);
    private final SocketClient socketClient;
    private final JsonMapper jsonMapper;

    public AdminClientService() {
        this.socketClient = SocketClient.getInstance();
        this.jsonMapper = JsonMapper.getInstance();
    }

    /**
     * Gets all users (Admin only).
     */
    public CompletableFuture<Response<List<UserDto>>> getUsers() {
        Request<Void> request = new Request<>(MessageType.ADMIN_GET_USERS, null, null, null);

        return socketClient.<Void, List<UserDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<UserDto> data = jsonMapper.convertList(response.getData(), UserDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Updates a user's status (Admin only).
     */
    public CompletableFuture<Response<Void>> updateUserStatus(Long userId, boolean active) {
        UpdateUserStatusRequest data = new UpdateUserStatusRequest(userId, active);
        Request<UpdateUserStatusRequest> request = new Request<>(MessageType.ADMIN_UPDATE_USER_STATUS, null, null, data);

        return socketClient.<UpdateUserStatusRequest, Void>sendRequest(request);
    }

    /**
     * Gets all auctions (Admin only).
     */
    public CompletableFuture<Response<List<AuctionDetailDto>>> getAuctions() {
        Request<Void> request = new Request<>(MessageType.ADMIN_GET_AUCTIONS, null, null, null);

        return socketClient.<Void, List<AuctionDetailDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<AuctionDetailDto> data = jsonMapper.convertList(response.getData(), AuctionDetailDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Cancels an auction (Admin only).
     */
    public CompletableFuture<Response<Void>> cancelAuction(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.ADMIN_CANCEL_AUCTION, null, null, auctionId);
        return socketClient.<Long, Void>sendRequest(request);
    }
}
