package com.auction.server.service;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.UserDao;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionManagerServiceTest {

  @Mock private UserDao userDao;
  @Mock private WalletService walletService;

  private FakeAuctionDao auctionDao;
  private AuctionManagerService auctionManagerService;

  @BeforeEach
  void setUp() {
    auctionDao = new FakeAuctionDao();
    auctionManagerService = new AuctionManagerService(auctionDao, userDao, walletService);
  }

  @Test
  void shouldKeepFinishedAuctionWhenSettlementFails() throws Exception {
    Auction auction =
        new Auction(
            1L,
            10L,
            2L,
            new BigDecimal("200.00"),
            new BigDecimal("200.00"),
            new BigDecimal("150.00"),
            4L,
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusMinutes(1),
            AuctionStatus.FINISHED,
            0L,
            LocalDateTime.now().minusHours(3));

    org.mockito.Mockito.doThrow(new IllegalStateException("wallet temporarily unavailable"))
        .when(walletService)
        .settleAuction(4L, 2L, new BigDecimal("200.00"));

    invokeUpdateStatus(auction);

    org.junit.jupiter.api.Assertions.assertEquals(1, auctionDao.settlementState.attempts());
    org.junit.jupiter.api.Assertions.assertTrue(
        auctionDao.settlementState.lastError().contains("wallet temporarily unavailable"));
    org.junit.jupiter.api.Assertions.assertNotNull(auctionDao.settlementState.nextRetryAt());
  }

  private void invokeUpdateStatus(Auction auction) throws Exception {
    Method method =
        AuctionManagerService.class.getDeclaredMethod(
            "updateStatusIfNecessary", Auction.class, LocalDateTime.class);
    method.setAccessible(true);
    method.invoke(auctionManagerService, auction, LocalDateTime.now());
  }

  private static class FakeAuctionDao implements AuctionDao {
    private SettlementState settlementState = new SettlementState(0, null, null);

    @Override
    public long create(Auction auction) {
      return auction.getId();
    }

    @Override
    public Optional<Auction> findById(long id) {
      return Optional.empty();
    }

    @Override
    public Optional<Auction> findByItemId(long itemId) {
      return Optional.empty();
    }

    @Override
    public List<Auction> findAll() {
      return List.of();
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
      return List.of();
    }

    @Override
    public List<Auction> findByBidderId(long bidderId) {
      return List.of();
    }

    @Override
    public List<Auction> findBySellerId(long sellerId) {
      return List.of();
    }

    @Override
    public void update(Auction auction) {}

    @Override
    public SettlementState getSettlementState(long auctionId) {
      return settlementState;
    }

    @Override
    public void markSettlementFailed(
        long auctionId, int attempts, String lastError, LocalDateTime nextRetryAt) {
      settlementState = new SettlementState(attempts, lastError, nextRetryAt);
    }

    @Override
    public void clearSettlementFailure(long auctionId) {
      settlementState = new SettlementState(0, null, null);
    }
  }
}
