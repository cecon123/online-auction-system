package com.auction.client.service;

import com.auction.client.socket.SocketClient;
import com.auction.client.util.JsonMapper;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
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
}
