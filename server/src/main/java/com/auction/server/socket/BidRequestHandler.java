package com.auction.server.socket;

import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.bid.AutoBidDto;
import com.auction.common.dto.bid.BidHistoryDto;
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.dto.bid.SetAutoBidRequest;
import com.auction.common.enums.ItemType;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.exception.ValidationException;
import java.math.BigDecimal;
import java.util.List;

final class BidRequestHandler {

  private final RouterContext context;

  BidRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<?> handlePlaceBid(Request<?> request) {
    Long userId = context.requireRole(request, Role.BIDDER);
    PlaceBidRequest bidData =
        context.requireData(request, PlaceBidRequest.class, "Missing bid data");
    return Response.ok(
        MessageType.PLACE_BID,
        request.getRequestId(),
        "Bid accepted",
        context.bidService.placeBid(userId, bidData));
  }

  Response<List<PlaceBidResponse>> handleGetBidHistory(Request<?> request) {
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new ValidationException("Missing auction ID");
    }

    return Response.ok(
        MessageType.GET_BID_HISTORY,
        request.getRequestId(),
        "Bid history loaded",
        context.bidService.getBidHistory(auctionId));
  }

  Response<List<AuctionSummaryDto>> handleGetMyBids(Request<?> request) {
    Long userId = context.requireActiveUser(request);
    List<Long> auctionIds = context.bidService.getMyBids(userId);

    List<AuctionSummaryDto> auctions =
        auctionIds.stream()
            .map(context.auctionDao::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(this::toSummaryDto)
            .toList();

    return Response.ok(MessageType.GET_MY_BIDS, request.getRequestId(), "My bids loaded", auctions);
  }

  Response<List<BidHistoryDto>> handleGetUserBidHistory(Request<?> request) {
    Long userId = context.requireActiveUser(request);
    List<BidHistoryDto> history = context.bidService.getUserBidHistory(userId);

    List<BidHistoryDto> enrichedHistory =
        history.stream()
            .map(
                bid -> {
                  String title =
                      context.auctionDao
                          .findById(bid.auctionId())
                          .flatMap(auction -> context.itemDao.findById(auction.getItemId()))
                          .map(Item::getName)
                          .orElse(bid.auctionTitle());

                  return new BidHistoryDto(
                      bid.bidId(),
                      bid.auctionId(),
                      title,
                      bid.amount(),
                      bid.timestamp(),
                      bid.result());
                })
            .toList();

    return Response.ok(
        MessageType.GET_USER_BID_HISTORY,
        request.getRequestId(),
        "User bid history loaded",
        enrichedHistory);
  }

  Response<Void> handleSetAutoBid(Request<?> request) {
    Long userId = context.requireRole(request, Role.BIDDER);
    SetAutoBidRequest data =
        context.requireData(
            request, SetAutoBidRequest.class, "SET_AUTO_BID requires payload");

    context.bidService.setAutoBid(userId, data);

    return Response.ok(
        MessageType.SET_AUTO_BID, request.getRequestId(), "Auto-bid limit saved", null);
  }

  Response<AutoBidDto> handleGetAutoBid(Request<?> request) {
    Long userId = context.requireActiveUser(request);
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new ValidationException("Missing auction ID");
    }

    return Response.ok(
        MessageType.GET_AUTO_BID,
        request.getRequestId(),
        "Auto-bid limit loaded",
        context.bidService.getAutoBid(userId, auctionId).orElse(null));
  }

  private AuctionSummaryDto toSummaryDto(Auction auction) {
    Item item = context.itemDao.findById(auction.getItemId()).orElse(null);
    String title = (item != null) ? item.getName() : "Unknown Item";
    ItemType itemType = (item != null) ? item.getItemType() : ItemType.ELECTRONICS;
    String image = (item != null) ? item.getImagePath() : null;
    BigDecimal startingPrice = (item != null) ? item.getStartingPrice() : auction.getCurrentPrice();

    return new AuctionSummaryDto(
        auction.getId(),
        title,
        itemType,
        startingPrice,
        auction.getCurrentPrice(),
        auction.getHighestBidderId(),
        auction.getStartTime(),
        auction.getEndTime(),
        auction.getStatus(),
        image);
  }
}
