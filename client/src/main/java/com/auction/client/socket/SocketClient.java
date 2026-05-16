package com.auction.client.socket;

import com.auction.client.util.JsonMapper;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton SocketClient for managing TCP connection with the server. Handles asynchronous
 * communication using CompletableFuture.
 */
public final class SocketClient {

  private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
  private static final SocketClient INSTANCE = new SocketClient();

  private String host = "localhost";
  private int port = 8080;
  private String token;

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private Thread listenerThread;
  private volatile boolean running;

  // Maps requestId to its pending CompletableFuture
  private final Map<String, CompletableFuture<Response<?>>> pendingRequests =
      new ConcurrentHashMap<>();

  // Listeners for realtime events (requestId is null)
  private final Map<MessageType, List<Consumer<Response<?>>>> eventListeners =
      new ConcurrentHashMap<>();

  private final ObjectProperty<ConnectionState> connectionState =
      new SimpleObjectProperty<>(ConnectionState.DISCONNECTED);

  public ObjectProperty<ConnectionState> connectionStateProperty() {
    return connectionState;
  }

  public ConnectionState getConnectionState() {
    return connectionState.get();
  }

  // Listeners for connection status
  private final List<Consumer<Boolean>> connectionListeners = new CopyOnWriteArrayList<>();

  private SocketClient() {}

  public static SocketClient getInstance() {
    return INSTANCE;
  }

  /** Registers a listener for real-time events from the server. */
  public void addEventListener(MessageType type, Consumer<Response<?>> listener) {
    eventListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
  }

  /** Unregisters a listener for real-time events. */
  public void removeEventListener(MessageType type, Consumer<Response<?>> listener) {
    List<Consumer<Response<?>>> listeners = eventListeners.get(type);
    if (listeners != null) {
      listeners.remove(listener);
    }
  }

  public void addConnectionListener(Consumer<Boolean> listener) {
    connectionListeners.add(listener);
  }

  public void removeConnectionListener(Consumer<Boolean> listener) {
    connectionListeners.remove(listener);
  }

  private void notifyConnectionStatus(boolean connected) {
    for (Consumer<Boolean> listener : connectionListeners) {
      try {
        listener.accept(connected);
      } catch (Exception e) {
        logger.error("Error in connection listener", e);
      }
    }
  }

  /** Connects to the server and starts the listener thread. */
  public synchronized void connect() throws IOException {
    if (getConnectionState() == ConnectionState.CONNECTED) return;

    updateConnectionState(ConnectionState.CONNECTING);
    logger.info("Connecting to server at {}:{}", host, port);
    try {
      socket = new Socket(host, port);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      running = true;
      updateConnectionState(ConnectionState.CONNECTED);

      listenerThread = new Thread(this::listen, "SocketListener");
      listenerThread.setDaemon(true);
      listenerThread.start();

      notifyConnectionStatus(true);
      logger.info("Connected and listener thread started.");
    } catch (IOException e) {
      updateConnectionState(ConnectionState.DISCONNECTED);
      notifyConnectionStatus(false);
      throw e;
    }
  }

  private void updateConnectionState(ConnectionState state) {
    if (Platform.isFxApplicationThread()) {
      connectionState.set(state);
    } else {
      Platform.runLater(() -> connectionState.set(state));
    }
  }

  /** Closes the connection. */
  public synchronized void disconnect() {
    boolean wasRunning = running;
    running = false;
    token = null;
    failPendingRequests(new IOException("Disconnected from server"));
    try {
      if (socket != null) socket.close();
      if (listenerThread != null) listenerThread.interrupt();
    } catch (IOException e) {
      logger.error("Error while disconnecting", e);
    }
    updateConnectionState(ConnectionState.DISCONNECTED);
    if (wasRunning) {
      notifyConnectionStatus(false);
    }
    logger.info("Disconnected from server.");
  }

  /** Sends a request and returns a CompletableFuture for the response. */
  public <T, R> CompletableFuture<Response<R>> sendRequest(Request<T> request) {
    if (!isConnected()) {
      return CompletableFuture.failedFuture(new IOException("Not connected to server"));
    }

    String requestId = UUID.randomUUID().toString();
    request.setRequestId(requestId);
    request.setToken(this.token);

    CompletableFuture<Response<?>> future = new CompletableFuture<>();
    pendingRequests.put(requestId, future);

    String json = JsonMapper.getInstance().toJson(request);
    logger.debug("TX {}", summarizeRequest(request));
    out.println(json);
    if (out.checkError()) {
      pendingRequests.remove(requestId);
      return CompletableFuture.failedFuture(new IOException("Failed to send request to server"));
    }

    // Cast to specific return type for the caller
    return future.thenApply(response -> (Response<R>) response);
  }

  public boolean isConnected() {
    return running && socket != null && socket.isConnected() && !socket.isClosed() && out != null;
  }

  /** Listener loop that reads messages from the server. */
  private void listen() {
    Socket currentSocket = this.socket;
    try {
      String line;
      while (running && (line = in.readLine()) != null) {
        handleMessage(line);
      }
      if (running) {
        logger.warn("Connection closed by server.");
        handleConnectionLoss(new IOException("Connection closed by server"));
      }
    } catch (IOException e) {
      if (running) {
        logger.error("Connection lost: {}", e.getMessage());
        handleConnectionLoss(e);
      }
    } finally {
      try {
        if (currentSocket != null && !currentSocket.isClosed()) {
          currentSocket.close();
        }
      } catch (IOException e) {
        logger.error("Error closing socket", e);
      }
    }
  }

  private synchronized void handleConnectionLoss(IOException cause) {
    running = false;
    token = null;
    failPendingRequests(cause);
    updateConnectionState(ConnectionState.DISCONNECTED);
    notifyConnectionStatus(false);
  }

  private void failPendingRequests(IOException cause) {
    pendingRequests.forEach((id, future) -> future.completeExceptionally(cause));
    pendingRequests.clear();
  }

  private void handleMessage(String json) {
    try {
      Response<?> response = JsonMapper.getInstance().fromJson(json, Response.class);
      logger.debug("RX {}", summarizeResponse(response));
      String requestId = response.getRequestId();

      // Realtime events from server often have requestId starting with 'event-'
      // or 'sys-notify-', or no requestId at all.
      if (requestId != null
          && !requestId.startsWith("event-")
          && !requestId.startsWith("sys-notify-")) {
        CompletableFuture<Response<?>> future = pendingRequests.remove(requestId);
        if (future != null) {
          future.complete(response);
        } else {
          logger.warn("Received response for unknown requestId: {}", requestId);
        }
      } else {
        // Handle realtime events (server pushes)
        handleEvent(response);
      }
    } catch (Exception e) {
      logger.error("Failed to parse or handle message: {}", abbreviate(json, 240), e);
    }
  }

  private void handleEvent(Response<?> event) {
    List<Consumer<Response<?>>> listeners = eventListeners.get(event.getType());
    logger.debug(
        "Event {} id={} listeners={}",
        event.getType(),
        shortId(event.getRequestId()),
        listeners == null ? 0 : listeners.size());
    if (listeners != null) {
      for (Consumer<Response<?>> listener : listeners) {
        try {
          Platform.runLater(() -> listener.accept(event));
        } catch (Exception e) {
          logger.error("Error in event listener for {}", event.getType(), e);
        }
      }
    }
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  private String summarizeRequest(Request<?> request) {
    return String.format(
        "%s id=%s token=%s data=%s",
        request.getType(),
        shortId(request.getRequestId()),
        request.getToken() == null ? "none" : "set",
        payloadName(request.getData()));
  }

  private String summarizeResponse(Response<?> response) {
    return String.format(
        "%s id=%s ok=%s msg=\"%s\" data=%s",
        response.getType(),
        shortId(response.getRequestId()),
        response.isSuccess(),
        abbreviate(response.getMessage(), 80),
        payloadName(response.getData()));
  }

  private String payloadName(Object payload) {
    if (payload == null) {
      return "none";
    }
    return payload.getClass().getSimpleName();
  }

  private String shortId(String requestId) {
    if (requestId == null || requestId.isBlank()) {
      return "none";
    }
    return requestId.length() <= 12 ? requestId : requestId.substring(0, 8);
  }

  private String abbreviate(String value, int maxLength) {
    if (value == null) {
      return "";
    }
    return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
  }
}
