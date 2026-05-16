package com.auction.server.exception;

/** Raised when an operation requires an active auction but the auction is closed. */
public class AuctionClosedException extends BusinessException {

  public AuctionClosedException(String message) {
    super(message);
  }
}
