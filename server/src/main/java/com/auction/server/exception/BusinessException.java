package com.auction.server.exception;

/** Base class for business-level errors that can be shown to clients. */
public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}
