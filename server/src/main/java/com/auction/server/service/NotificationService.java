package com.auction.server.service;

import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Response;
import com.auction.server.util.JsonMapper;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Singleton service for handling real-time notifications and auction subscriptions. */
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private static final NotificationService INSTANCE = new NotificationService();

  // Map of auctionId -> set of PrintWriters for subscribed clients
  private final Map<Long, Set<PrintWriter>> subscriptions = new ConcurrentHashMap<>();

  // Map of userId -> set of PrintWriters for targeted user notifications
  private final Map<Long, Set<PrintWriter>> userConnections = new ConcurrentHashMap<>();

  private final JsonMapper jsonMapper = JsonMapper.getInstance();

  private NotificationService() {}

  public static NotificationService getInstance() {
    return INSTANCE;
  }

  /** Subscribes a client to an auction. */
  public void subscribe(long auctionId, PrintWriter clientWriter) {
    subscriptions.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(clientWriter);
    logger.debug("Client subscribed to auction {}", auctionId);
  }

  /** Unsubscribes a client from an auction. */
  public void unsubscribe(long auctionId, PrintWriter clientWriter) {
    Set<PrintWriter> clients = subscriptions.get(auctionId);
    if (clients != null) {
      clients.remove(clientWriter);
      if (clients.isEmpty()) {
        subscriptions.remove(auctionId);
      }
    }
    logger.debug("Client unsubscribed from auction {}", auctionId);
  }

  /** Unsubscribes a client from all auctions (e.g., when they disconnect). */
  public void unsubscribeFromAll(PrintWriter clientWriter) {
    subscriptions.entrySet().removeIf(entry -> removeWriter(entry.getValue(), clientWriter));
    unregisterUserConnection(clientWriter);
  }

  /** Registers a client's socket connection to a specific user ID after login. */
  public void registerUserConnection(long userId, PrintWriter clientWriter) {
    userConnections.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(clientWriter);
    logger.debug("Registered connection for user {}", userId);
  }

  /** Removes a client's socket connection from any user ID mapping upon disconnect. */
  public void unregisterUserConnection(PrintWriter clientWriter) {
    userConnections.entrySet().removeIf(entry -> removeWriter(entry.getValue(), clientWriter));
  }

  /** Sends a direct notification to all active connections of a specific user. */
  public void notifyUser(long userId, MessageType type, Object data) {
    Set<PrintWriter> clients = userConnections.getOrDefault(userId, Collections.emptySet());

    if (clients.isEmpty()) {
      return;
    }

    Response<?> response =
        Response.ok(type, "sys-notify-" + System.currentTimeMillis(), "System notification", data);
    String json = jsonMapper.toJson(response);

    logger.info("Sending {} to User {}", type, userId);

    clients.forEach(
        writer -> {
          try {
            sendOrDrop(clients, writer, json);
          } catch (Exception e) {
            logger.error("Failed to send notification to User {}", userId, e);
            clients.remove(writer);
          }
        });
  }

  /** Broadcasts a message to all connected users. */
  public void broadcastToAllUsers(MessageType type, Object data) {
    Response<?> response =
        Response.ok(type, "sys-notify-" + System.currentTimeMillis(), "Global broadcast", data);
    String json = jsonMapper.toJson(response);

    logger.info("Broadcasting {} to all connected users", type);

    userConnections.values().stream()
        .flatMap(Set::stream)
        .distinct()
        .forEach(
            writer -> {
              try {
                sendOrDrop(userConnections.values().stream()
                    .filter(clients -> clients.contains(writer))
                    .findFirst()
                    .orElse(Collections.emptySet()), writer, json);
              } catch (Exception e) {
                logger.error("Failed to send broadcast to a user", e);
                unregisterUserConnection(writer);
              }
            });
  }

  /** Broadcasts a message to all clients subscribed to a specific auction. */
  public void broadcast(long auctionId, MessageType type, Object data) {
    Set<PrintWriter> clients = subscriptions.getOrDefault(auctionId, Collections.emptySet());

    if (clients.isEmpty()) {
      return;
    }

    Response<?> response =
        Response.ok(type, "event-" + System.currentTimeMillis(), "Realtime update", data);
    String json = jsonMapper.toJson(response);

    logger.info(
        "Broadcasting {} update for auction {} to {} clients", type, auctionId, clients.size());

    clients.forEach(
        writer -> {
          try {
            sendOrDrop(clients, writer, json);
          } catch (Exception e) {
            logger.error("Failed to send broadcast to a client", e);
            clients.remove(writer);
          }
        });
  }

  private void sendOrDrop(Set<PrintWriter> clients, PrintWriter writer, String json) {
    synchronized (writer) {
      writer.println(json);
      if (writer.checkError()) {
        clients.remove(writer);
        unregisterUserConnection(writer);
      }
    }
  }

  private boolean removeWriter(Set<PrintWriter> clients, PrintWriter writer) {
    clients.remove(writer);
    return clients.isEmpty();
  }
}
