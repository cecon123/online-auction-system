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

  // Credentials for silent re-authentication
  private String lastUsername;
  private String lastPassword;
  private Runnable onReconnect;

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

  private int reconnectAttempts = 0;
  private static final int MAX_RECONNECT_ATTEMPTS = 5;
  private static final long RECONNECT_DELAY_MS = 3000;

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
    if (connected && onReconnect != null) {
      onReconnect.run();
    }
    for (Consumer<Boolean> listener : connectionListeners) {
      try {
        listener.accept(connected);
      } catch (Exception e) {
        logger.error("Error in connection listener", e);
      }
    }
  }

  public void setCredentials(String username, String password) {
    this.lastUsername = username;
    this.lastPassword = password;
  }

  public void setOnReconnect(Runnable onReconnect) {
    this.onReconnect = onReconnect;
  }

  public String getLastUsername() {
    return lastUsername;
  }

  public String getLastPassword() {
    return lastPassword;
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
      reconnectAttempts = 0;
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
    logger.debug("Sending request: {}", json);
    out.println(json);

    // Cast to specific return type for the caller
    return future.thenApply(response -> (Response<R>) response);
  }

  public boolean isConnected() {
    return running && socket != null && socket.isConnected() && out != null;
  }

  /** Listener loop that reads messages from the server. */
  private void listen() {
    Socket currentSocket = this.socket;
    try {
      String line;
      while (running && (line = in.readLine()) != null) {
        logger.debug("Received message: {}", line);
        handleMessage(line);
      }
    } catch (IOException e) {
      if (running) {
        logger.error("Connection lost: {}", e.getMessage());
        notifyConnectionStatus(false);
        scheduleReconnection();
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

  private void scheduleReconnection() {
    if (!running) return;

    Thread reconnectThread =
        new Thread(
            () -> {
              while (running && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                updateConnectionState(ConnectionState.RECONNECTING);
                logger.info(
                    "Attempting to reconnect ({}/{})...",
                    reconnectAttempts,
                    MAX_RECONNECT_ATTEMPTS);
                try {
                  Thread.sleep(RECONNECT_DELAY_MS);
                  connect();
                  return; // Success, exit thread
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  break;
                } catch (IOException e) {
                  logger.error("Reconnection attempt failed: {}", e.getMessage());
                }
              }

              if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                logger.error("Max reconnection attempts reached. Giving up.");
                running = false;
                updateConnectionState(ConnectionState.DISCONNECTED);
              }
            },
            "ReconnectThread");
    reconnectThread.setDaemon(true);
    reconnectThread.start();
  }

  private void handleMessage(String json) {
    try {
      Response<?> response = JsonMapper.getInstance().fromJson(json, Response.class);
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
      logger.error("Failed to parse or handle message: {}", json, e);
    }
  }

  private void handleEvent(Response<?> event) {
    logger.debug("Dispatching realtime event: {}", event.getType());
    List<Consumer<Response<?>>> listeners = eventListeners.get(event.getType());
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
}
