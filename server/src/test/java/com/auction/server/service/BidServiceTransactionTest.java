package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.Electronics;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteAutoBidDao;
import com.auction.server.dao.sqlite.SQLiteItemDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BidServiceTransactionTest {

  @TempDir Path tempDir;

  private SQLiteUserDao userDao;
  private SQLiteAuctionDao auctionDao;
  private BidService bidService;

  private long auctionId;
  private long previousBidderId;
  private long newBidderId;

  @BeforeEach
  void setUp() {
    System.setProperty("auction.db.url", "jdbc:sqlite:" + tempDir.resolve("bid-tx.db"));
    System.setProperty("auction.skip.seed", "true");
    SchemaInitializer.initialize();

    userDao = new SQLiteUserDao();
    auctionDao = new SQLiteAuctionDao();
    WalletService walletService = new WalletService(userDao);
    bidService =
        new BidService(
            auctionDao,
            new FailingBidDao(),
            userDao,
            new SQLiteAutoBidDao(),
            walletService);

    long sellerId =
        userDao.create(
            "seller",
            "hash",
            "Seller",
            Role.SELLER,
            new BigDecimal("1000.00"),
            BigDecimal.ZERO);
    previousBidderId =
        userDao.create(
            "previous",
            "hash",
            "Previous Bidder",
            Role.BIDDER,
            new BigDecimal("1000.00"),
            new BigDecimal("100.00"));
    newBidderId =
        userDao.create(
            "new_bidder",
            "hash",
            "New Bidder",
            Role.BIDDER,
            new BigDecimal("1000.00"),
            BigDecimal.ZERO);

    Electronics item =
        new Electronics(
            0,
            sellerId,
            "Phone",
            "Test phone",
            "New",
            new BigDecimal("100.00"),
            null,
            "Brand",
            "Model",
            LocalDateTime.now());
    long itemId = new SQLiteItemDao().create(item);

    Auction auction =
        new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            null,
            previousBidderId,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(1),
            AuctionStatus.RUNNING,
            0,
            LocalDateTime.now());
    auctionId = auctionDao.create(auction);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("auction.db.url");
    System.clearProperty("auction.skip.seed");
  }

  @Test
  void shouldRollbackWalletAndAuctionWhenBidPersistenceFails() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                bidService.placeBid(
                    newBidderId, new PlaceBidRequest(auctionId, new BigDecimal("150.00"))));

    assertEquals("forced bid insert failure", exception.getMessage());

    Auction reloadedAuction = auctionDao.findById(auctionId).orElseThrow();
    assertEquals(0, new BigDecimal("100.00").compareTo(reloadedAuction.getCurrentPrice()));
    assertEquals(0, new BigDecimal("100.00").compareTo(reloadedAuction.getHighestMaxBid()));
    assertEquals(previousBidderId, reloadedAuction.getHighestBidderId());

    UserDao.UserRecord previousBidder = userDao.findById(previousBidderId).orElseThrow();
    UserDao.UserRecord newBidder = userDao.findById(newBidderId).orElseThrow();
    assertEquals(0, new BigDecimal("100.00").compareTo(previousBidder.lockedBalance()));
    assertEquals(0, BigDecimal.ZERO.compareTo(newBidder.lockedBalance()));
  }

  private static final class FailingBidDao implements BidDao {
    @Override
    public long create(BidTransaction bid) {
      throw new IllegalStateException("forced bid insert failure");
    }

    @Override
    public List<BidTransaction> findByAuctionId(long auctionId) {
      return List.of();
    }

    @Override
    public List<BidTransaction> findByBidderId(long bidderId) {
      return List.of();
    }
  }
}
