package com.auction.server.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.bid.BidUpdateEvent;
import com.auction.common.enums.Role;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.service.NotificationService;
import com.auction.server.util.JsonMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientHandlerIntegrationTest {

  private static final String TEST_DB_FILE = "client_handler_integration_test.db";
  private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

  private final JsonMapper jsonMapper = JsonMapper.getInstance();

  @BeforeAll
  static void initDb() {
    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      dbFile.delete();
    }
    System.setProperty("auction.db.url", TEST_DB_URL);
    System.setProperty("auction.skip.seed", "false");
    SchemaInitializer.initialize();
  }

  @AfterAll
  static void cleanupDb() {
    System.clearProperty("auction.db.url");
    System.clearProperty("auction.skip.seed");
    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      dbFile.delete();
    }
  }

  @AfterEach
  void cleanupNotifications() throws Exception {
    clearNotificationMaps();
  }

  @Test
  void handlesNewlineDelimitedRequestAndBroadcastsSubscribedAuctionEvent() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try (ServerSocket serverSocket = new ServerSocket(0);
        Socket clientSocket = new Socket("localhost", serverSocket.getLocalPort());
        Socket acceptedSocket = serverSocket.accept();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
      Future<?> handlerFuture = executor.submit(new ClientHandler(acceptedSocket));

      writer.println(
          jsonMapper.toJson(
              new Request<>(
                  MessageType.REGISTER,
                  "register-1",
                  null,
                  new RegisterRequest(
                      "Realtime Bidder",
                      "realtime_bidder",
                      "password123",
                      Role.BIDDER))));
      Response<?> registerResponse = readResponse(reader);
      assertTrue(registerResponse.isSuccess(), registerResponse.getMessage());

      writer.println(
          jsonMapper.toJson(
              new Request<>(
                  MessageType.LOGIN,
                  "login-1",
                  null,
                  new LoginRequest("realtime_bidder", "password123"))));
      Response<?> loginResponse = readResponse(reader);
      assertTrue(loginResponse.isSuccess(), loginResponse.getMessage());
      assertEquals(MessageType.LOGIN, loginResponse.getType());

      writer.println(
          jsonMapper.toJson(
              new Request<>(MessageType.SUBSCRIBE_AUCTION, "sub-1", null, 1L)));
      Response<?> subscribeResponse = readResponse(reader);
      assertTrue(subscribeResponse.isSuccess());
      assertEquals(MessageType.SUBSCRIBE_AUCTION, subscribeResponse.getType());

      NotificationService.getInstance()
          .broadcast(
              1L,
              MessageType.BID_UPDATE,
              new BidUpdateEvent(
                  1L,
                  "bidder01",
                  new BigDecimal("13000.00"),
                  LocalDateTime.now(),
                  LocalDateTime.now().plusMinutes(5)));

      Response<?> event = readResponse(reader);
      assertTrue(event.isSuccess());
      assertEquals(MessageType.BID_UPDATE, event.getType());
      assertTrue(event.getRequestId().startsWith("event-"));

      clientSocket.close();
      handlerFuture.get(2, TimeUnit.SECONDS);
      waitUntil(this::notificationMapsAreEmpty);
      assertTrue(notificationMapsAreEmpty());
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void unsubscribesClientWhenSocketDisconnects() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try (ServerSocket serverSocket = new ServerSocket(0);
        Socket clientSocket = new Socket("localhost", serverSocket.getLocalPort());
        Socket acceptedSocket = serverSocket.accept();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
      Future<?> handlerFuture = executor.submit(new ClientHandler(acceptedSocket));

      writer.println(
          jsonMapper.toJson(
              new Request<>(MessageType.SUBSCRIBE_AUCTION, "sub-2", null, 1L)));
      assertTrue(readResponse(reader).isSuccess());
      assertFalse(notificationMapsAreEmpty());

      clientSocket.close();
      handlerFuture.get(2, TimeUnit.SECONDS);
      waitUntil(this::notificationMapsAreEmpty);

      assertTrue(notificationMapsAreEmpty());
    } finally {
      executor.shutdownNow();
    }
  }

  private Response<?> readResponse(BufferedReader reader) throws Exception {
    String line = reader.readLine();
    return jsonMapper.fromJson(line, Response.class);
  }

  private void waitUntil(Condition condition) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
    while (System.nanoTime() < deadline) {
      if (condition.matches()) {
        return;
      }
      Thread.sleep(25);
    }
  }

  private boolean notificationMapsAreEmpty() throws Exception {
    NotificationService service = NotificationService.getInstance();
    return getMap(service, "subscriptions").isEmpty()
        && getMap(service, "userConnections").isEmpty();
  }

  private void clearNotificationMaps() throws Exception {
    NotificationService service = NotificationService.getInstance();
    getMap(service, "subscriptions").clear();
    getMap(service, "userConnections").clear();
  }

  @SuppressWarnings("unchecked")
  private Map<Object, Object> getMap(NotificationService service, String fieldName)
      throws Exception {
    Field field = NotificationService.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return (Map<Object, Object>) field.get(service);
  }

  private interface Condition {
    boolean matches() throws Exception;
  }
}
