package com.auction.server.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Registry to manage and reuse ReentrantLocks per auctionId. */
public class LockRegistry {

  private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

  /** Retrieves a lock for a specific ID. Creates a new one if it doesn't exist. */
  public ReentrantLock getLock(long id) {
    return locks.computeIfAbsent(id, k -> new ReentrantLock());
  }

  /** Removes a lock when no longer needed. */
  public void removeLock(long id) {
    locks.remove(id);
  }
}
