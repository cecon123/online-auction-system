package com.auction.server.socket;

import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.auction.UpdateAuctionRequest;
import com.auction.common.dto.dashboard.SellerStatsDto;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.config.AppProperties;
import com.auction.server.dao.UserDao;
import com.auction.server.util.ImageUtil;
import java.math.BigDecimal;
import java.util.List;

final class AuctionRequestHandler {

  private final RouterContext context;

  AuctionRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<List<AuctionSummaryDto>> handleGetAuctions(Request<?> request) {
    return mapToAuctionSummaries(context.auctionDao.findAll(), request.getRequestId());
  }

  Response<List<AuctionSummaryDto>> handleGetSellerAuctions(Request<?> request) {
    Long userId = context.requireRole(request, Role.SELLER);
    return mapToAuctionSummaries(
        context.auctionDao.findBySellerId(userId),
        MessageType.GET_SELLER_AUCTIONS,
        "Seller auctions loaded",
        request.getRequestId());
  }

  Response<SellerStatsDto> handleGetSellerStats(Request<?> request) {
    Long userId = context.requireRole(request, Role.SELLER);
    List<Auction> sellerAuctions = context.auctionDao.findBySellerId(userId);

    BigDecimal expectedRevenue = BigDecimal.ZERO;
    BigDecimal totalRevenue = BigDecimal.ZERO;
    int activeAuctionsCount = 0;
    int closedAuctionsCount = 0;
    int successfulAuctionsCount = 0;
    int totalAuctionsCount = sellerAuctions.size();

    for (Auction auction : sellerAuctions) {
      if (auction.getStatus() == AuctionStatus.RUNNING
          || auction.getStatus() == AuctionStatus.OPEN) {
        activeAuctionsCount++;
        if (auction.getCurrentPrice() != null) {
          expectedRevenue = expectedRevenue.add(auction.getCurrentPrice());
        }
      } else if (auction.getStatus() == AuctionStatus.PAID
          || auction.getStatus() == AuctionStatus.CANCELED) {
        closedAuctionsCount++;
        if (auction.getStatus() == AuctionStatus.PAID) {
          successfulAuctionsCount++;
          if (auction.getCurrentPrice() != null) {
            totalRevenue = totalRevenue.add(auction.getCurrentPrice());
          }
        }
      } else if (auction.getStatus() == AuctionStatus.FINISHED) {
        closedAuctionsCount++;
      }
    }

    int successRate = 0;
    if (closedAuctionsCount > 0) {
      successRate =
          (int) Math.round((double) successfulAuctionsCount / closedAuctionsCount * 100);
    }

    int totalBidsReceived = 0;
    for (Auction auction : sellerAuctions) {
      List<?> history = context.bidService.getBidHistory(auction.getId());
      if (history != null) {
        totalBidsReceived += history.size();
      }
    }

    SellerStatsDto stats =
        new SellerStatsDto(
            expectedRevenue,
            totalRevenue,
            totalBidsReceived,
            successRate,
            activeAuctionsCount,
            totalAuctionsCount);

    return Response.ok(
        MessageType.GET_SELLER_STATS, request.getRequestId(), "Seller stats loaded", stats);
  }

  Response<AuctionDetailDto> handleGetAuctionDetail(Request<?> request) {
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new IllegalArgumentException("Missing auction ID");
    }

    Auction auction =
        context.auctionDao
            .findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

    Item item =
        context.itemDao
            .findById(auction.getItemId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Item not found for auction: " + auction.getItemId()));

    String sellerName =
        context.userDao
            .findById(auction.getSellerId())
            .map(UserDao.UserRecord::username)
            .orElse("Unknown Seller");

    String highestBidder = null;
    if (auction.getHighestBidderId() != null) {
      highestBidder =
          context.userDao
              .findById(auction.getHighestBidderId())
              .map(UserDao.UserRecord::username)
              .orElse("Unknown");
    }

    AuctionDetailDto detail =
        new AuctionDetailDto(
            auction.getId(),
            auction.getItemId(),
            auction.getSellerId(),
            sellerName,
            item.getName(),
            item.getItemType(),
            item.getCondition(),
            item.getDescription(),
            auction.getCurrentPrice(),
            auction.getCurrentPrice(),
            auction.getReservePrice(),
            highestBidder,
            auction.getStartTime(),
            auction.getEndTime(),
            auction.getStatus(),
            item.getImagePath());

    return Response.ok(
        MessageType.GET_AUCTION_DETAIL, request.getRequestId(), "Auction detail loaded", detail);
  }

  Response<AuctionSummaryDto> handleCreateAuction(Request<?> request) {
    Long userId = context.requireRole(request, Role.SELLER);
    CreateAuctionRequest data =
        context.requireData(
            request, CreateAuctionRequest.class, "CREATE_AUCTION requires auction payload");

    validateCreateAuctionRequest(data);

    String savedFileName =
        ImageUtil.saveBase64Image(data.imageBase64(), AppProperties.getInstance().getAssetDir());

    CreateAuctionRequest finalRequest = data;
    if (savedFileName != null) {
      finalRequest =
          new CreateAuctionRequest(
              data.itemName(),
              data.itemType(),
              data.condition(),
              data.description(),
              data.startingPrice(),
              data.reservePrice(),
              data.startTime(),
              data.endTime(),
              savedFileName,
              null);
    }

    AuctionSummaryDto response = context.auctionService.createAuction(userId, finalRequest);
    context.notificationService.broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

    return Response.ok(
        MessageType.CREATE_AUCTION,
        request.getRequestId(),
        "Auction created successfully",
        response);
  }

  Response<?> handleUpdateAuction(Request<?> request) {
    Long userId = context.requireRole(request, Role.SELLER);
    UpdateAuctionRequest data =
        context.requireData(
            request, UpdateAuctionRequest.class, "UPDATE_AUCTION requires auction payload");

    validateUpdateAuctionRequest(data);

    String savedFileName =
        ImageUtil.saveBase64Image(data.imageBase64(), AppProperties.getInstance().getAssetDir());

    UpdateAuctionRequest finalRequest = data;
    if (savedFileName != null) {
      finalRequest =
          new UpdateAuctionRequest(
              data.auctionId(),
              data.itemName(),
              data.itemType(),
              data.condition(),
              data.description(),
              data.startingPrice(),
              data.reservePrice(),
              data.startTime(),
              data.endTime(),
              savedFileName,
              null);
    }

    context.auctionService.updateAuction(userId, finalRequest);
    context.notificationService.broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

    return Response.ok(
        MessageType.UPDATE_AUCTION, request.getRequestId(), "Auction updated successfully", null);
  }

  Response<?> handleCancelAuction(Request<?> request) {
    Long userId = context.requireRole(request, Role.SELLER);
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new IllegalArgumentException("Missing auction ID");
    }

    context.auctionService.cancelAuction(userId, auctionId);
    context.notificationService.broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

    return Response.ok(
        MessageType.CANCEL_AUCTION, request.getRequestId(), "Auction canceled successfully", null);
  }

  Response<List<AuctionDetailDto>> handleAdminGetAuctions(Request<?> request) {
    context.requireAdmin(request);
    List<AuctionDetailDto> auctions = context.auctionDao.findAll().stream().map(this::toDto).toList();

    return Response.ok(
        MessageType.ADMIN_GET_AUCTIONS, request.getRequestId(), "Auctions loaded", auctions);
  }

  Response<?> handleAdminCancelAuction(Request<?> request) {
    Long adminId = context.requireAdmin(request);
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new IllegalArgumentException("Missing auction ID");
    }

    context.auctionService.adminCancelAuction(adminId, auctionId);
    context.notificationService.broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

    return Response.ok(
        MessageType.ADMIN_CANCEL_AUCTION,
        request.getRequestId(),
        "Auction canceled successfully by Admin",
        null);
  }

  private Response<List<AuctionSummaryDto>> mapToAuctionSummaries(
      List<Auction> auctionList, String requestId) {
    return mapToAuctionSummaries(
        auctionList, MessageType.GET_AUCTIONS, "Auctions loaded", requestId);
  }

  private Response<List<AuctionSummaryDto>> mapToAuctionSummaries(
      List<Auction> auctionList, MessageType type, String message, String requestId) {
    List<Item> itemList = context.itemDao.findAll();
    java.util.Map<Long, Item> itemMap =
        itemList.stream().collect(java.util.stream.Collectors.toMap(Item::getId, i -> i));

    List<AuctionSummaryDto> auctions =
        auctionList.stream()
            .map(auction -> toSummaryDto(auction, itemMap.get(auction.getItemId())))
            .toList();

    return Response.ok(type, requestId, message, auctions);
  }

  private AuctionSummaryDto toSummaryDto(Auction auction, Item item) {
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

  private AuctionDetailDto toDto(Auction auction) {
    Item item = context.itemDao.findById(auction.getItemId()).orElse(null);
    String sellerName =
        context.userDao
            .findById(auction.getSellerId())
            .map(UserDao.UserRecord::username)
            .orElse("Unknown Seller");
    String highestBidder = null;
    if (auction.getHighestBidderId() != null) {
      highestBidder =
          context.userDao
              .findById(auction.getHighestBidderId())
              .map(UserDao.UserRecord::username)
              .orElse("Unknown");
    }

    return new AuctionDetailDto(
        auction.getId(),
        auction.getItemId(),
        auction.getSellerId(),
        sellerName,
        item != null ? item.getName() : "Unknown Item",
        item != null ? item.getItemType() : ItemType.ELECTRONICS,
        item != null ? item.getCondition() : "",
        item != null ? item.getDescription() : "",
        item != null ? item.getStartingPrice() : auction.getCurrentPrice(),
        auction.getCurrentPrice(),
        auction.getReservePrice(),
        highestBidder,
        auction.getStartTime(),
        auction.getEndTime(),
        auction.getStatus(),
        item != null ? item.getImagePath() : null);
  }

  private void validateCreateAuctionRequest(CreateAuctionRequest data) {
    if (context.isBlank(data.itemName())) {
      throw new IllegalArgumentException("Item name is required.");
    }
    if (data.itemType() == null) {
      throw new IllegalArgumentException("Item type is required.");
    }
    if (context.isBlank(data.condition())) {
      throw new IllegalArgumentException("Condition is required.");
    }
    if (context.isBlank(data.description())) {
      throw new IllegalArgumentException("Description is required.");
    }
    if (data.startingPrice() == null || data.startingPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Starting price must be positive.");
    }
    if (data.startTime() == null) {
      throw new IllegalArgumentException("Start time is required.");
    }
    if (data.endTime() == null) {
      throw new IllegalArgumentException("End time is required.");
    }
    if (!data.endTime().isAfter(data.startTime())) {
      throw new IllegalArgumentException("End time must be after start time.");
    }
  }

  private void validateUpdateAuctionRequest(UpdateAuctionRequest data) {
    if (data.auctionId() <= 0) {
      throw new IllegalArgumentException("Invalid auction ID.");
    }
    if (context.isBlank(data.itemName())) {
      throw new IllegalArgumentException("Item name is required.");
    }
    if (data.itemType() == null) {
      throw new IllegalArgumentException("Item type is required.");
    }
    if (context.isBlank(data.condition())) {
      throw new IllegalArgumentException("Condition is required.");
    }
    if (context.isBlank(data.description())) {
      throw new IllegalArgumentException("Description is required.");
    }
    if (data.startingPrice() == null || data.startingPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Starting price must be positive.");
    }
    if (data.startTime() == null) {
      throw new IllegalArgumentException("Start time is required.");
    }
    if (data.endTime() == null) {
      throw new IllegalArgumentException("End time is required.");
    }
    if (!data.endTime().isAfter(data.startTime())) {
      throw new IllegalArgumentException("End time must be after start time.");
    }
  }
}
