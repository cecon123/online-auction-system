package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionManagerTest {

  private SessionManager sessionManager;

  @BeforeEach
  void setUp() throws Exception {
    sessionManager = SessionManager.getInstance();
    activeSessions().clear();
  }

  @AfterEach
  void tearDown() throws Exception {
    activeSessions().clear();
  }

  @Test
  void getUserId_shouldRemoveExpiredSession_whenTokenExpired() throws Exception {
    String token = sessionManager.createSession(42L);
    activeSessions().put(token, session(42L, Instant.now().minusSeconds(1)));

    Long userId = sessionManager.getUserId(token);

    assertNull(userId);
    assertFalse(activeSessions().containsKey(token));
  }

  @Test
  void invalidateSession_shouldRemoveActiveToken() throws Exception {
    String token = sessionManager.createSession(42L);

    sessionManager.invalidateSession(token);

    assertNull(sessionManager.getUserId(token));
    assertEquals(0, activeSessions().size());
  }

  private Object session(long userId, Instant expiresAt) throws Exception {
    Class<?> sessionClass = Class.forName("com.auction.server.service.SessionManager$Session");
    Constructor<?> constructor = sessionClass.getDeclaredConstructor(long.class, Instant.class);
    constructor.setAccessible(true);
    return constructor.newInstance(userId, expiresAt);
  }

  @SuppressWarnings("unchecked")
  private ConcurrentHashMap<String, Object> activeSessions() throws Exception {
    Field field = SessionManager.class.getDeclaredField("activeSessions");
    field.setAccessible(true);
    return (ConcurrentHashMap<String, Object>) field.get(sessionManager);
  }
}
