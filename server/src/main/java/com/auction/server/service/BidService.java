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

    private static final java.math.BigDecimal MIN_INCREMENT = new java.math.BigDecimal("10.00");
    private static final int ANTI_SNIPING_WINDOW_SECONDS = 30;
    private static final int EXTENSION_DURATION_SECONDS = 60;

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
        WalletService walletService
    ) {
        this.lockManager = AuctionLockManager.getInstance();
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.userDao = userDao;
        this.autoBidDao = autoBidDao;
        this.walletService = walletService;
        this.notificationService = NotificationService.getInstance();
    }

    /**
     * Processes a manual bid request (Traditional Bidding).
     * The price jumps exactly to the requested amount.
     *
     * @param bidderId The ID of the bidder.
     * @param request  The bid details.
     * @return PlaceBidResponse on success.
     */
    public PlaceBidResponse placeBid(long bidderId, PlaceBidRequest request) {
        PlaceBidResponse response = lockManager.executeLocked(request.auctionId(), () -> {
            LocalDateTime now = LocalDateTime.now();
            java.math.BigDecimal bidAmount = request.amount();

            // 1. Fetch auction and bidder
            Auction auction = auctionDao.findById(request.auctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found."));
            
            com.auction.server.dao.UserDao.UserRecord bidder = userDao.findById(bidderId)
                .orElseThrow(() -> new IllegalArgumentException("Bidder not found."));

            // 2. Preliminary Validations
            if (auction.getSellerId() == bidderId) {
                throw new IllegalArgumentException("You cannot bid on your own auction.");
            }
            if (auction.getStatus() != AuctionStatus.RUNNING || now.isAfter(auction.getEndTime())) {
                throw new IllegalArgumentException("Auction is not active.");
            }

            // 3. Traditional Bidding Validation (Must be higher than current + increment)
            java.math.BigDecimal minRequired = auction.getCurrentPrice();
            if (auction.getHighestBidderId() != null) {
                minRequired = minRequired.add(MIN_INCREMENT);
            }

            if (bidAmount.compareTo(minRequired) < 0) {
                throw new IllegalArgumentException("Your bid must be at least " + minRequired);
            }

            // 4. Update Auction State (Traditional: Price = bidAmount)
            Long previousLeaderId = auction.getHighestBidderId();
            java.math.BigDecimal previousMax = auction.getHighestMaxBid(); // This was the old "Proxy" limit

            // 5. ESCROW: Release old leader's funds (Full Max Lock model)
            if (previousLeaderId != null && previousLeaderId != bidderId) {
                walletService.releaseFunds(previousLeaderId, previousMax);
            }

            // 6. Lock new bidder's funds
            java.math.BigDecimal amountToLock = bidAmount;
            if (previousLeaderId != null && previousLeaderId == bidderId) {
                // Same bidder increasing their bid: only lock the difference
                amountToLock = bidAmount.subtract(previousMax);
            }

            java.math.BigDecimal available = bidder.balance().subtract(bidder.lockedBalance());
            if (available.compareTo(amountToLock) < 0) {
                throw new IllegalArgumentException("Insufficient balance. Need additional: " + amountToLock);
            }
            walletService.lockFunds(bidderId, amountToLock);

            // 7. Update Auction
            auction.setCurrentPrice(bidAmount);
            auction.setHighestMaxBid(bidAmount); // In traditional bid, Max = Price
            auction.setHighestBidderId(bidderId);

            // 8. Anti-Sniping Extension
            boolean timeExtended = false;
            if (now.isAfter(auction.getEndTime().minusSeconds(ANTI_SNIPING_WINDOW_SECONDS))) {
                auction.setEndTime(auction.getEndTime().plusSeconds(EXTENSION_DURATION_SECONDS));
                timeExtended = true;
                logger.info("Auction {} extended by {}s", auction.getId(), EXTENSION_DURATION_SECONDS);
            }

            auctionDao.update(auction);

            // 9. Record Transaction
            BidTransaction transaction = new BidTransaction(0L, auction.getId(), bidderId, bidAmount, now);
            bidDao.create(transaction);

            // 10. Notify
            notificationService.broadcast(auction.getId(), com.auction.common.protocol.MessageType.BID_UPDATE,
                new com.auction.common.dto.bid.BidUpdateEvent(
                    auction.getId(), bidder.username(), auction.getCurrentPrice(), now, auction.getEndTime()
                )
            );

            if (timeExtended) {
                notificationService.broadcast(auction.getId(), com.auction.common.protocol.MessageType.TIME_EXTENDED,
                    new com.auction.common.dto.auction.AuctionEventDto(
                        auction.getId(), auction.getStatus(), "Time extended!", null, null, auction.getEndTime()
                    )
                );
            }

            return new PlaceBidResponse(auction.getId(), auction.getCurrentPrice(), bidder.username(), now);
        });

        // 11. AFTER Manual Bid: Trigger Auto-bids for others to respond!
        triggerAutoBids(request.auctionId(), bidderId);

        return response;
    }

    /**
     * Sets or updates an auto-bid rule (Proxy Bidding limit).
     */
    public void setAutoBid(long userId, com.auction.common.dto.bid.SetAutoBidRequest request) {
        lockManager.executeLocked(request.auctionId(), () -> {
            com.auction.server.dao.UserDao.UserRecord user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

            // Validation: Max bid must be higher than current price + increment
            Auction auction = auctionDao.findById(request.auctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found."));
            
            java.math.BigDecimal minRequired = auction.getCurrentPrice().add(MIN_INCREMENT);
            if (request.maxBid().compareTo(minRequired) < 0) {
                throw new IllegalArgumentException("Max bid must be at least " + minRequired);
            }

            // Wallet check: Must be able to lock the full max bid
            java.math.BigDecimal available = user.balance().subtract(user.lockedBalance());
            if (available.compareTo(request.maxBid()) < 0) {
                 throw new IllegalArgumentException("Insufficient balance to set auto-bid limit of " + request.maxBid());
            }

            com.auction.common.model.AutoBidRule rule = new com.auction.common.model.AutoBidRule(
                0L, request.auctionId(), userId, request.maxBid(), MIN_INCREMENT, request.active(), LocalDateTime.now()
            );
            autoBidDao.createOrUpdate(rule);
            logger.info("Auto-bid limit set to {} for User {} on Auction {}", request.maxBid(), userId, request.auctionId());
            return null;
        });

        // Trigger check immediately
        triggerAutoBids(request.auctionId(), 0L);
    }

    private void triggerAutoBids(long auctionId, long lastBidderId) {
        // Find all active auto-bid rules for this auction
        java.util.List<com.auction.common.model.AutoBidRule> rules = autoBidDao.findActiveByAuction(auctionId);

        for (com.auction.common.model.AutoBidRule rule : rules) {
            if (rule.getBidderId() == lastBidderId) continue;

            lockManager.executeLocked(auctionId, () -> {
                Auction auction = auctionDao.findById(auctionId).orElse(null);
                if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) return null;

                // If user is already the leader, do nothing
                if (auction.getHighestBidderId() != null && auction.getHighestBidderId() == rule.getBidderId()) {
                    return null;
                }

                // Calculate next bid: Current Price + Increment
                java.math.BigDecimal nextBid = auction.getCurrentPrice().add(MIN_INCREMENT);

                // Can this auto-bid rule cover it?
                if (rule.canBidUpTo(nextBid)) {
                    logger.info("Triggering Auto-bid response for User {} on Auction {}", rule.getBidderId(), auctionId);
                    
                    // We call a internal method to place the "auto" jump
                    placeAutoStep(rule.getBidderId(), auctionId, nextBid, rule.getMaxBid());
                }
                return null;
            });
        }
    }

    /**
     * Internal step for auto-bidding. Similar to placeBid but uses proxy logic
     * (only jumps to next increment, but locks full max).
     */
    private void placeAutoStep(long bidderId, long auctionId, java.math.BigDecimal nextPrice, java.math.BigDecimal userMaxBid) {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionDao.findById(auctionId).get();
        
        Long previousLeaderId = auction.getHighestBidderId();
        java.math.BigDecimal previousMax = auction.getHighestMaxBid();

        // Escrow management
        if (previousLeaderId != null && previousLeaderId != bidderId) {
            walletService.releaseFunds(previousLeaderId, previousMax);
        }

        // Lock new leader's FULL max bid (Model 1)
        walletService.lockFunds(bidderId, userMaxBid);

        // Update Auction
        auction.setCurrentPrice(nextPrice);
        auction.setHighestMaxBid(userMaxBid);
        auction.setHighestBidderId(bidderId);
        
        // Anti-sniping check
        if (now.isAfter(auction.getEndTime().minusSeconds(ANTI_SNIPING_WINDOW_SECONDS))) {
            auction.setEndTime(auction.getEndTime().plusSeconds(EXTENSION_DURATION_SECONDS));
        }

        auctionDao.update(auction);
        bidDao.create(new BidTransaction(0L, auctionId, bidderId, nextPrice, now));

        notificationService.broadcast(auctionId, com.auction.common.protocol.MessageType.BID_UPDATE,
            new com.auction.common.dto.bid.BidUpdateEvent(
                auctionId, 
                userDao.findById(bidderId).map(u -> u.username()).orElse("Auto"), 
                auction.getCurrentPrice(), 
                now, 
                auction.getEndTime()
            )
        );
    }

    public java.util.Optional<com.auction.common.dto.bid.AutoBidDto> getAutoBid(long userId, long auctionId) {
        return autoBidDao.findByAuctionAndBidder(auctionId, userId)
            .map(rule -> new com.auction.common.dto.bid.AutoBidDto(
                rule.getAuctionId(),
                rule.getMaxBid(),
                rule.getIncrement(),
                rule.isActive()
            ));
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
}
