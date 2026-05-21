package com.auction.server.concurrency;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** Manages locking for high-level auction operations. */
public class AuctionLockManager {

  private static final AuctionLockManager INSTANCE = new AuctionLockManager();
  private final LockRegistry registry = new LockRegistry();

  private AuctionLockManager() {}

  public static AuctionLockManager getInstance() {
    return INSTANCE;
  }

  /** Executes a task within a lock for a specific auctionId. */
  public <T> T executeLocked(long auctionId, Supplier<T> task) {
    ReentrantLock lock = registry.getLock(auctionId);
    lock.lock();
    try {
      return task.get();
    } finally {
      lock.unlock();
    }
  }

  /** Executes a runnable task (no return value) within a lock. */
  public void executeLocked(long auctionId, Runnable task) {
    executeLocked(
        auctionId,
        () -> {
          task.run();
          return null;
        });
  }
}
