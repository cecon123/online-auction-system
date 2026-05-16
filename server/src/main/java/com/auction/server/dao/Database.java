package com.auction.server.dao;

import com.auction.server.config.AppProperties;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton responsible for creating SQLite connections.
 *
 * <p>Only the server module is allowed to use this class. The client module must never connect to
 * SQLite directly.
 */
public final class Database {
  private static final Database INSTANCE = new Database();

  private final AppProperties appProperties;
  private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

  private Database() {
    this.appProperties = AppProperties.getInstance();
  }

  public static Database getInstance() {
    return INSTANCE;
  }

  public Connection getConnection() throws SQLException {
    Connection activeConnection = transactionConnection.get();
    if (activeConnection != null) {
      return closeShield(activeConnection);
    }

    Connection connection = DriverManager.getConnection(getDatabaseUrl());
    configureConnection(connection);
    return connection;
  }

  public <T> T runInTransaction(TransactionWork<T> work) {
    if (transactionConnection.get() != null) {
      try {
        return work.execute();
      } catch (Exception e) {
        if (e instanceof RuntimeException runtimeException) {
          throw runtimeException;
        }
        throw new IllegalStateException("Database transaction failed", e);
      }
    }

    try (Connection connection = DriverManager.getConnection(getDatabaseUrl())) {
      configureConnection(connection);
      connection.setAutoCommit(false);
      transactionConnection.set(connection);
      try {
        T result = work.execute();
        connection.commit();
        return result;
      } catch (Exception e) {
        connection.rollback();
        if (e instanceof RuntimeException runtimeException) {
          throw runtimeException;
        }
        throw new IllegalStateException("Database transaction failed", e);
      } finally {
        transactionConnection.remove();
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Could not run database transaction", e);
    }
  }

  private String getDatabaseUrl() {
    String overrideUrl = System.getProperty("auction.db.url");

    if (overrideUrl != null && !overrideUrl.isBlank()) {
      return overrideUrl;
    }

    return appProperties.getDatabaseUrl();
  }

  private void configureConnection(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON");
      statement.execute("PRAGMA busy_timeout = " + appProperties.getBusyTimeoutMs());

      if (appProperties.isWalEnabled()) {
        statement.execute("PRAGMA journal_mode = WAL");
      }
    }
  }

  private Connection closeShield(Connection connection) {
    InvocationHandler handler =
        (proxy, method, args) -> {
          if ("close".equals(method.getName())) {
            return null;
          }
          try {
            return method.invoke(connection, args);
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        };

    return (Connection)
        Proxy.newProxyInstance(
            Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, handler);
  }

  @FunctionalInterface
  public interface TransactionWork<T> {
    T execute() throws Exception;
  }
}
