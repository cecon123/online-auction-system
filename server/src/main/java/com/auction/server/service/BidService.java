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
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling bidding business logic.
 * Integrates locking for thread-safety and real database persistence.
 */
public class BidService {

    private static final Logger logger = LoggerFactory.getLogger(
        BidService.class
    );

    private final AuctionLockManager lockManager;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final AutoBidDao autoBidDao;
    private final com.auction.server.dao.UserDao userDao;
    private final NotificationService notificationService;

    public BidService(
        AuctionDao auctionDao,
        BidDao bidDao,
        com.auction.server.dao.UserDao userDao,
        AutoBidDao autoBidDao
    ) {
        this.lockManager = AuctionLockManager.getInstance();
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.userDao = userDao;
        this.autoBidDao = autoBidDao;
        this.notificationService = NotificationService.getInstance();
    }

    /**
     * Processes a bid request.
     *
     * @param bidderId The ID of the bidder.
     * @param request  The bid details.
     * @return PlaceBidResponse on success.
     */
    public PlaceBidResponse placeBid(long bidderId, PlaceBidRequest request) {
        PlaceBidResponse response = lockManager.executeLocked(request.auctionId(), () -> {
            // 1. Fetch bidder and check balance
            com.auction.server.dao.UserDao.UserRecord bidder = userDao
                .findById(bidderId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Bidder not found.")
                );

            if (bidder.balance().compareTo(request.amount()) < 0) {
                throw new IllegalArgumentException(
                    "Insufficient balance to place this bid."
                );
            }

            // 2. Fetch auction from DB
            Auction auction = auctionDao
                .findById(request.auctionId())
                .orElseThrow(() ->
                    new IllegalArgumentException("Auction not found.")
                );

            // 2.1 Prevent seller from bidding on their own auction
            if (auction.getSellerId() == bidderId) {
                throw new IllegalArgumentException(
                    "You cannot bid on your own auction."
                );
            }

            // 3. Validate auction status
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new IllegalArgumentException(
                    "Auction is not currently running. Current status: " +
                    auction.getStatus()
                );
            }

            // 4. Validate end time
            if (LocalDateTime.now().isAfter(auction.getEndTime())) {
                throw new IllegalArgumentException(
                    "Auction has already ended."
                );
            }

            // 5. Validate bid amount
            if (request.amount().compareTo(auction.getCurrentPrice()) <= 0) {
                throw new IllegalArgumentException(
                    "Bid amount must be higher than the current price."
                );
            }

            // 6. Update auction state
            auction.updateHighestBid(bidderId, request.amount());
            
            // Time Extension Logic: If bid is placed within the last 1 minute, extend by 2 minutes
            boolean timeExtended = false;
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(auction.getEndTime().minusMinutes(1))) {
                auction.setEndTime(auction.getEndTime().plusMinutes(2));
                timeExtended = true;
                logger.info("Auction {} extended to {} due to last-minute bid", 
                    auction.getId(), auction.getEndTime());
            }

            auctionDao.update(auction);

            // 7. Record bid transaction
            BidTransaction transaction = new BidTransaction(
                0L, // ID will be generated
                auction.getId(),
                bidderId,
                request.amount(),
                now
            );
            bidDao.create(transaction);

            logger.info(
                "Bid accepted: Auction {} now at {} by User {} ({})",
                auction.getId(),
                request.amount(),
                bidderId,
                bidder.username()
            );

            // 8. Notify subscribed clients
            notificationService.broadcast(
                auction.getId(),
                com.auction.common.protocol.MessageType.BID_UPDATE,
                new com.auction.common.dto.bid.BidUpdateEvent(
                    auction.getId(),
                    bidder.username(),
                    request.amount(),
                    transaction.getCreatedAt(),
                    auction.getEndTime()
                )
            );

            if (timeExtended) {
                notificationService.broadcast(
                    auction.getId(),
                    com.auction.common.protocol.MessageType.TIME_EXTENDED,
                    new com.auction.common.dto.auction.AuctionEventDto(
                        auction.getId(),
                        auction.getStatus(),
                        "Auction extended due to last-minute bid!",
                        null,
                        null,
                        auction.getEndTime()
                    )
                );
            }

            return new PlaceBidResponse(
                auction.getId(),
                request.amount(),
                bidder.username(),
                transaction.getCreatedAt()
            );
        });

        // Trigger auto-bids for other users
        triggerAutoBids(request.auctionId(), bidderId);

        return response;
    }

    private void triggerAutoBids(long auctionId, long lastBidderId) {
        java.util.List<com.auction.common.model.AutoBidRule> rules = autoBidDao.findActiveByAuction(auctionId);

        for (com.auction.common.model.AutoBidRule rule : rules) {
            if (rule.getBidderId() == lastBidderId) continue;

            // Use lock to ensure we have the latest auction price
            lockManager.executeLocked(auctionId, () -> {
                Auction auction = auctionDao.findById(auctionId).orElse(null);
                if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) return null;

                java.math.BigDecimal nextBid = auction.getCurrentPrice().add(rule.getIncrement());
                if (rule.canBidUpTo(nextBid)) {
                    try {
                        logger.info("Server-side Auto-bid triggered for User {} on Auction {}", rule.getBidderId(), auctionId);
                        // Recursively call placeBid. This will trigger the next auto-bid if needed.
                        // We do this outside the current lock in placeBid to avoid nested locks if possible, 
                        // but placeBid also uses executeLocked. ReentrantLock handles this.
                        this.placeBid(rule.getBidderId(), new PlaceBidRequest(auctionId, nextBid));
                    } catch (Exception e) {
                        logger.warn("Auto-bid failed for User {}: {}", rule.getBidderId(), e.getMessage());
                    }
                }
                return null;
            });
        }
    }

    /**
     * Retrieves the bid history for a specific auction.
     *
     * @param auctionId The ID of the auction.
     * @return List of bid responses.
     */
    public java.util.List<PlaceBidResponse> getBidHistory(long auctionId) {
        return bidDao.findByAuctionId(auctionId).stream()
            .map(transaction -> {
                String username = userDao.findById(transaction.getBidderId())
                    .map(com.auction.server.dao.UserDao.UserRecord::username)
                    .orElse("Unknown");

                return new PlaceBidResponse(
                    transaction.getAuctionId(),
                    transaction.getAmount(),
                    username,
                    transaction.getCreatedAt()
                );
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
     * Gets detailed bid history for a user.
     */
    public java.util.List<com.auction.common.dto.bid.BidHistoryDto> getUserBidHistory(long userId) {
        return bidDao.findByBidderId(userId).stream()
            .map(bid -> {
                com.auction.common.model.Auction auction = auctionDao.findById(bid.getAuctionId()).orElse(null);
                String title = "Unknown Auction";
                String result = "OUTBID";
                
                if (auction != null) {
                    title = "Auction #" + auction.getId();
                    
                    if (auction.getStatus() == com.auction.common.enums.AuctionStatus.FINISHED) {
                        result = userId == (auction.getHighestBidderId() != null ? auction.getHighestBidderId() : -1) ? "WON" : "LOST";
                    } else {
                        result = userId == (auction.getHighestBidderId() != null ? auction.getHighestBidderId() : -1) ? "WINNING" : "OUTBID";
                    }
                }
                
                return new com.auction.common.dto.bid.BidHistoryDto(
                    bid.getId(),
                    bid.getAuctionId(),
                    title,
                    bid.getAmount(),
                    bid.getCreatedAt(),
                    result
                );
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Sets or updates an auto-bid rule for a user.
     */
    public void setAutoBid(long userId, com.auction.common.dto.bid.SetAutoBidRequest request) {
        com.auction.common.model.AutoBidRule rule = new com.auction.common.model.AutoBidRule(
            0L, // ID ignored for createOrUpdate
            request.auctionId(),
            userId,
            request.maxBid(),
            request.increment(),
            request.active(),
            LocalDateTime.now()
        );
        autoBidDao.createOrUpdate(rule);
        logger.info("Auto-bid rule set for User {} on Auction {}", userId, request.auctionId());

        // Check if we should trigger it immediately
        if (request.active()) {
            triggerAutoBids(request.auctionId(), 0L); // 0L as lastBidderId to check everyone
        }
    }

    /**
     * Gets the current auto-bid rule for a user and auction.
     */
    public java.util.Optional<com.auction.common.dto.bid.AutoBidDto> getAutoBid(long userId, long auctionId) {
        return autoBidDao.findByAuctionAndBidder(auctionId, userId)
            .map(rule -> new com.auction.common.dto.bid.AutoBidDto(
                rule.getAuctionId(),
                rule.getMaxBid(),
                rule.getIncrement(),
                rule.isActive()
            ));
    }
}
