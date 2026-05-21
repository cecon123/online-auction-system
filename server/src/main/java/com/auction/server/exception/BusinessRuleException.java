package com.auction.server.exception;

/** Raised when a valid request violates a business workflow rule. */
public class BusinessRuleException extends BusinessException {

  public BusinessRuleException(String message) {
    super(message);
  }
}
