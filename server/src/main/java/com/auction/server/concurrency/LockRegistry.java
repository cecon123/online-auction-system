package com.auction.server.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry quản lý và tái sử dụng ReentrantLock theo auctionId.
 */
public class LockRegistry {

    private final ConcurrentHashMap<Long, ReentrantLock> locks =
        new ConcurrentHashMap<>();

    /**
     * Lấy lock cho một ID cụ thể. Nếu chưa có, lock mới sẽ được tạo.
     */
    public ReentrantLock getLock(long id) {
        return locks.computeIfAbsent(id, k -> new ReentrantLock());
    }

    /**
     * Xóa lock khi không còn cần thiết.
     */
    public void removeLock(long id) {
        locks.remove(id);
    }
}
