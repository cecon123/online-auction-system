package com.auction.server.dao;

import com.auction.common.model.AutoBidRule;
import java.util.List;
import java.util.Optional;

/** Data access interface for AutoBidRules. */
public interface AutoBidDao {
  /** Persists a new rule or updates an existing one for the same auction and bidder. */
  void createOrUpdate(AutoBidRule rule);

  /** Finds a rule for a specific auction and bidder. */
  Optional<AutoBidRule> findByAuctionAndBidder(long auctionId, long bidderId);

  /** Finds all active rules for a specific auction. */
  List<AutoBidRule> findActiveByAuction(long auctionId);

  /** Deactivates or removes a rule. */
  void delete(long auctionId, long bidderId);
}
