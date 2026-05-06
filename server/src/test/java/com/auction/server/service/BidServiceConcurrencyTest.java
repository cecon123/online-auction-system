package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.dto.bid.PlaceBidRequest;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BidServiceConcurrencyTest {

    private BidService bidService;

    @BeforeEach
    void setUp() {
        bidService = new BidService();
    }

    @Test
    void testConcurrentBidding() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long auctionId = 1L;
        // Mock current price is 1000.00. We send bids of 1500.00.
        // With the current MOCK logic in BidService, all 10 should "succeed"
        // because the current price doesn't actually update in the mock DB.
        // But this test ensures the Locking mechanism doesn't crash or deadlock.

        for (int i = 0; i < threadCount; i++) {
            final long bidderId = i + 1;
            executor.submit(() -> {
                try {
                    latch.await(); // Wait for all threads to be ready
                    bidService.placeBid(bidderId, new PlaceBidRequest(auctionId, new BigDecimal("1500.00")));
                    successCount.incrementAndGet();
                } catch (Exception e) {
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

        assertEquals(threadCount, successCount.get(), "Tất cả các thread phải hoàn thành mà không bị crash do tranh chấp lock.");
    }
}
