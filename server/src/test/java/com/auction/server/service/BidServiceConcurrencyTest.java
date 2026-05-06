package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Electronics;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteBidDao;
import com.auction.server.dao.sqlite.SQLiteItemDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BidServiceConcurrencyTest {

    private BidService bidService;
    private AuctionDao auctionDao;
    private long auctionId;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDatabase = Files.createTempFile("auction-bid-test-", ".db");
        System.setProperty("auction.db.url", "jdbc:sqlite:" + tempDatabase.toAbsolutePath());

        SchemaInitializer.initialize();
        
        auctionDao = new SQLiteAuctionDao();
        BidDao bidDao = new SQLiteBidDao();
        ItemDao itemDao = new SQLiteItemDao();
        UserDao userDao = new SQLiteUserDao();

        bidService = new BidService(auctionDao, bidDao);

        // Setup test data
        long sellerId = userDao.create("seller", "pass", "The Seller", Role.SELLER);
        long itemId = itemDao.create(new Electronics(
            0, sellerId, "Laptop", "D", "C", new BigDecimal("100"), null, "B", "M", LocalDateTime.now()
        ));

        Auction auction = new Auction(
            0, itemId, sellerId, new BigDecimal("100"), null,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1),
            AuctionStatus.RUNNING, 0, LocalDateTime.now()
        );
        auctionId = auctionDao.create(auction);
    }

    @Test
    void testConcurrentBiddingWithSameAmount() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long bidderId = i + 1;
            executor.submit(() -> {
                try {
                    latch.await(); // Wait for all threads to be ready
                    // All threads bid the SAME amount
                    bidService.placeBid(
                        bidderId,
                        new PlaceBidRequest(
                            auctionId,
                            new BigDecimal("1500.00")
                        )
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Bid failed: " + e.getMessage());
                    // 9 threads should fail with "Bid amount must be higher than the current price" or version mismatch
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown(); // Start all threads at once
        executor.shutdown();

        // Wait for all threads to finish
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        System.out.println("Success bids: " + successCount.get());
        System.out.println("Failed bids: " + failureCount.get());

        // Exactly ONE bid should succeed because of the lock, the rest will read the updated price and fail
        assertEquals(1, successCount.get(), "Chỉ duy nhất 1 thread được bid thành công.");
        assertEquals(threadCount - 1, failureCount.get(), "Các thread còn lại phải bị từ chối.");
        
        Auction updatedAuction = auctionDao.findById(auctionId).get();
        assertEquals(new BigDecimal("1500.00"), updatedAuction.getCurrentPrice());
    }
}
