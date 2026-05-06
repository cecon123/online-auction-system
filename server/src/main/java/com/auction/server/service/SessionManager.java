package com.auction.server.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý phiên làm việc (Session) của người dùng.
 * Cấp và xác thực token sau khi login thành công.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    // Map: Token -> UserID
    private final ConcurrentHashMap<String, Long> activeSessions = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Tạo session mới cho người dùng.
     * @param userId ID của người dùng vừa login.
     * @return Session token (UUID string).
     */
    public String createSession(long userId) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, userId);
        return token;
    }

    /**
     * Lấy UserID từ token.
     * @param token Token được gửi từ client.
     * @return UserID nếu hợp lệ, null nếu không tìm thấy session.
     */
    public Long getUserId(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    /**
     * Hủy session (Logout).
     * @param token Token cần hủy.
     */
    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}
