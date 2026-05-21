package com.auction.server.dao;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Data access interface for Auctions. */
public interface AuctionDao {
  /**
   * Persists a new auction.
   *
   * @return The generated ID.
   */
  long create(Auction auction);

  /** Finds an auction by its ID. */
  Optional<Auction> findById(long id);

  /** Finds an auction by its associated item ID. */
  Optional<Auction> findByItemId(long itemId);

  /** Finds all auctions. */
  List<Auction> findAll();

  /** Finds auctions by status. */
  List<Auction> findByStatus(AuctionStatus status);

  /** Finds auctions where the given user is the highest bidder. */
  List<Auction> findByBidderId(long bidderId);

  /** Finds auctions created by the given seller. */
  List<Auction> findBySellerId(long sellerId);

  /**
   * Updates an existing auction. Implementations should use optimistic locking via the version
   * field.
   *
   * @param auction The auction to update.
   * @throws IllegalStateException if the update fails due to a version mismatch.
   */
  void update(Auction auction);

  default SettlementState getSettlementState(long auctionId) {
    return new SettlementState(0, null, null);
  }

  default void markSettlementFailed(
      long auctionId, int attempts, String lastError, LocalDateTime nextRetryAt) {}

  default void clearSettlementFailure(long auctionId) {}

  record SettlementState(int attempts, String lastError, LocalDateTime nextRetryAt) {}
}
