package com.auction.server.service;

import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling wallet operations (deposit, withdraw).
 */
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

        UserDao.UserRecord user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        BigDecimal newBalance = user.balance().add(amount);
        userDao.updateBalance(userId, newBalance);

        logger.info("User {} deposited {}. New balance: {}", userId, amount, newBalance);
        return newBalance;
    }

    public BigDecimal withdraw(long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive.");
        }

        UserDao.UserRecord user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.balance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance.");
        }

        BigDecimal newBalance = user.balance().subtract(amount);
        userDao.updateBalance(userId, newBalance);

        logger.info("User {} withdrew {}. New balance: {}", userId, amount, newBalance);
        return newBalance;
    }

    public BigDecimal getBalance(long userId) {
        return userDao.findById(userId)
            .map(UserDao.UserRecord::balance)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
