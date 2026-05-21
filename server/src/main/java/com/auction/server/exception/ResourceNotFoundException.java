package com.auction.server.exception;

/** Raised when a required business resource cannot be found. */
public class ResourceNotFoundException extends BusinessException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
