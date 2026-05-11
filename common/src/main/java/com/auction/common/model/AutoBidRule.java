package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Optional feature model for auto-bidding.
 *
 * <p>A bidder can define maxBid and increment. The server will use this rule to automatically place
 * bids later.
 */
public class AutoBidRule extends Entity {

  private long auctionId;
  private long bidderId;
  private BigDecimal maxBid;
  private BigDecimal increment;
  private boolean active;

  public AutoBidRule(
      long id,
      long auctionId,
      long bidderId,
      BigDecimal maxBid,
      BigDecimal increment,
      boolean active,
      LocalDateTime createdAt) {
    super(id, createdAt);
    setAuctionId(auctionId);
    setBidderId(bidderId);
    setMaxBid(maxBid);
    setIncrement(increment);
    this.active = active;
  }

  public long getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(long auctionId) {
    if (auctionId <= 0) {
      throw new IllegalArgumentException("auctionId must be positive.");
    }
    this.auctionId = auctionId;
  }

  public long getBidderId() {
    return bidderId;
  }

  public void setBidderId(long bidderId) {
    if (bidderId <= 0) {
      throw new IllegalArgumentException("bidderId must be positive.");
    }
    this.bidderId = bidderId;
  }

  public BigDecimal getMaxBid() {
    return maxBid;
  }

  public void setMaxBid(BigDecimal maxBid) {
    if (maxBid == null || maxBid.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("maxBid must be positive.");
    }
    this.maxBid = maxBid;
  }

  public BigDecimal getIncrement() {
    return increment;
  }

  public void setIncrement(BigDecimal increment) {
    if (increment == null || increment.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("increment must be positive.");
    }
    this.increment = increment;
  }

  public boolean isActive() {
    return active;
  }

  public void deactivate() {
    this.active = false;
  }

  public boolean canBidUpTo(BigDecimal amount) {
    return active && amount != null && amount.compareTo(maxBid) <= 0;
  }

  @Override
  public String displayName() {
    return "AutoBidRule #" + getId() + " max=" + maxBid;
  }
}
