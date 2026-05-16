package com.auction.server.exception;

/** Raised when request data is missing or invalid for a business operation. */
public class ValidationException extends BusinessException {

  public ValidationException(String message) {
    super(message);
  }
}
