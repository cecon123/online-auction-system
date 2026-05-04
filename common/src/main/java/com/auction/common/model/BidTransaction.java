package com.auction.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a valid bid recorded in the system.
 */
public class BidTransaction extends Entity {

    private long auctionId;
    private long bidderId;
    private BigDecimal amount;

    public BidTransaction(
        long id,
        long auctionId,
        long bidderId,
        BigDecimal amount,
        LocalDateTime createdAt
    ) {
        super(id, createdAt);
        setAuctionId(auctionId);
        setBidderId(bidderId);
        setAmount(amount);
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive.");
        }

        this.amount = amount;
    }

    @Override
    public String displayName() {
        return "Bid #" + getId() + " - " + amount;
    }
}
