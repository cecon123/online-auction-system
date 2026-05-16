package com.auction.client.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.client.util.JsonMapper;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SocketClientIntegrationTest {

  private final SocketClient client = SocketClient.getInstance();
  private final JsonMapper jsonMapper = JsonMapper.getInstance();

  @BeforeAll
  static void startJavaFx() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException e) {
      latch.countDown();
    }
    assertTrue(latch.await(2, TimeUnit.SECONDS));
  }

  @BeforeEach
  void resetClient() throws Exception {
    client.disconnect();
    setField("host", "localhost");
    setField("token", null);
    clearMap("pendingRequests");
    clearMap("eventListeners");
    waitUntil(() -> client.getConnectionState() == ConnectionState.DISCONNECTED);
  }

  @AfterEach
  void cleanup() throws Exception {
    client.disconnect();
    clearMap("pendingRequests");
    clearMap("eventListeners");
  }

  @Test
  void sendRequestCompletesFutureForMatchingRequestId() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      setField("port", serverSocket.getLocalPort());
      executor.submit(() -> respondToOneRequest(serverSocket));

      client.connect();
      Response<String> response =
          client
              .<Void, String>sendRequest(
                  new Request<Void>(MessageType.GET_AUCTIONS, null, null, null))
              .get(2, TimeUnit.SECONDS);

      assertTrue(response.isSuccess());
      assertEquals(MessageType.GET_AUCTIONS, response.getType());
      assertEquals("ok", response.getMessage());
      assertEquals("done", response.getData());
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void dispatchesEventResponsesWithEventRequestIdPrefix() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    CountDownLatch eventReceived = new CountDownLatch(1);
    Consumer<Response<?>> listener =
        response -> {
          assertEquals(MessageType.BID_UPDATE, response.getType());
          assertTrue(response.getRequestId().startsWith("event-"));
          eventReceived.countDown();
        };

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      setField("port", serverSocket.getLocalPort());
      executor.submit(() -> sendServerEvent(serverSocket));

      client.addEventListener(MessageType.BID_UPDATE, listener);
      client.connect();

      assertTrue(eventReceived.await(2, TimeUnit.SECONDS));
    } finally {
      client.removeEventListener(MessageType.BID_UPDATE, listener);
      executor.shutdownNow();
    }
  }

  @Test
  void serverCloseMarksDisconnectedFailsPendingRequestsAndClearsToken() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    try (ServerSocket serverSocket = new ServerSocket(0)) {
      setField("port", serverSocket.getLocalPort());
      executor.submit(() -> closeAfterOneRequest(serverSocket));

      client.connect();
      client.setToken("session-token");
      CompletableFuture<Response<Void>> future =
          client.<Void, Void>sendRequest(
              new Request<Void>(MessageType.GET_AUCTIONS, null, null, null));

      waitUntil(future::isCompletedExceptionally);
      waitUntil(() -> client.getConnectionState() == ConnectionState.DISCONNECTED);

      assertTrue(future.isCompletedExceptionally());
      assertEquals(ConnectionState.DISCONNECTED, client.getConnectionState());
      assertNotEquals(ConnectionState.RECONNECTING, client.getConnectionState());
      assertNull(client.getToken());
    } finally {
      executor.shutdownNow();
    }
  }

  private void respondToOneRequest(ServerSocket serverSocket) {
    try (Socket socket = serverSocket.accept();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
      Request<?> request = jsonMapper.fromJson(reader.readLine(), Request.class);
      writer.println(
          jsonMapper.toJson(
              Response.ok(request.getType(), request.getRequestId(), "ok", "done")));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private void sendServerEvent(ServerSocket serverSocket) {
    try (Socket socket = serverSocket.accept();
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
      writer.println(
          jsonMapper.toJson(
              Response.ok(MessageType.BID_UPDATE, "event-test", "Realtime update", null)));
      Thread.sleep(100);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private void closeAfterOneRequest(ServerSocket serverSocket) {
    try (Socket socket = serverSocket.accept();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      reader.readLine();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SocketClient.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(client, value);
  }

  @SuppressWarnings("unchecked")
  private void clearMap(String fieldName) throws Exception {
    Field field = SocketClient.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    ((Map<Object, Object>) field.get(client)).clear();
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

  private interface Condition {
    boolean matches() throws Exception;
  }
}
