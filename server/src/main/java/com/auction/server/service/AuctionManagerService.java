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
 * Background service that periodically checks and updates auction statuses
 * based on their start and end times.
 */
public class AuctionManagerService {

    private static final Logger logger = LoggerFactory.getLogger(
        AuctionManagerService.class
    );

    private final AuctionDao auctionDao;
    private final com.auction.server.dao.UserDao userDao;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public AuctionManagerService(AuctionDao auctionDao, com.auction.server.dao.UserDao userDao) {
        this.auctionDao = auctionDao;
        this.userDao = userDao;
        this.notificationService = NotificationService.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "AuctionManager-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Starts the periodic status check.
     */
    public void start() {
        logger.info("Starting Auction Manager Service...");
        // Check every 5 seconds for more responsiveness
        scheduler.scheduleAtFixedRate(this::checkStatuses, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Stops the background service.
     */
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
            newStatus = AuctionStatus.FINISHED;
        }

        if (newStatus != currentStatus) {
            try {
                auction.setStatus(newStatus);
                auctionDao.update(auction);
                logger.info("Auction {} status changed from {} to {}", 
                    auction.getId(), currentStatus, newStatus);

                // Broadcast status change
                String winner = null;
                if (newStatus == AuctionStatus.FINISHED && auction.getHighestBidderId() != null) {
                    winner = userDao.findById(auction.getHighestBidderId())
                        .map(com.auction.server.dao.UserDao.UserRecord::username)
                        .orElse("Unknown");
                }

                notificationService.broadcast(
                    auction.getId(),
                    newStatus == AuctionStatus.FINISHED ? 
                        com.auction.common.protocol.MessageType.AUCTION_CLOSED : 
                        com.auction.common.protocol.MessageType.BID_UPDATE, // Reuse for RUNNING status change
                    new com.auction.common.dto.auction.AuctionEventDto(
                        auction.getId(),
                        newStatus,
                        "Auction status changed to " + newStatus,
                        winner,
                        auction.getCurrentPrice(),
                        auction.getEndTime()
                    )
                );
            } catch (IllegalStateException e) {
                // Optimistic locking failure - someone else might have updated it
                logger.warn("Optimistic locking failure while updating status for Auction {}", auction.getId());
            } catch (Exception e) {
                logger.error("Failed to update status for Auction {}", auction.getId(), e);
            }
        }
    }
}
