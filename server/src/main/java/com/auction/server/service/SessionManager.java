package com.auction.server.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user sessions.
 * Issues and validates tokens after successful login.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    // Map: Token -> UserID
    private final ConcurrentHashMap<String, Long> activeSessions =
        new ConcurrentHashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new session for a user.
     * @param userId The ID of the logged-in user.
     * @return Session token (UUID string).
     */
    public String createSession(long userId) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, userId);
        return token;
    }

    /**
     * Retrieves UserID from a token.
     * @param token The token sent from the client.
     * @return UserID if valid, null if session not found.
     */
    public Long getUserId(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    /**
     * Invalidates a session (Logout).
     * @param token The token to invalidate.
     */
    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}
