package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.common.enums.Role;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock private UserDao userDao;

  private WalletService walletService;

  private final long USER_ID = 1L;

  @BeforeEach
  void setUp() {
    walletService = new WalletService(userDao);
  }

  @Test
  void shouldDepositSuccessfully() {
    // Arrange
    UserDao.UserRecord user = createMockUser(USER_ID, new BigDecimal("100.00"), BigDecimal.ZERO);
    when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));

    // Act
    BigDecimal newBalance = walletService.deposit(USER_ID, new BigDecimal("50.00"));

    // Assert
    assertEquals(new BigDecimal("150.00"), newBalance);
    verify(userDao).updateBalance(eq(USER_ID), eq(new BigDecimal("150.00")));
  }

  @Test
  void shouldWithdrawSuccessfully() {
    // Arrange
    UserDao.UserRecord user = createMockUser(USER_ID, new BigDecimal("100.00"), BigDecimal.ZERO);
    when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));

    // Act
    BigDecimal newBalance = walletService.withdraw(USER_ID, new BigDecimal("50.00"));

    // Assert
    assertEquals(new BigDecimal("50.00"), newBalance);
    verify(userDao).updateBalance(eq(USER_ID), eq(new BigDecimal("50.00")));
  }

  @Test
  void shouldFailWithdrawWhenInsufficientBalance() {
    // Arrange
    UserDao.UserRecord user =
        createMockUser(USER_ID, new BigDecimal("100.00"), new BigDecimal("80.00"));
    when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> {
          walletService.withdraw(USER_ID, new BigDecimal("50.00")); // Available is 20
        });
  }

  @Test
  void shouldLockFundsSuccessfully() {
    // Arrange
    UserDao.UserRecord user = createMockUser(USER_ID, new BigDecimal("100.00"), BigDecimal.ZERO);
    when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));

    // Act
    walletService.lockFunds(USER_ID, new BigDecimal("40.00"));

    // Assert
    verify(userDao).updateLockedBalance(eq(USER_ID), eq(new BigDecimal("40.00")));
  }

  @Test
  void shouldSettleAuctionSuccessfully() {
    // Arrange
    long winnerId = 1L;
    long sellerId = 2L;
    BigDecimal amount = new BigDecimal("200.00");

    UserDao.UserRecord winner =
        createMockUser(winnerId, new BigDecimal("500.00"), new BigDecimal("200.00"));
    UserDao.UserRecord seller = createMockUser(sellerId, new BigDecimal("100.00"), BigDecimal.ZERO);

    when(userDao.findById(winnerId)).thenReturn(Optional.of(winner));
    when(userDao.findById(sellerId)).thenReturn(Optional.of(seller));

    // Act
    walletService.settleAuction(winnerId, sellerId, amount);

    // Assert
    verify(userDao)
        .updateBalances(
            eq(winnerId),
            argThat(b -> b.compareTo(new BigDecimal("300.00")) == 0),
            argThat(b -> b.compareTo(BigDecimal.ZERO) == 0));
    verify(userDao)
        .updateBalance(eq(sellerId), argThat(b -> b.compareTo(new BigDecimal("300.00")) == 0));
  }

  private UserDao.UserRecord createMockUser(long id, BigDecimal balance, BigDecimal lockedBalance) {
    return new UserDao.UserRecord(
        id,
        "user" + id,
        "hash",
        "Full Name",
        Role.BIDDER,
        balance,
        lockedBalance,
        true,
        LocalDateTime.now());
  }
}
