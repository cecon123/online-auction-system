package com.auction.server.service;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import com.auction.server.concurrency.AuctionLockManager;
import com.auction.server.dao.AuctionDao;
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

    public BidService(AuctionDao auctionDao, BidDao bidDao) {
        this.lockManager = AuctionLockManager.getInstance();
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
    }

    /**
     * Processes a bid request.
     *
     * @param bidderId The ID of the bidder.
     * @param request  The bid details.
     * @return PlaceBidResponse on success.
     */
    public PlaceBidResponse placeBid(long bidderId, PlaceBidRequest request) {
        return lockManager.executeLocked(request.auctionId(), () -> {
            // 1. Fetch auction from DB
            Auction auction = auctionDao
                .findById(request.auctionId())
                .orElseThrow(() ->
                    new IllegalArgumentException("Auction not found.")
                );

            // 2. Validate auction status
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new IllegalArgumentException(
                    "Auction is not currently running."
                );
            }

            // 3. Validate end time
            if (LocalDateTime.now().isAfter(auction.getEndTime())) {
                throw new IllegalArgumentException(
                    "Auction has already ended."
                );
            }

            // 4. Validate bid amount
            if (request.amount().compareTo(auction.getCurrentPrice()) <= 0) {
                throw new IllegalArgumentException(
                    "Bid amount must be higher than the current price."
                );
            }

            // 5. Update auction state
            auction.updateHighestBid(bidderId, request.amount());
            auctionDao.update(auction);

            // 6. Record bid transaction
            BidTransaction transaction = new BidTransaction(
                0L, // ID will be generated
                auction.getId(),
                bidderId,
                request.amount(),
                LocalDateTime.now()
            );
            bidDao.create(transaction);

            logger.info(
                "Bid accepted: Auction {} now at {} by User {}",
                auction.getId(),
                request.amount(),
                bidderId
            );

            return new PlaceBidResponse(
                auction.getId(),
                request.amount(),
                "user-" + bidderId, // Later: Fetch actual username
                transaction.getCreatedAt()
            );
        });
    }
}
