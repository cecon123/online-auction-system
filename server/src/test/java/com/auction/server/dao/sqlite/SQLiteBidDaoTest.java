package com.auction.server.dao.sqlite;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.Electronics;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SQLiteBidDaoTest {

  private BidDao bidDao;
  private AuctionDao auctionDao;
  private ItemDao itemDao;
  private UserDao userDao;
  private long bidderId;
  private long auctionId;

  @BeforeEach
  void setUp() throws Exception {
    Path tempDatabase = Files.createTempFile("auction-bid-test-", ".db");
    System.setProperty("auction.db.url", "jdbc:sqlite:" + tempDatabase.toAbsolutePath());
    System.setProperty("auction.skip.seed", "true");

    SchemaInitializer.initialize();
    bidDao = new SQLiteBidDao();
    auctionDao = new SQLiteAuctionDao();
    itemDao = new SQLiteItemDao();
    userDao = new SQLiteUserDao();

    long sellerId =
        userDao.create(
            "seller",
            "pass",
            "The Seller",
            Role.SELLER,
            java.math.BigDecimal.ZERO,
            java.math.BigDecimal.ZERO);
    bidderId =
        userDao.create(
            "bidder",
            "pass",
            "The Bidder",
            Role.BIDDER,
            new java.math.BigDecimal("1000"),
            java.math.BigDecimal.ZERO);
    long itemId =
        itemDao.create(
            new Electronics(
                0,
                sellerId,
                "Laptop",
                "D",
                "C",
                new BigDecimal("100"),
                null,
                "B",
                "M",
                LocalDateTime.now()));
    auctionId =
        auctionDao.create(
            new Auction(
                0,
                itemId,
                sellerId,
                new BigDecimal("100"),
                new BigDecimal("100"), // highestMaxBid
                null, // reservePrice
                null, // highestBidderId
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                AuctionStatus.RUNNING,
                0,
                LocalDateTime.now()));
  }

  @Test
  void createBidShouldReturnId() {
    BidTransaction bid =
        new BidTransaction(0, auctionId, bidderId, new BigDecimal("150"), LocalDateTime.now());
    long id = bidDao.create(bid);
    assertTrue(id > 0);

    List<BidTransaction> bids = bidDao.findByAuctionId(auctionId);
    assertEquals(1, bids.size());
    assertEquals(new BigDecimal("150"), bids.get(0).getAmount());
  }

  @Test
  void createBidShouldPreserveProvidedTimestamp() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 5, 16, 10, 30, 45);
    bidDao.create(new BidTransaction(0, auctionId, bidderId, new BigDecimal("150"), createdAt));

    List<BidTransaction> bids = bidDao.findByAuctionId(auctionId);

    assertEquals(createdAt, bids.get(0).getCreatedAt());
  }

  @Test
  void findByAuctionIdShouldUseIdAsTieBreakerForSameTimestamp() {
    LocalDateTime sameTimestamp = LocalDateTime.of(2026, 5, 16, 11, 0, 0);
    long firstId =
        bidDao.create(
            new BidTransaction(0, auctionId, bidderId, new BigDecimal("150"), sameTimestamp));
    long secondId =
        bidDao.create(
            new BidTransaction(0, auctionId, bidderId, new BigDecimal("200"), sameTimestamp));

    List<BidTransaction> bids = bidDao.findByAuctionId(auctionId);

    assertEquals(secondId, bids.get(0).getId());
    assertEquals(firstId, bids.get(1).getId());
  }

  @Test
  void findByBidderIdShouldReturnBids() {
    bidDao.create(
        new BidTransaction(0, auctionId, bidderId, new BigDecimal("150"), LocalDateTime.now()));
    bidDao.create(
        new BidTransaction(0, auctionId, bidderId, new BigDecimal("200"), LocalDateTime.now()));

    List<BidTransaction> bids = bidDao.findByBidderId(bidderId);
    assertEquals(2, bids.size());
  }

  @Test
  void findByBidderIdShouldUseIdAsTieBreakerForSameTimestamp() {
    LocalDateTime sameTimestamp = LocalDateTime.of(2026, 5, 16, 11, 30, 0);
    long firstId =
        bidDao.create(
            new BidTransaction(0, auctionId, bidderId, new BigDecimal("150"), sameTimestamp));
    long secondId =
        bidDao.create(
            new BidTransaction(0, auctionId, bidderId, new BigDecimal("200"), sameTimestamp));

    List<BidTransaction> bids = bidDao.findByBidderId(bidderId);

    assertEquals(secondId, bids.get(0).getId());
    assertEquals(firstId, bids.get(1).getId());
  }
}
