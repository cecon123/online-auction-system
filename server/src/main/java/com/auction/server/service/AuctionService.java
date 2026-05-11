package com.auction.server.service;

import com.auction.common.dto.auction.AuctionEventDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.auction.UpdateAuctionRequest;
import com.auction.common.dto.notification.SystemNotificationDto;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.common.protocol.MessageType;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.factory.ItemFactory;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for managing auction lifecycle and creation. */
public class AuctionService {

  private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

  private final AuctionDao auctionDao;
  private final ItemDao itemDao;
  private final com.auction.server.dao.BidDao bidDao;
  private final WalletService walletService;
  private final NotificationService notificationService;
  private final ItemFactory itemFactory;

  public AuctionService(
      AuctionDao auctionDao,
      ItemDao itemDao,
      com.auction.server.dao.BidDao bidDao,
      WalletService walletService) {
    this.auctionDao = auctionDao;
    this.itemDao = itemDao;
    this.bidDao = bidDao;
    this.walletService = walletService;
    this.notificationService = NotificationService.getInstance();
    this.itemFactory = new ItemFactory();
  }

  /**
   * Creates a new item and its associated auction.
   *
   * @param sellerId The ID of the user creating the auction.
   * @param request The request containing item and auction details.
   * @return A summary DTO of the newly created auction.
   */
  public AuctionSummaryDto createAuction(long sellerId, CreateAuctionRequest request) {
    logger.info("Creating new auction for seller ID: {}, Item: {}", sellerId, request.itemName());

    // 1. Create and persist Item
    ItemFactory.CreateItemData itemData =
        new ItemFactory.CreateItemData(
            0L,
            sellerId,
            request.itemType(),
            request.itemName(),
            request.description(),
            request.condition(),
            request.startingPrice(),
            request.imagePath(),
            null, // brand
            null, // model
            null, // artist
            null, // material
            null, // manufacturer
            0, // year
            LocalDateTime.now());

    Item item = itemFactory.create(itemData);

    long itemId = itemDao.create(item);
    logger.debug("Item created with ID: {}", itemId);

    // 2. Create and persist Auction
    LocalDateTime now = LocalDateTime.now();
    AuctionStatus initialStatus =
        request.startTime().isAfter(now) ? AuctionStatus.OPEN : AuctionStatus.RUNNING;

    Auction auction =
        new Auction(
            0L, // ID will be generated
            itemId,
            sellerId,
            request.startingPrice(),
            request.startingPrice(), // highestMaxBid = startingPrice
            request.reservePrice(),
            null, // No highest bidder yet
            request.startTime(),
            request.endTime(),
            initialStatus,
            0L, // Initial version
            now);

    long auctionId = auctionDao.create(auction);
    logger.info("Auction created with ID: {} for Item ID: {}", auctionId, itemId);

    return new AuctionSummaryDto(
        auctionId,
        request.itemName(),
        request.itemType(),
        request.startingPrice(),
        request.startingPrice(),
        null, // No highest bidder yet
        request.startTime(),
        request.endTime(),
        initialStatus,
        request.imagePath());
  }

  /**
   * Cancels an auction by the seller. Allowed only if the auction is in OPEN or RUNNING status.
   *
   * @param userId The ID of the seller.
   * @param auctionId The ID of the auction to cancel.
   * @throws IllegalArgumentException if auction is not found.
   * @throws IllegalStateException if user is not the seller or status is invalid.
   */
  public void cancelAuction(long userId, long auctionId) {
    Auction auction =
        auctionDao
            .findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

    if (auction.getSellerId() != userId) {
      throw new IllegalStateException("You can only cancel your own auctions.");
    }

    if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
      throw new IllegalStateException(
          "Auction cannot be canceled in its current status: " + auction.getStatus());
    }

    auction.setStatus(AuctionStatus.CANCELED);
    auctionDao.update(auction);

    performCancellationCleanup(auction, "Seller canceled the auction.");

    logger.info("User {} canceled Auction {}", userId, auctionId);
  }

  /**
   * Cancels an auction by an administrator.
   *
   * @param adminId The ID of the administrator.
   * @param auctionId The ID of the auction to cancel.
   * @throws IllegalArgumentException if auction is not found.
   * @throws IllegalStateException if status is invalid for cancellation.
   */
  public void adminCancelAuction(long adminId, long auctionId) {
    Auction auction =
        auctionDao
            .findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

    if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
      throw new IllegalStateException(
          "Auction cannot be canceled in its current status: " + auction.getStatus());
    }

    auction.setStatus(AuctionStatus.CANCELED);
    auctionDao.update(auction);

    performCancellationCleanup(
        auction, "Administrator canceled the auction due to policy violation.");

    logger.info("Admin {} canceled Auction {}", adminId, auctionId);
  }

  /**
   * Performs cleanup tasks after an auction is canceled, including refunding the highest bidder and
   * notifying relevant parties.
   *
   * @param auction The canceled auction.
   * @param reason The reason for cancellation.
   */
  private void performCancellationCleanup(Auction auction, String reason) {
    LocalDateTime now = LocalDateTime.now();

    // 1. Release funds for the current highest bidder
    if (auction.getHighestBidderId() != null && auction.getHighestMaxBid() != null) {
      walletService.releaseFunds(auction.getHighestBidderId(), auction.getHighestMaxBid());

      // Notify the outbid (now canceled) leader
      SystemNotificationDto refundNotice =
          new SystemNotificationDto(
              "Auction Canceled - Refund Issued",
              "Auction #"
                  + auction.getId()
                  + " has been canceled. Your locked bid of $"
                  + auction.getHighestMaxBid()
                  + " has been returned to your balance.",
              "INFO",
              now);
      notificationService.notifyUser(
          auction.getHighestBidderId(), MessageType.SYSTEM_NOTIFICATION, refundNotice);
    }

    // 2. Notify Seller
    SystemNotificationDto sellerNotice =
        new SystemNotificationDto(
            "Auction Canceled",
            "Your auction #" + auction.getId() + " has been canceled. Reason: " + reason,
            "WARNING",
            now);
    notificationService.notifyUser(
        auction.getSellerId(), MessageType.SYSTEM_NOTIFICATION, sellerNotice);

    // 3. Broadcast to all observers (Bidders currently viewing or subscribed)
    notificationService.broadcast(
        auction.getId(),
        MessageType.AUCTION_CANCELED,
        new AuctionEventDto(
            auction.getId(), AuctionStatus.CANCELED, reason, null, null, auction.getEndTime()));
  }

  /**
   * Updates a seller's own auction that has no bids yet.
   *
   * @param sellerId The user requesting the update.
   * @param request The updated fields.
   */
  /**
   * Updates an existing auction. Only allowed for the seller, before any bids are placed, and if
   * the status is OPEN.
   *
   * @param sellerId The ID of the seller.
   * @param request The update request containing new details.
   * @throws IllegalArgumentException if auction or item is not found.
   * @throws IllegalStateException if bids exist or status/ownership is invalid.
   */
  public void updateAuction(long sellerId, UpdateAuctionRequest request) {
    Auction auction =
        auctionDao
            .findById(request.auctionId())
            .orElseThrow(
                () -> new IllegalArgumentException("Auction not found: " + request.auctionId()));

    if (auction.getSellerId() != sellerId) {
      throw new IllegalStateException("You can only edit your own auctions.");
    }

    if (auction.getHighestBidderId() != null) {
      throw new IllegalStateException("Cannot edit auction. Bids have already been placed.");
    }

    if (auction.getStatus() != AuctionStatus.OPEN) {
      throw new IllegalStateException(
          "Auction cannot be edited in its current status: " + auction.getStatus());
    }

    // Update Item fields
    Item item =
        itemDao
            .findById(auction.getItemId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Item not found for auction: " + request.auctionId()));

    item.setName(request.itemName());
    item.setDescription(request.description());
    item.setCondition(request.condition());
    item.setImagePath(request.imagePath());
    item.setStartingPrice(request.startingPrice());
    itemDao.update(item);

    // Update Auction fields
    auction.setStartTime(request.startTime());
    auction.setEndTime(request.endTime());
    auction.setReservePrice(request.reservePrice());

    // Auto update status if start time has passed
    LocalDateTime now = LocalDateTime.now();
    if (!request.startTime().isAfter(now)) {
      auction.setStatus(AuctionStatus.RUNNING);
    }

    // Reset current price to new starting price (no bids yet, so this is safe)
    auction.setCurrentPrice(request.startingPrice());
    auctionDao.update(auction);

    logger.info("User {} updated Auction {}", sellerId, request.auctionId());
  }
}
