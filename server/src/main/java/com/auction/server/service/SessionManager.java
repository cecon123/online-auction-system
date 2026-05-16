package com.auction.server.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Manages user sessions. Issues and validates tokens after successful login. */
public class SessionManager {

  private static final SessionManager INSTANCE = new SessionManager();

  private static final Duration SESSION_TTL = Duration.ofHours(2);

  // Map: Token -> Session
  private final ConcurrentHashMap<String, Session> activeSessions = new ConcurrentHashMap<>();

  private SessionManager() {}

  public static SessionManager getInstance() {
    return INSTANCE;
  }

  /**
   * Creates a new session for a user.
   *
   * @param userId The ID of the logged-in user.
   * @return Session token (UUID string).
   */
  public String createSession(long userId) {
    String token = UUID.randomUUID().toString();
    activeSessions.put(token, new Session(userId, Instant.now().plus(SESSION_TTL)));
    return token;
  }

  /**
   * Retrieves UserID from a token.
   *
   * @param token The token sent from the client.
   * @return UserID if valid, null if session not found.
   */
  public Long getUserId(String token) {
    if (token == null) return null;
    Session session = activeSessions.get(token);
    if (session == null) {
      return null;
    }
    if (Instant.now().isAfter(session.expiresAt())) {
      activeSessions.remove(token, session);
      return null;
    }
    return session.userId();
  }

  /**
   * Invalidates a session (Logout).
   *
   * @param token The token to invalidate.
   */
  public void invalidateSession(String token) {
    if (token != null) {
      activeSessions.remove(token);
    }
  }

  private record Session(long userId, Instant expiresAt) {}
}
