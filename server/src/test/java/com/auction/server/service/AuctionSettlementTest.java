package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Electronics;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteAutoBidDao;
import com.auction.server.dao.sqlite.SQLiteBidDao;
import com.auction.server.dao.sqlite.SQLiteItemDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionSettlementTest {

  private SQLiteUserDao userDao;
  private SQLiteAuctionDao auctionDao;
  private WalletService walletService;
  private BidService bidService;
  private AuctionManagerService auctionManager;

  private long sellerId;
  private long bidderId;
  private long itemId;

  @BeforeEach
  void setUp() {
    com.auction.server.dao.SchemaInitializer.initialize();
    userDao = new SQLiteUserDao();
    auctionDao = new SQLiteAuctionDao();
    walletService = new WalletService(userDao);
    bidService =
        new BidService(
            auctionDao, new SQLiteBidDao(), userDao, new SQLiteAutoBidDao(), walletService);
    auctionManager = new AuctionManagerService(auctionDao, userDao, walletService);

    String suffix = String.valueOf(System.currentTimeMillis()) + "_" + (int) (Math.random() * 1000);
    sellerId =
        userDao.create(
            "seller_" + suffix,
            "hash",
            "Seller",
            Role.SELLER,
            new BigDecimal("1000"),
            BigDecimal.ZERO);
    bidderId =
        userDao.create(
            "bidder_" + suffix,
            "hash",
            "Bidder",
            Role.BIDDER,
            new BigDecimal("5000"),
            BigDecimal.ZERO);

    Electronics item =
        new Electronics(
            0,
            sellerId,
            "Test Item",
            "Desc",
            "New",
            new BigDecimal("100"),
            null,
            "Brand",
            "Model",
            LocalDateTime.now());
    itemId = new SQLiteItemDao().create(item);
  }

  @Test
  void successfulAuctionShouldTransferFunds() {
    // 1. Create Auction
    Auction auction =
        new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("150"), // Reserve price
            null,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusSeconds(2), // Ends very soon
            AuctionStatus.RUNNING,
            0,
            LocalDateTime.now());
    long auctionId = auctionDao.create(auction);

    // 2. Place Bid (Higher than reserve)
    BigDecimal bidAmount = new BigDecimal("200");
    bidService.placeBid(bidderId, new PlaceBidRequest(auctionId, bidAmount));

    // 3. Manually EXPIRE the auction in DB to bypass Anti-sniping wait
    Auction latest = auctionDao.findById(auctionId).get();
    latest.setEndTime(LocalDateTime.now().minusSeconds(1));
    auctionDao.update(latest);

    // 4. Run Auction Manager check
    auctionManager.start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
    }
    auctionManager.stop();

    // 4. Verify Settlement
    Auction finishedAuction = auctionDao.findById(auctionId).get();
    assertEquals(AuctionStatus.PAID, finishedAuction.getStatus());

    // Bidder: 5000 - 200 = 4800. Locked should be 0.
    UserDao.UserRecord finalBidder = userDao.findById(bidderId).get();
    assertEquals(0, new BigDecimal("4800.00").compareTo(finalBidder.balance()));
    assertEquals(0, new BigDecimal("0.00").compareTo(finalBidder.lockedBalance()));

    // Seller: 1000 + 200 = 1200
    UserDao.UserRecord finalSeller = userDao.findById(sellerId).get();
    assertEquals(0, new BigDecimal("1200.00").compareTo(finalSeller.balance()));
  }

  @Test
  void failedAuctionBelowReserveShouldRefundFunds() {
    // 1. Create Auction with high reserve
    Auction auction =
        new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("1000"), // Reserve price very high
            null,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusSeconds(2),
            AuctionStatus.RUNNING,
            0,
            LocalDateTime.now());
    long auctionId = auctionDao.create(auction);

    // 2. Place Bid (Below reserve)
    BigDecimal bidAmount = new BigDecimal("500");
    bidService.placeBid(bidderId, new PlaceBidRequest(auctionId, bidAmount));

    // 3. Manually EXPIRE
    Auction latest = auctionDao.findById(auctionId).get();
    latest.setEndTime(LocalDateTime.now().minusSeconds(1));
    auctionDao.update(latest);

    // 4. Wait and Settle
    auctionManager.start();
    try {
      Thread.sleep(7000);
    } catch (InterruptedException e) {
    }
    auctionManager.stop();

    // 4. Verify Refund
    Auction canceledAuction = auctionDao.findById(auctionId).get();
    assertEquals(AuctionStatus.CANCELED, canceledAuction.getStatus());

    // Bidder: Should have original 5000 back, 0 locked.
    UserDao.UserRecord finalBidder = userDao.findById(bidderId).get();
    assertEquals(0, new BigDecimal("5000.00").compareTo(finalBidder.balance()));
    assertEquals(0, new BigDecimal("0.00").compareTo(finalBidder.lockedBalance()));

    // Seller: Should still have 1000.
    UserDao.UserRecord finalSeller = userDao.findById(sellerId).get();
    assertEquals(0, new BigDecimal("1000.00").compareTo(finalSeller.balance()));
  }
}
