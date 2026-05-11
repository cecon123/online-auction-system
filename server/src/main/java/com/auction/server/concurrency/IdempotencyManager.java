package com.auction.server.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Singleton manager to prevent Replay Attacks by tracking recently processed requestIds. */
public class IdempotencyManager {

  private static final IdempotencyManager INSTANCE = new IdempotencyManager();

  // Maps requestId to expiration timestamp
  private final Map<String, Long> processedRequests = new ConcurrentHashMap<>();

  // Retention period for requestIds (e.g., 10 minutes)
  private static final long EXPIRATION_MS = 10 * 60 * 1000;

  private final ScheduledExecutorService cleaner =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "IdempotencyCleaner");
            thread.setDaemon(true);
            return thread;
          });

  private IdempotencyManager() {
    // Run cleanup every minute
    cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
  }

  public static IdempotencyManager getInstance() {
    return INSTANCE;
  }

  /**
   * Checks if a requestId has been processed and registers it if not.
   *
   * @param requestId The ID to check.
   * @return true if it's a new request, false if it's a replay.
   */
  public boolean isNewRequest(String requestId) {
    if (requestId == null || requestId.isBlank()) {
      return true; // Don't track empty IDs
    }

    long now = System.currentTimeMillis();
    Long existing = processedRequests.putIfAbsent(requestId, now + EXPIRATION_MS);

    return existing == null;
  }

  private void cleanup() {
    long now = System.currentTimeMillis();
    processedRequests.entrySet().removeIf(entry -> now > entry.getValue());
  }
}
