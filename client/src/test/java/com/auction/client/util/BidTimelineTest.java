package com.auction.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.dto.bid.PlaceBidResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BidTimelineTest {

  private static final long AUCTION_ID = 5L;
  private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 5, 16, 10, 0, 0);

  @Test
  void mergeHistoryShouldNotDropRealtimeBidWhenOlderSnapshotArrivesLater() {
    BidTimeline timeline = new BidTimeline();

    timeline.mergeHistory(List.of(history("bidder01", "400.00", BASE_TIME)));
    timeline.mergeEvent(event("bidder02", "450.00", BASE_TIME.plusSeconds(1)));
    timeline.mergeHistory(List.of(history("bidder01", "400.00", BASE_TIME)));

    List<BidTimeline.Entry> newestFirst = timeline.newestFirst();
    assertEquals(2, newestFirst.size());
    assertEquals("bidder02", newestFirst.get(0).bidderUsername());
    assertEquals(new BigDecimal("450.00"), newestFirst.get(0).amount());
  }

  @Test
  void mergeEventShouldIgnoreDuplicateRealtimeEvent() {
    BidTimeline timeline = new BidTimeline();
    BidUpdateEvent event = event("bidder01", "400.00", BASE_TIME);

    assertTrue(timeline.mergeEvent(event));
    assertFalse(timeline.mergeEvent(event));

    assertEquals(1, timeline.newestFirst().size());
  }

  @Test
  void chartPointsShouldKeepAllAutoBidBatchStepsWithUniqueLabels() {
    BidTimeline timeline = new BidTimeline();

    timeline.mergeEvents(
        List.of(
            event("bidder01", "400.00", BASE_TIME),
            event("bidder02", "450.00", BASE_TIME),
            event("bidder01", "500.00", BASE_TIME)));

    List<BidTimeline.ChartPoint> chartPoints = timeline.chartPoints(15);

    assertEquals(3, chartPoints.size());
    assertEquals("10:00:00", chartPoints.get(0).label());
    assertEquals("10:00:00 #2", chartPoints.get(1).label());
    assertEquals("10:00:00 #3", chartPoints.get(2).label());
    assertEquals(new BigDecimal("500.00"), chartPoints.get(2).amount());
  }

  @Test
  void newestFirstShouldBeSameForBothBiddersAfterSharedSequence() {
    BidTimeline bidder01View = new BidTimeline();
    BidTimeline bidder02View = new BidTimeline();
    List<BidUpdateEvent> events =
        List.of(
            event("bidder01", "400.00", BASE_TIME),
            event("bidder02", "450.00", BASE_TIME.plusSeconds(1)),
            event("bidder01", "500.00", BASE_TIME.plusSeconds(2)));

    bidder01View.mergeEvents(events);
    bidder02View.mergeEvents(events);

    assertEquals(bidder01View.newestFirst(), bidder02View.newestFirst());
    assertEquals("bidder01", bidder01View.newestFirst().get(0).bidderUsername());
    assertEquals("bidder02", bidder01View.newestFirst().get(1).bidderUsername());
  }

  private static PlaceBidResponse history(String bidder, String amount, LocalDateTime timestamp) {
    return new PlaceBidResponse(AUCTION_ID, new BigDecimal(amount), bidder, timestamp);
  }

  private static BidUpdateEvent event(String bidder, String amount, LocalDateTime timestamp) {
    return new BidUpdateEvent(
        AUCTION_ID, bidder, new BigDecimal(amount), timestamp, timestamp.plusMinutes(5));
  }
}
