package com.auction.server.service;

import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for handling wallet operations (deposit, withdraw). */
public class WalletService {
  private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
  private final UserDao userDao;

  public WalletService(UserDao userDao) {
    this.userDao = userDao;
  }

  public BigDecimal deposit(long userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Deposit amount must be positive.");
    }

    UserDao.UserRecord user =
        userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

    BigDecimal newBalance = user.balance().add(amount);
    userDao.updateBalance(userId, newBalance);

    logger.info("User {} deposited {}. New balance: {}", userId, amount, newBalance);
    return newBalance;
  }

  public BigDecimal withdraw(long userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Withdraw amount must be positive.");
    }

    UserDao.UserRecord user =
        userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

    if (user.balance().subtract(user.lockedBalance()).compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient available balance.");
    }

    BigDecimal newBalance = user.balance().subtract(amount);
    userDao.updateBalance(userId, newBalance);

    logger.info("User {} withdrew {}. New balance: {}", userId, amount, newBalance);
    return newBalance;
  }

  public void lockFunds(long userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Lock amount must be non-negative.");
    }

    UserDao.UserRecord user =
        userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

    if (user.balance().subtract(user.lockedBalance()).compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient available balance to lock funds.");
    }

    BigDecimal newLockedBalance = user.lockedBalance().add(amount);
    userDao.updateLockedBalance(userId, newLockedBalance);
    logger.info("Locked {} for user {}. New locked balance: {}", amount, userId, newLockedBalance);
  }

  public void releaseFunds(long userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Release amount must be non-negative.");
    }

    UserDao.UserRecord user =
        userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

    BigDecimal newLockedBalance = user.lockedBalance().subtract(amount);
    if (newLockedBalance.compareTo(BigDecimal.ZERO) < 0) {
      newLockedBalance = BigDecimal.ZERO;
    }
    userDao.updateLockedBalance(userId, newLockedBalance);
    logger.info(
        "Released {} for user {}. New locked balance: {}", amount, userId, newLockedBalance);
  }

  public void settleAuction(long winnerId, long sellerId, BigDecimal amount) {
    UserDao.UserRecord winner =
        userDao
            .findById(winnerId)
            .orElseThrow(() -> new IllegalArgumentException("Winner not found."));
    UserDao.UserRecord seller =
        userDao
            .findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found."));

    // Winner: subtract from both balance and locked_balance (because it was locked)
    BigDecimal winnerNewBalance = winner.balance().subtract(amount);
    BigDecimal winnerNewLocked = winner.lockedBalance().subtract(amount);
    if (winnerNewLocked.compareTo(BigDecimal.ZERO) < 0) winnerNewLocked = BigDecimal.ZERO;
    userDao.updateBalances(winnerId, winnerNewBalance, winnerNewLocked);

    // Seller: add to balance
    BigDecimal sellerNewBalance = seller.balance().add(amount);
    userDao.updateBalance(sellerId, sellerNewBalance);

    logger.info(
        "Settled auction: Winner {} paid {}, Seller {} received {}.",
        winnerId,
        amount,
        sellerId,
        amount);
  }
}
