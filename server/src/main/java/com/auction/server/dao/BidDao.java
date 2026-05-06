package com.auction.server.dao;

import com.auction.common.model.BidTransaction;
import java.util.List;

/**
 * Data access interface for Bids.
 */
public interface BidDao {
    /**
     * Persists a new bid transaction.
     * @return The generated ID.
     */
    long create(BidTransaction bid);

    /**
     * Finds all bids for a specific auction.
     */
    List<BidTransaction> findByAuctionId(long auctionId);

    /**
     * Finds all bids made by a specific bidder.
     */
    List<BidTransaction> findByBidderId(long bidderId);
}
