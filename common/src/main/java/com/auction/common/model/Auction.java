package com.auction.common.model;

import com.auction.common.enums.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Represents an auction session for one item. */
public class Auction extends Entity {

  private long itemId;
  private long sellerId;
  private BigDecimal currentPrice;
  private BigDecimal highestMaxBid;
  private BigDecimal reservePrice;
  private Long highestBidderId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private AuctionStatus status;
  private long version;

  public Auction(
      long id,
      long itemId,
      long sellerId,
      BigDecimal currentPrice,
      BigDecimal highestMaxBid,
      BigDecimal reservePrice,
      Long highestBidderId,
      LocalDateTime startTime,
      LocalDateTime endTime,
      AuctionStatus status,
      long version,
      LocalDateTime createdAt) {
    super(id, createdAt);
    setItemId(itemId);
    setSellerId(sellerId);
    setCurrentPrice(currentPrice);
    setHighestMaxBid(highestMaxBid != null ? highestMaxBid : currentPrice);
    setReservePrice(reservePrice);
    this.highestBidderId = highestBidderId;
    setStartTime(startTime);
    setEndTime(endTime);
    this.status = Objects.requireNonNull(status, "status must not be null");
    this.version = version;
  }

  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    if (itemId <= 0) {
      throw new IllegalArgumentException("itemId must be positive.");
    }
    this.itemId = itemId;
  }

  public long getSellerId() {
    return sellerId;
  }

  public void setSellerId(long sellerId) {
    if (sellerId <= 0) {
      throw new IllegalArgumentException("sellerId must be positive.");
    }
    this.sellerId = sellerId;
  }

  public BigDecimal getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(BigDecimal currentPrice) {
    if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("currentPrice must be non-negative.");
    }
    this.currentPrice = currentPrice;
  }

  public BigDecimal getHighestMaxBid() {
    return highestMaxBid;
  }

  public void setHighestMaxBid(BigDecimal highestMaxBid) {
    if (highestMaxBid != null && highestMaxBid.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("highestMaxBid must be non-negative.");
    }
    this.highestMaxBid = highestMaxBid;
  }

  public BigDecimal getReservePrice() {
    return reservePrice;
  }

  public void setReservePrice(BigDecimal reservePrice) {
    this.reservePrice = reservePrice;
  }

  public boolean isReserveMet() {
    return reservePrice == null
        || (currentPrice != null && currentPrice.compareTo(reservePrice) >= 0);
  }

  public Long getHighestBidderId() {
    return highestBidderId;
  }

  public void setHighestBidderId(Long highestBidderId) {
    this.highestBidderId = highestBidderId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = Objects.requireNonNull(endTime, "endTime must not be null");
  }

  public AuctionStatus getStatus() {
    return status;
  }

  public void setStatus(AuctionStatus status) {
    this.status = Objects.requireNonNull(status, "status must not be null");
  }

  public long getVersion() {
    return version;
  }

  public void increaseVersion() {
    this.version++;
  }

  public boolean isRunningAt(LocalDateTime now) {
    Objects.requireNonNull(now, "now must not be null");

    return (status == AuctionStatus.RUNNING && !now.isBefore(startTime) && now.isBefore(endTime));
  }

  public boolean canAcceptBidAt(LocalDateTime now) {
    return isRunningAt(now);
  }

  public void updateHighestBid(long bidderId, BigDecimal amount) {
    if (amount == null || amount.compareTo(currentPrice) <= 0) {
      throw new IllegalArgumentException("New bid must be higher than current price.");
    }

    this.highestBidderId = bidderId;
    this.currentPrice = amount;
  }

  public void extendEndTimeSeconds(long seconds) {
    if (seconds <= 0) {
      throw new IllegalArgumentException("seconds must be positive.");
    }

    this.endTime = this.endTime.plusSeconds(seconds);
  }

  @Override
  public String displayName() {
    return "Auction #" + getId() + " for item #" + itemId;
  }
}
