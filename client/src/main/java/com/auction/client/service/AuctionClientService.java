package com.auction.client.service;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for auction-related actions using Socket communication.
 */
public class AuctionClientService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionClientService.class);
    private final SocketClient socketClient;
    private final JsonMapper jsonMapper;

    public AuctionClientService() {
        this.socketClient = SocketClient.getInstance();
        this.jsonMapper = JsonMapper.getInstance();
    }

    /**
     * Fetches all active and upcoming auctions.
     */
    public CompletableFuture<Response<List<AuctionSummaryDto>>> getAuctions() {
        Request<Void> request = new Request<>(MessageType.GET_AUCTIONS, null, null, null);
        
        return socketClient.<Void, List<AuctionSummaryDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<AuctionSummaryDto> data = jsonMapper.convertList(response.getData(), AuctionSummaryDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches details of a specific auction.
     */
    public CompletableFuture<Response<AuctionDetailDto>> getAuctionDetail(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.GET_AUCTION_DETAIL, null, null, auctionId);
        
        return socketClient.<Long, AuctionDetailDto>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    AuctionDetailDto data = jsonMapper.convertData(response.getData(), AuctionDetailDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Places a bid on an auction.
     */
    public CompletableFuture<Response<PlaceBidResponse>> placeBid(Long auctionId, java.math.BigDecimal amount) {
        PlaceBidRequest bidData = new PlaceBidRequest(auctionId, amount);
        Request<PlaceBidRequest> request = new Request<>(MessageType.PLACE_BID, null, null, bidData);
        
        return socketClient.<PlaceBidRequest, PlaceBidResponse>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    PlaceBidResponse data = jsonMapper.convertData(response.getData(), PlaceBidResponse.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Subscribes to realtime updates for an auction.
     */
    public CompletableFuture<Response<Void>> subscribeAuction(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.SUBSCRIBE_AUCTION, null, null, auctionId);
        return socketClient.sendRequest(request);
    }

    /**
     * Unsubscribes from realtime updates for an auction.
     */
    public CompletableFuture<Response<Void>> unsubscribeAuction(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.UNSUBSCRIBE_AUCTION, null, null, auctionId);
        return socketClient.sendRequest(request);
    }

    /**
     * Creates a new auction.
     */
    public CompletableFuture<Response<AuctionSummaryDto>> createAuction(CreateAuctionRequest createRequest) {
        Request<CreateAuctionRequest> request = new Request<>(MessageType.CREATE_AUCTION, null, null, createRequest);
        
        return socketClient.<CreateAuctionRequest, AuctionSummaryDto>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    AuctionSummaryDto data = jsonMapper.convertData(response.getData(), AuctionSummaryDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Updates an existing auction.
     */
    public CompletableFuture<Response<Void>> updateAuction(com.auction.common.dto.auction.UpdateAuctionRequest updateRequest) {
        Request<com.auction.common.dto.auction.UpdateAuctionRequest> request = new Request<>(MessageType.UPDATE_AUCTION, null, null, updateRequest);
        return socketClient.sendRequest(request);
    }

    /**
     * Fetches the bid history for an auction.
     */
    public CompletableFuture<Response<List<PlaceBidResponse>>> getBidHistory(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.GET_BID_HISTORY, null, null, auctionId);
        
        return socketClient.<Long, List<PlaceBidResponse>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<PlaceBidResponse> data = jsonMapper.convertList(response.getData(), PlaceBidResponse.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches auctions created by the current user.
     */
    public CompletableFuture<Response<List<AuctionSummaryDto>>> getSellerAuctions() {
        Request<Void> request = new Request<>(MessageType.GET_SELLER_AUCTIONS, null, null, null);

        return socketClient.<Void, List<AuctionSummaryDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<AuctionSummaryDto> data = jsonMapper.convertList(response.getData(), AuctionSummaryDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches statistics for the current seller.
     */
    public CompletableFuture<Response<com.auction.common.dto.dashboard.SellerStatsDto>> getSellerStats() {
        Request<Void> request = new Request<>(MessageType.GET_SELLER_STATS, null, null, null);

        return socketClient.<Void, com.auction.common.dto.dashboard.SellerStatsDto>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    com.auction.common.dto.dashboard.SellerStatsDto data = jsonMapper.convertData(response.getData(), com.auction.common.dto.dashboard.SellerStatsDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches dashboard statistics.
     */
    public CompletableFuture<Response<com.auction.common.dto.dashboard.DashboardDto>> getDashboard() {
        Request<Void> request = new Request<>(MessageType.GET_DASHBOARD, null, null, null);

        return socketClient.<Void, com.auction.common.dto.dashboard.DashboardDto>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    com.auction.common.dto.dashboard.DashboardDto data = jsonMapper.convertData(response.getData(), com.auction.common.dto.dashboard.DashboardDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches auctions where the current user has placed bids.
     */
    public CompletableFuture<Response<List<AuctionSummaryDto>>> getMyBids() {
        Request<Void> request = new Request<>(MessageType.GET_MY_BIDS, null, null, null);
        
        return socketClient.<Void, List<AuctionSummaryDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<AuctionSummaryDto> data = jsonMapper.convertList(response.getData(), AuctionSummaryDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Fetches the detailed bid history for the current user.
     */
    public CompletableFuture<Response<List<com.auction.common.dto.bid.BidHistoryDto>>> getUserBidHistory() {
        Request<Void> request = new Request<>(MessageType.GET_USER_BID_HISTORY, null, null, null);
        
        return socketClient.<Void, List<com.auction.common.dto.bid.BidHistoryDto>>sendRequest(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    List<com.auction.common.dto.bid.BidHistoryDto> data = jsonMapper.convertList(response.getData(), com.auction.common.dto.bid.BidHistoryDto.class);
                    response.setData(data);
                }
                return response;
            });
    }

    /**
     * Cancels an auction.
     */
    public CompletableFuture<Response<Void>> cancelAuction(Long auctionId) {
        Request<Long> request = new Request<>(MessageType.CANCEL_AUCTION, null, null, auctionId);
        return socketClient.<Long, Void>sendRequest(request);
    }
}
