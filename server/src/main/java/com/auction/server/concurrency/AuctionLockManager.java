package com.auction.server.concurrency;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Quản lý locking cho các thao tác đấu giá cấp cao.
 */
public class AuctionLockManager {

    private static final AuctionLockManager INSTANCE = new AuctionLockManager();
    private final LockRegistry registry = new LockRegistry();

    private AuctionLockManager() {}

    public static AuctionLockManager getInstance() {
        return INSTANCE;
    }

    /**
     * Thực thi một task bên trong lock của auctionId cụ thể.
     */
    public <T> T executeLocked(long auctionId, Supplier<T> task) {
        ReentrantLock lock = registry.getLock(auctionId);
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thực thi một runnable task không có giá trị trả về bên trong lock.
     */
    public void executeLocked(long auctionId, Runnable task) {
        executeLocked(auctionId, () -> {
            task.run();
            return null;
        });
    }
}
