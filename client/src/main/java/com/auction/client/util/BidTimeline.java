package com.auction.client.util;

import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.dto.bid.PlaceBidResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Canonical bid timeline used to merge initial history snapshots with realtime events. */
public final class BidTimeline {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final Comparator<Entry> CHRONOLOGICAL =
      Comparator.comparing(Entry::timestamp)
          .thenComparing(Entry::amount)
          .thenComparing(Entry::bidderUsername)
          .thenComparing(Entry::key);

  private final Map<String, Entry> entriesByKey = new LinkedHashMap<>();

  public void clear() {
    entriesByKey.clear();
  }

  public boolean isEmpty() {
    return entriesByKey.isEmpty();
  }

  public boolean mergeHistory(Collection<PlaceBidResponse> history) {
    if (history == null || history.isEmpty()) {
      return false;
    }
    boolean changed = false;
    for (PlaceBidResponse bid : history) {
      changed |= merge(toEntry(bid));
    }
    return changed;
  }

  public boolean mergeEvent(BidUpdateEvent event) {
    return merge(toEntry(event));
  }

  public boolean mergeEvents(Collection<BidUpdateEvent> events) {
    if (events == null || events.isEmpty()) {
      return false;
    }
    boolean changed = false;
    for (BidUpdateEvent event : events) {
      changed |= mergeEvent(event);
    }
    return changed;
  }

  public List<Entry> newestFirst() {
    List<Entry> entries = chronological();
    java.util.Collections.reverse(entries);
    return entries;
  }

  public List<Entry> chronological() {
    List<Entry> entries = new ArrayList<>(entriesByKey.values());
    entries.sort(CHRONOLOGICAL);
    return entries;
  }

  public List<ChartPoint> chartPoints(int maxPoints) {
    List<Entry> entries = chronological();
    int start = Math.max(0, entries.size() - Math.max(0, maxPoints));
    List<Entry> visible = entries.subList(start, entries.size());
    Map<String, Integer> labelCounts = new LinkedHashMap<>();
    List<ChartPoint> points = new ArrayList<>(visible.size());
    for (Entry entry : visible) {
      String baseLabel = entry.timestamp().format(TIME_FMT);
      int count = labelCounts.merge(baseLabel, 1, Integer::sum);
      String label = count == 1 ? baseLabel : baseLabel + " #" + count;
      points.add(new ChartPoint(label, entry.amount()));
    }
    return points;
  }

  private boolean merge(Entry entry) {
    if (entry == null || entry.amount() == null || entry.auctionId() <= 0) {
      return false;
    }
    return entriesByKey.putIfAbsent(entry.key(), entry) == null;
  }

  private Entry toEntry(PlaceBidResponse bid) {
    if (bid == null) {
      return null;
    }
    return createEntry(
        bid.auctionId(), bid.currentPrice(), bid.highestBidderUsername(), bid.timestamp());
  }

  private Entry toEntry(BidUpdateEvent event) {
    if (event == null || event.auctionId() == null) {
      return null;
    }
    return createEntry(event.auctionId(), event.amount(), event.bidderUsername(), event.timestamp());
  }

  private Entry createEntry(
      long auctionId, BigDecimal amount, String bidderUsername, LocalDateTime timestamp) {
    LocalDateTime safeTimestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    String safeBidder =
        bidderUsername == null || bidderUsername.isBlank() ? "Unknown" : bidderUsername;
    String key =
        auctionId
            + "|"
            + safeTimestamp
            + "|"
            + safeBidder
            + "|"
            + normalizeAmount(amount);
    return new Entry(key, auctionId, amount, safeBidder, safeTimestamp);
  }

  private String normalizeAmount(BigDecimal amount) {
    return amount == null ? "" : amount.stripTrailingZeros().toPlainString();
  }

  public record Entry(
      String key,
      long auctionId,
      BigDecimal amount,
      String bidderUsername,
      LocalDateTime timestamp) {}

  public record ChartPoint(String label, BigDecimal amount) {}
}
