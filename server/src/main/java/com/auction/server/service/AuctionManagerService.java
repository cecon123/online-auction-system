package com.auction.server.service;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.server.dao.AuctionDao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background service that periodically checks and updates auction statuses based on their start and
 * end times.
 */
public class AuctionManagerService {

  private static final Logger logger = LoggerFactory.getLogger(AuctionManagerService.class);

  private final AuctionDao auctionDao;
  private final com.auction.server.dao.UserDao userDao;
  private final WalletService walletService;
  private final NotificationService notificationService;
  private final ScheduledExecutorService scheduler;

  public AuctionManagerService(
      AuctionDao auctionDao, com.auction.server.dao.UserDao userDao, WalletService walletService) {
    this.auctionDao = auctionDao;
    this.userDao = userDao;
    this.walletService = walletService;
    this.notificationService = NotificationService.getInstance();
    this.scheduler =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "AuctionManager-Thread");
              thread.setDaemon(true);
              return thread;
            });
  }

  /** Starts the periodic status check. */
  public void start() {
    logger.info("Starting Auction Manager Service...");
    // Check every 5 seconds for more responsiveness
    scheduler.scheduleAtFixedRate(this::checkStatuses, 0, 5, TimeUnit.SECONDS);
  }

  /** Stops the background service. */
  public void stop() {
    logger.info("Stopping Auction Manager Service...");
    scheduler.shutdown();
  }

  private void checkStatuses() {
    try {
      LocalDateTime now = LocalDateTime.now();
      List<Auction> allAuctions = auctionDao.findAll();

      for (Auction auction : allAuctions) {
        updateStatusIfNecessary(auction, now);
      }
    } catch (Exception e) {
      logger.error("Error during background status check", e);
    }
  }

  private void updateStatusIfNecessary(Auction auction, LocalDateTime now) {
    AuctionStatus currentStatus = auction.getStatus();
    AuctionStatus newStatus = currentStatus;

    if (currentStatus == AuctionStatus.OPEN && !now.isBefore(auction.getStartTime())) {
      newStatus = AuctionStatus.RUNNING;
    } else if (currentStatus == AuctionStatus.RUNNING && now.isAfter(auction.getEndTime())) {
      // Check reserve price when finished
      if (auction.getHighestBidderId() != null) {
        // Determine if reserve met (if null reserve, it's always met)
        boolean reserveMet =
            auction.getReservePrice() == null
                || auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;

        if (reserveMet) {
          newStatus = AuctionStatus.FINISHED;
        } else {
          newStatus = AuctionStatus.CANCELED;
        }
      } else {
        // No bids at all
        newStatus = AuctionStatus.CANCELED;
      }
    }

    if (newStatus != currentStatus) {
      try {
        logger.info(
            "Transitioning Auction {} from {} to {}.", auction.getId(), currentStatus, newStatus);

        // ESCROW SETTLEMENT LOGIC
        if (newStatus == AuctionStatus.FINISHED) {
          Long winnerId = auction.getHighestBidderId();
          java.math.BigDecimal finalPrice = auction.getCurrentPrice();
          java.math.BigDecimal maxLocked = auction.getHighestMaxBid();

          logger.info(
              "Settling Auction {}: Winner={}, Price={}, MaxLocked={}",
              auction.getId(),
              winnerId,
              finalPrice,
              maxLocked);

          // Success: Transfer funds from winner to seller
          walletService.settleAuction(winnerId, auction.getSellerId(), finalPrice);

          // Release the leftover max bid if any (Model 1: Full Max Lock)
          java.math.BigDecimal leftover = maxLocked.subtract(finalPrice);
          if (leftover.compareTo(java.math.BigDecimal.ZERO) > 0) {
            logger.info("Releasing leftover Proxy funds: {}", leftover);
            walletService.releaseFunds(winnerId, leftover);
          }
        } else if (newStatus == AuctionStatus.CANCELED && auction.getHighestBidderId() != null) {
          // Failure (reserve not met): Refund highest bidder their full locked max bid
          logger.info(
              "Auction {} canceled. Refunding {} to User {}",
              auction.getId(),
              auction.getHighestMaxBid(),
              auction.getHighestBidderId());
          walletService.releaseFunds(auction.getHighestBidderId(), auction.getHighestMaxBid());
        }

        auction.setStatus(newStatus);
        auctionDao.update(auction);

        // Broadcast status change
        String winnerUsername = null;
        if (auction.getHighestBidderId() != null) {
          winnerUsername =
              userDao
                  .findById(auction.getHighestBidderId())
                  .map(com.auction.server.dao.UserDao.UserRecord::username)
                  .orElse("Unknown");
        }

        if (newStatus == AuctionStatus.FINISHED || newStatus == AuctionStatus.CANCELED) {
          String message =
              "Auction "
                  + (newStatus == AuctionStatus.FINISHED
                      ? "sold successfully!"
                      : "ended without sale.");
          notificationService.broadcast(
              auction.getId(),
              com.auction.common.protocol.MessageType.AUCTION_CLOSED,
              new com.auction.common.dto.auction.AuctionEventDto(
                  auction.getId(),
                  newStatus,
                  message,
                  winnerUsername,
                  auction.getCurrentPrice(),
                  auction.getEndTime()));
        }

        // --- Global Seller & Winner Notification ---
        if (newStatus == AuctionStatus.FINISHED) {
          com.auction.common.dto.notification.SystemNotificationDto successNotice =
              new com.auction.common.dto.notification.SystemNotificationDto(
                  "Auction Sold! \uD83D\uDCB0", // 💰
                  "Congratulations! Your auction #"
                      + auction.getId()
                      + " was won by "
                      + winnerUsername
                      + " for $"
                      + auction.getCurrentPrice()
                      + ".",
                  "SUCCESS",
                  now);
          notificationService.notifyUser(
              auction.getSellerId(),
              com.auction.common.protocol.MessageType.SYSTEM_NOTIFICATION,
              successNotice);

          // Notify Winner as well
          if (auction.getHighestBidderId() != null) {
            com.auction.common.dto.notification.SystemNotificationDto winNotice =
                new com.auction.common.dto.notification.SystemNotificationDto(
                    "Auction Won! \uD83C\uDF89", // 🎉
                    "Congratulations! You won auction #"
                        + auction.getId()
                        + " for $"
                        + auction.getCurrentPrice()
                        + ".",
                    "SUCCESS",
                    now);
            notificationService.notifyUser(
                auction.getHighestBidderId(),
                com.auction.common.protocol.MessageType.SYSTEM_NOTIFICATION,
                winNotice);
          }
        } else if (newStatus == AuctionStatus.CANCELED) {
          String reason =
              (auction.getHighestBidderId() != null)
                  ? "reserve price not met."
                  : "no bids were placed.";
          com.auction.common.dto.notification.SystemNotificationDto failNotice =
              new com.auction.common.dto.notification.SystemNotificationDto(
                  "Auction Ended \uD83D\uDE14", // 😔
                  "Your auction #" + auction.getId() + " ended because " + reason,
                  "WARNING",
                  now);
          notificationService.notifyUser(
              auction.getSellerId(),
              com.auction.common.protocol.MessageType.SYSTEM_NOTIFICATION,
              failNotice);
        }

        // Global broadcast for list update
        notificationService.broadcastToAllUsers(
            com.auction.common.protocol.MessageType.AUCTION_LIST_UPDATED, null);
      } catch (IllegalStateException e) {
        logger.warn("Status update failed for Auction {}: {}", auction.getId(), e.getMessage());
      } catch (Exception e) {
        logger.error("Error updating status for Auction {}", auction.getId(), e);
      }
    }
  }
}
