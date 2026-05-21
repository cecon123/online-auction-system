package com.auction.server.exception;

/** Raised when a user cannot be authenticated or has no active session. */
public class AuthenticationException extends BusinessException {

  public AuthenticationException(String message) {
    super(message);
  }
}
