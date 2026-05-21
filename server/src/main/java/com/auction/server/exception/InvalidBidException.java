package com.auction.server.exception;

/** Raised when a bid violates auction bidding rules. */
public class InvalidBidException extends BusinessException {

  public InvalidBidException(String message) {
    super(message);
  }
}
