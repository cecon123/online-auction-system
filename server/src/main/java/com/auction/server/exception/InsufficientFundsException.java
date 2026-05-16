package com.auction.server.exception;

/** Raised when a wallet does not have enough available or locked funds. */
public class InsufficientFundsException extends BusinessException {

  public InsufficientFundsException(String message) {
    super(message);
  }
}
