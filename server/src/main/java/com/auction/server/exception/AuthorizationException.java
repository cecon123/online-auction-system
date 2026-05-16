package com.auction.server.exception;

/** Raised when an authenticated user is not allowed to perform an operation. */
public class AuthorizationException extends BusinessException {

  public AuthorizationException(String message) {
    super(message);
  }
}
