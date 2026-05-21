package com.auction.server.service;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import com.auction.server.concurrency.AuctionLockManager;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.AutoBidDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.Database;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.BusinessRuleException;
import com.auction.server.exception.InsufficientFundsException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling bidding business logic. Integrates locking for thread-safety and real
 * database persistence.
 */
public class BidService {

  private static final Logger logger = LoggerFactory.getLogger(BidService.class);

  private static final java.math.BigDecimal MIN_INCREMENT = new java.math.BigDecimal("10.00");
  private static final int ANTI_SNIPING_WINDOW_SECONDS = 30;
  private static final int EXTENSION_DURATION_SECONDS = 60;
  private static final long MIN_BID_INTERVAL_MS = 1000;

  private final java.util.Map<String, Long> lastBidTimes =
      new java.util.concurrent.ConcurrentHashMap<>();

  private static String formatMoney(java.math.BigDecimal amount) {
    if (amount == null) return "$0.00";
    return ("$" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString());
  }

  private final AuctionLockManager lockManager;
  private final AuctionDao auctionDao;
  private final BidDao bidDao;
  private final AutoBidDao autoBidDao;
  private final com.auction.server.dao.UserDao userDao;
  private final WalletService walletService;
  private final NotificationService notificationService;

  public BidService(
      AuctionDao auctionDao,
      BidDao bidDao,
      com.auction.server.dao.UserDao userDao,
      AutoBidDao autoBidDao,
      WalletService walletService) {
    this.lockManager = AuctionLockManager.getInstance();
    this.auctionDao = auctionDao;
    this.bidDao = bidDao;
    this.userDao = userDao;
    this.autoBidDao = autoBidDao;
    this.walletService = walletService;
    this.notificationService = NotificationService.getInstance();
  }

  /**
   * Processes a manual bid request (Traditional Bidding). The price jumps exactly to the requested
   * amount.
   *
   * @param bidderId The ID of the bidder.
   * @param request The bid details.
   * @return PlaceBidResponse on success.
   */
  public PlaceBidResponse placeBid(long bidderId, PlaceBidRequest request) {
    PlaceBidResponse response =
        lockManager.executeLocked(
            request.auctionId(),
            () ->
                Database.getInstance()
                    .runInTransaction(
                        () -> {
              LocalDateTime now = LocalDateTime.now();
              java.math.BigDecimal bidAmount = request.amount();

              // 0. Rate limiting check (per user, per auction)
              String rateKey = bidderId + ":" + request.auctionId();
              long currentTime = System.currentTimeMillis();
              Long lastTime = lastBidTimes.get(rateKey);
              if (lastTime != null && (currentTime - lastTime) < MIN_BID_INTERVAL_MS) {
                throw new BusinessRuleException("You are bidding too fast. Please wait a moment.");
              }

              // 1. Fetch auction and bidder
              Auction auction =
                  auctionDao
                      .findById(request.auctionId())
                      .orElseThrow(() -> new ResourceNotFoundException("Auction not found."));

              com.auction.server.dao.UserDao.UserRecord bidder =
                  userDao
                      .findById(bidderId)
                      .orElseThrow(() -> new ResourceNotFoundException("Bidder not found."));

              // 2. Preliminary Validations
              if (auction.getSellerId() == bidderId) {
                throw new InvalidBidException("You cannot bid on your own auction.");
              }
              if (auction.getStatus() != AuctionStatus.RUNNING
                  || now.isAfter(auction.getEndTime())) {
                throw new AuctionClosedException("Auction is not active.");
              }

              // 3. Traditional Bidding Validation (Must be higher than current + increment)
              java.math.BigDecimal minRequired = auction.getCurrentPrice();
              if (auction.getHighestBidderId() != null) {
                minRequired = minRequired.add(MIN_INCREMENT);
              }

              if (bidAmount.compareTo(minRequired) < 0) {
                throw new InvalidBidException("Your bid must be at least " + minRequired);
              }

              // 4. Update Auction State (Traditional: Price = bidAmount)
              Long previousLeaderId = auction.getHighestBidderId();
              java.math.BigDecimal previousMax =
                  auction.getHighestMaxBid(); // This was the old "Proxy" limit

              // 5. Lock new bidder's funds before releasing the old leader. The surrounding
              // transaction rolls everything back if auction or bid persistence fails.
              java.math.BigDecimal amountToLock = bidAmount;
              if (previousLeaderId != null && previousLeaderId == bidderId) {
                // Same bidder increasing their bid: only lock the difference
                amountToLock = bidAmount.subtract(previousMax);
              }

              if (amountToLock.compareTo(java.math.BigDecimal.ZERO) > 0) {
                com.auction.server.dao.UserDao.UserRecord currentBidder =
                    userDao.findById(bidderId).get();
                java.math.BigDecimal available =
                    currentBidder.balance().subtract(currentBidder.lockedBalance());
                if (available.compareTo(amountToLock) < 0) {
                  throw new InsufficientFundsException(
                      String.format(
                          "Insufficient balance. You need %s but only have %s available (check your active bids).",
                          formatMoney(amountToLock), formatMoney(available)));
                }
                walletService.lockFunds(bidderId, amountToLock);
              } else if (amountToLock.compareTo(java.math.BigDecimal.ZERO) < 0) {
                // Same bidder reducing their proxy max: release the difference
                walletService.releaseFunds(bidderId, amountToLock.abs());
              }

              // 6. ESCROW: Release old leader's funds (Full Max Lock model)
              if (previousLeaderId != null && previousLeaderId != bidderId) {
                walletService.releaseFunds(previousLeaderId, previousMax);
              }

              // 7. Update Auction
              auction.setCurrentPrice(bidAmount);
              auction.setHighestMaxBid(bidAmount); // In traditional bid, Max = Price
              auction.setHighestBidderId(bidderId);

              // 8. Anti-Sniping Extension
              boolean timeExtended = false;
              if (now.isAfter(auction.getEndTime().minusSeconds(ANTI_SNIPING_WINDOW_SECONDS))) {
                auction.setEndTime(auction.getEndTime().plusSeconds(EXTENSION_DURATION_SECONDS));
                timeExtended = true;
                logger.info(
                    "Auction {} extended by {}s", auction.getId(), EXTENSION_DURATION_SECONDS);
              }

              auctionDao.update(auction);

              // 9. Record Transaction
              BidTransaction transaction =
                  new BidTransaction(0L, auction.getId(), bidderId, bidAmount, now);
              bidDao.create(transaction);
              lastBidTimes.put(rateKey, currentTime);

              // 10. Notify
              notificationService.broadcast(
                  auction.getId(),
                  com.auction.common.protocol.MessageType.BID_UPDATE,
                  new com.auction.common.dto.bid.BidUpdateEvent(
                      auction.getId(),
                      bidder.username(),
                      auction.getCurrentPrice(),
                      now,
                      auction.getEndTime()));

              // 10.5 Seller Notification: First Bid
              if (previousLeaderId == null) {
                com.auction.common.dto.notification.SystemNotificationDto firstBidNotice =
                    new com.auction.common.dto.notification.SystemNotificationDto(
                        "First Bid Received! \uD83C\uDF89", // 🎉
                        "Your auction #"
                            + auction.getId()
                            + " just received its first bid of $"
                            + bidAmount
                            + " from "
                            + bidder.username()
                            + ".",
                        "INFO",
                        now);
                notificationService.notifyUser(
                    auction.getSellerId(),
                    com.auction.common.protocol.MessageType.SYSTEM_NOTIFICATION,
                    firstBidNotice);
              }

              if (timeExtended) {
                notificationService.broadcast(
                    auction.getId(),
                    com.auction.common.protocol.MessageType.TIME_EXTENDED,
                    new com.auction.common.dto.auction.AuctionEventDto(
                        auction.getId(),
                        auction.getStatus(),
                        "Time extended!",
                        null,
                        null,
                        auction.getEndTime()));
              }

              return new PlaceBidResponse(
                  auction.getId(), auction.getCurrentPrice(), bidder.username(), now);
                        }));

    try {
      // 11. AFTER Manual Bid: Trigger Auto-bids for others to respond.
      triggerAutoBids(request.auctionId(), bidderId);
    } catch (RuntimeException e) {
      logger.warn(
          "Auto-bid resolver failed after accepted manual bid on auction {}. Manual bid remains valid.",
          request.auctionId(),
          e);
    }

    return response;
  }

  /**
   * Sets or updates an auto-bid rule (Proxy Bidding limit). This allows the system to bid on behalf
   * of the user up to a maximum amount.
   *
   * @param userId The ID of the user.
   * @param request The auto-bid request containing max bid and auction ID.
   * @throws com.auction.server.exception.BusinessException if the request violates bid rules.
   */
  public void setAutoBid(long userId, com.auction.common.dto.bid.SetAutoBidRequest request) {
    lockManager.executeLocked(
        request.auctionId(),
        () -> {
          com.auction.server.dao.UserDao.UserRecord user =
              userDao
                  .findById(userId)
                  .orElseThrow(() -> new ResourceNotFoundException("User not found."));

          // Validation: Max bid must be higher than current price + increment
          Auction auction =
              auctionDao
                  .findById(request.auctionId())
                  .orElseThrow(() -> new ResourceNotFoundException("Auction not found."));

          java.math.BigDecimal minRequired = auction.getCurrentPrice().add(request.increment());
          if (request.active() && request.maxBid().compareTo(minRequired) < 0) {
            throw new InvalidBidException("Max bid must be at least " + minRequired);
          }

          // Wallet check: Must be able to lock the full max bid
          // IMPORTANT: Account for money already locked for THIS auction if they are already the
          // leader
          java.math.BigDecimal currentLockedForThis = java.math.BigDecimal.ZERO;
          if (auction.getHighestBidderId() != null && auction.getHighestBidderId() == userId) {
            currentLockedForThis = auction.getHighestMaxBid();
          }

          java.math.BigDecimal available =
              user.balance().subtract(user.lockedBalance()).add(currentLockedForThis);
          if (request.active() && available.compareTo(request.maxBid()) < 0) {
            throw new InsufficientFundsException(
                "Insufficient balance to set auto-bid limit of " + request.maxBid());
          }

          com.auction.common.model.AutoBidRule rule =
              new com.auction.common.model.AutoBidRule(
                  0L,
                  request.auctionId(),
                  userId,
                  request.maxBid(),
                  request.increment(),
                  request.active(),
                  LocalDateTime.now());
          autoBidDao.createOrUpdate(rule);
          logger.info(
              "Auto-bid limit set to {} for User {} on Auction {}",
              request.maxBid(),
              userId,
              request.auctionId());
          return null;
        });

    // Trigger check immediately when a rule becomes active.
    if (request.active()) {
      triggerAutoBids(request.auctionId(), userId);
    }
  }

  /**
   * Triggers the auto-bidding system for a specific auction. Iterates through all active auto-bid
   * rules and places bids if necessary.
   *
   * @param auctionId The ID of the auction.
   * @param lastBidderId The ID of the user who just placed a manual bid (to avoid auto-bidding
   *     against themselves).
   */
  private void triggerAutoBids(long auctionId, long lastBidderId) {
    com.auction.common.dto.bid.BidUpdateEvent event =
        lockManager.executeLocked(
            auctionId,
            () -> {
              Auction auction = auctionDao.findById(auctionId).orElse(null);
              if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) return null;

              java.util.List<com.auction.common.model.AutoBidRule> rules =
                  autoBidDao.findActiveByAuction(auctionId);
              if (rules == null || rules.isEmpty()) {
                return null;
              }
              com.auction.common.model.AutoBidRule winningRule = findWinningAutoBidRule(rules);
              if (winningRule == null) {
                return null;
              }

              if (auction.getHighestBidderId() != null
                  && auction.getHighestBidderId() == winningRule.getBidderId()) {
                return null;
              }

              java.math.BigDecimal nextBid = calculateAutoBidPrice(auction, rules, winningRule);
              if (!winningRule.canBidUpTo(nextBid)
                  || nextBid.compareTo(auction.getCurrentPrice()) <= 0) {
                return null;
              }

              try {
                logger.info(
                    "Triggering Auto-bid response for User {} on Auction {}",
                    winningRule.getBidderId(),
                    auctionId);
                return placeAutoStep(
                    winningRule.getBidderId(), auctionId, nextBid, winningRule.getMaxBid());
              } catch (RuntimeException e) {
                logger.warn(
                    "Auto-bid rule failed for User {} on Auction {}. Removing the rule. Reason: {}",
                    winningRule.getBidderId(),
                    auctionId,
                    e.getMessage());
                autoBidDao.delete(auctionId, winningRule.getBidderId());
                return null;
              }
            });

    if (event != null) {
      notificationService.broadcast(
          auctionId, com.auction.common.protocol.MessageType.BID_UPDATE, event);
    }
  }

  private com.auction.common.model.AutoBidRule findWinningAutoBidRule(
      java.util.List<com.auction.common.model.AutoBidRule> rules) {
    com.auction.common.model.AutoBidRule winningRule = null;
    for (com.auction.common.model.AutoBidRule rule : rules) {
      if (winningRule == null || rule.getMaxBid().compareTo(winningRule.getMaxBid()) > 0) {
        winningRule = rule;
      }
    }
    return winningRule;
  }

  private java.math.BigDecimal calculateAutoBidPrice(
      Auction auction,
      java.util.List<com.auction.common.model.AutoBidRule> rules,
      com.auction.common.model.AutoBidRule winningRule) {
    java.math.BigDecimal secondBestMax = java.math.BigDecimal.ZERO;
    for (com.auction.common.model.AutoBidRule rule : rules) {
      if (rule.getBidderId() != winningRule.getBidderId()
          && rule.getMaxBid().compareTo(secondBestMax) > 0) {
        secondBestMax = rule.getMaxBid();
      }
    }

    java.math.BigDecimal referencePrice = auction.getCurrentPrice().max(secondBestMax);
    java.math.BigDecimal nextBid = referencePrice.add(winningRule.getIncrement());
    if (nextBid.compareTo(winningRule.getMaxBid()) > 0) {
      return winningRule.getMaxBid();
    }
    return nextBid;
  }

  /**
   * Internal step for auto-bidding. Updates the auction price to the next increment and locks the
   * full max bid for the proxy leader.
   *
   * @param bidderId The ID of the auto-bidder.
   * @param auctionId The ID of the auction.
   * @param nextPrice The next price point to jump to.
   * @param userMaxBid The maximum budget defined in the auto-bid rule.
   */
  private com.auction.common.dto.bid.BidUpdateEvent placeAutoStep(
      long bidderId,
      long auctionId,
      java.math.BigDecimal nextPrice,
      java.math.BigDecimal userMaxBid) {
    return Database.getInstance()
        .runInTransaction(
            () -> {
              LocalDateTime now = LocalDateTime.now();
              Auction auction = auctionDao.findById(auctionId).get();

              Long previousLeaderId = auction.getHighestBidderId();
              java.math.BigDecimal previousMax = auction.getHighestMaxBid();

              java.math.BigDecimal amountToLock = userMaxBid;
              if (previousLeaderId != null && previousLeaderId == bidderId) {
                amountToLock = userMaxBid.subtract(previousMax);
              }

              if (amountToLock.compareTo(java.math.BigDecimal.ZERO) > 0) {
                walletService.lockFunds(bidderId, amountToLock);
              } else if (amountToLock.compareTo(java.math.BigDecimal.ZERO) < 0) {
                walletService.releaseFunds(bidderId, amountToLock.abs());
              }

              if (previousLeaderId != null && previousLeaderId != bidderId) {
                walletService.releaseFunds(previousLeaderId, previousMax);
              }

              auction.setCurrentPrice(nextPrice);
              auction.setHighestMaxBid(userMaxBid);
              auction.setHighestBidderId(bidderId);

              if (now.isAfter(auction.getEndTime().minusSeconds(ANTI_SNIPING_WINDOW_SECONDS))) {
                auction.setEndTime(auction.getEndTime().plusSeconds(EXTENSION_DURATION_SECONDS));
                logger.info("Auction {} extended by 60s", auctionId);
              }

              auctionDao.update(auction);
              bidDao.create(new BidTransaction(0L, auctionId, bidderId, nextPrice, now));

              return new com.auction.common.dto.bid.BidUpdateEvent(
                  auctionId,
                  userDao.findById(bidderId).map(u -> u.username()).orElse("Auto"),
                  auction.getCurrentPrice(),
                  now,
                  auction.getEndTime());
            });
  }

  /**
   * Retrieves the current auto-bid rule for a user on a specific auction.
   *
   * @param userId The ID of the user.
   * @param auctionId The ID of the auction.
   * @return An Optional containing the auto-bid details if found.
   */
  public java.util.Optional<com.auction.common.dto.bid.AutoBidDto> getAutoBid(
      long userId, long auctionId) {
    return autoBidDao
        .findByAuctionAndBidder(auctionId, userId)
        .map(
            rule ->
                new com.auction.common.dto.bid.AutoBidDto(
                    rule.getAuctionId(), rule.getMaxBid(), rule.getIncrement(), rule.isActive()));
  }

  /**
   * Retrieves the bid history for a specific auction.
   *
   * @param auctionId The ID of the auction.
   * @return List of bid responses.
   */
  public java.util.List<PlaceBidResponse> getBidHistory(long auctionId) {
    return bidDao.findByAuctionId(auctionId).stream()
        .map(
            transaction -> {
              String username =
                  userDao
                      .findById(transaction.getBidderId())
                      .map(com.auction.server.dao.UserDao.UserRecord::username)
                      .orElse("Unknown");

              return new PlaceBidResponse(
                  transaction.getAuctionId(),
                  transaction.getAmount(),
                  username,
                  transaction.getCreatedAt());
            })
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Retrieves all auction IDs where the user has placed at least one bid.
   *
   * @param userId The ID of the user.
   * @return List of unique auction IDs.
   */
  public java.util.List<Long> getMyBids(long userId) {
    return bidDao.findByBidderId(userId).stream()
        .map(BidTransaction::getAuctionId)
        .distinct()
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Gets a detailed bid history for a specific user, including the results (WON/WINNING/OUTBID).
   *
   * @param userId The ID of the user.
   * @return A list of bid history DTOs.
   */
  public java.util.List<com.auction.common.dto.bid.BidHistoryDto> getUserBidHistory(long userId) {
    return bidDao.findByBidderId(userId).stream()
        .map(
            bid -> {
              com.auction.common.model.Auction auction =
                  auctionDao.findById(bid.getAuctionId()).orElse(null);
              String title = "Unknown Auction";
              String result = "OUTBID";

              if (auction != null) {
                title = "Auction #" + auction.getId();

                if (auction.getStatus() == com.auction.common.enums.AuctionStatus.PAID) {
                  result =
                      userId
                              == (auction.getHighestBidderId() != null
                                  ? auction.getHighestBidderId()
                                  : -1)
                          ? "WON"
                          : "LOST";
                } else if (auction.getStatus() == com.auction.common.enums.AuctionStatus.FINISHED) {
                  result =
                      userId
                              == (auction.getHighestBidderId() != null
                                  ? auction.getHighestBidderId()
                                  : -1)
                          ? "WON_PENDING_PAYMENT"
                          : "LOST";
                } else if (auction.getStatus()
                    == com.auction.common.enums.AuctionStatus.CANCELED) {
                  result = "CANCELED";
                } else {
                  result =
                      userId
                              == (auction.getHighestBidderId() != null
                                  ? auction.getHighestBidderId()
                                  : -1)
                          ? "WINNING"
                          : "OUTBID";
                }
              }

              return new com.auction.common.dto.bid.BidHistoryDto(
                  bid.getId(),
                  bid.getAuctionId(),
                  title,
                  bid.getAmount(),
                  bid.getCreatedAt(),
                  result);
            })
        .collect(java.util.stream.Collectors.toList());
  }
}
