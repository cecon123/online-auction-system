package com.auction.server.dao;

import com.auction.server.config.AppProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton responsible for creating SQLite connections.
 *
 * Only the server module is allowed to use this class.
 * The client module must never connect to SQLite directly.
 */
public final class Database {
    private static final Database INSTANCE = new Database();

    private final AppProperties appProperties;

    private Database() {
        this.appProperties = AppProperties.getInstance();
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(getDatabaseUrl());
        configureConnection(connection);
        return connection;
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
}
