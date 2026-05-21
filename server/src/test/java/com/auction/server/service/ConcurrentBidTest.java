package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteBidDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Robust stress test for concurrent bidding. */
public class ConcurrentBidTest {
  private BidService bidService;
  private SQLiteAuctionDao auctionDao;
  private static final String TEST_DB_FILE = "auction_stress_test.db";
  private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

  @BeforeAll
  static void initDb() {
    // Force a fresh database for this test
    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      boolean deleted = dbFile.delete();
      if (!deleted) {
        throw new IllegalStateException(
            "Could not delete existing test database file: " + TEST_DB_FILE);
      }
    }

    // Use a dedicated test database
    System.setProperty("auction.db.url", TEST_DB_URL);
    // Ensure skip seed is NOT set
    System.setProperty("auction.skip.seed", "false");

    SchemaInitializer.initialize();
  }

  @AfterAll
  static void cleanup() {
    // Clear property to avoid affecting other tests
    System.clearProperty("auction.db.url");

    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      // Best effort cleanup
      dbFile.delete();
    }
  }

  @BeforeEach
  void setUp() {
    auctionDao = new SQLiteAuctionDao();
    SQLiteUserDao userDao = new SQLiteUserDao();
    WalletService walletService = new WalletService(userDao);
    bidService =
        new BidService(
            auctionDao,
            new SQLiteBidDao(),
            userDao,
            new com.auction.server.dao.sqlite.SQLiteAutoBidDao(),
            walletService);
  }

  @Test
  void testTwentyThreadsConcurrentBidding() throws InterruptedException {
    int threadCount = 20;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    AtomicReference<Throwable> firstError = new AtomicReference<>();

    long auctionId = 1L; // From seed.sql
    long bidderId = 4L; // Alice Bidder (bidder01)

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              startLatch.await(); // Wait for signal

              // Each thread bids 100 more than the base to ensure price increases
              // Base current price in seed for auction 1 is 12500.00
              BigDecimal amount =
                  new BigDecimal("13000.00")
                      .add(new BigDecimal(index).multiply(new BigDecimal("100.00")));

              bidService.placeBid(bidderId, new PlaceBidRequest(auctionId, amount));
              successCount.incrementAndGet();
            } catch (Throwable t) {
              failureCount.incrementAndGet();
              firstError.compareAndSet(null, t);
            } finally {
              finishLatch.countDown();
            }
          });
    }

    startLatch.countDown(); // FIRE!

    boolean completed = finishLatch.await(15, TimeUnit.SECONDS);

    executor.shutdown();

    assertTrue(
        completed,
        "Test timed out - only " + (threadCount - finishLatch.getCount()) + " threads finished");
    assertEquals(
        threadCount,
        successCount.get() + failureCount.get(),
        "Total results must match thread count");

    assertTrue(auctionDao.findById(auctionId).isPresent(), "Auction should remain readable");

    assertTrue(
        successCount.get() > 0,
        "At least one bid should have succeeded. Error: "
            + (firstError.get() != null ? firstError.get().getMessage() : "None"));
  }
}
