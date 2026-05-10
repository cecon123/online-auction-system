package com.auction.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads server configuration from application.properties.
 *
 * This keeps runtime configuration such as server port and database URL
 * outside Java source code.
 */
public final class AppProperties {
    private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);
    private static final String CONFIG_FILE = "/application.properties";
    private static final AppProperties INSTANCE = new AppProperties();

    private final Properties properties;

    private AppProperties() {
        this.properties = new Properties();
        load();
    }

    public static AppProperties getInstance() {
        return INSTANCE;
    }

    public int getServerPort() {
        return getInt("server.port", 8080);
    }

    public String getDatabaseUrl() {
        return getString("database.url", "jdbc:sqlite:auction.db");
    }

    public boolean isWalEnabled() {
        return getBoolean("database.enableWal", true);
    }

    public int getBusyTimeoutMs() {
        return getInt("database.busyTimeoutMs", 5000);
    }

    public int getAssetPort() {
        return getInt("server.asset.port", 8081);
    }

    public String getAssetDir() {
        return getString("server.asset.dir", "uploads");
    }

    private void load() {
        try (InputStream inputStream = AppProperties.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                logger.warn("application.properties not found. Using default configuration.");
                return;
            }

            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load application.properties", e);
        }
    }

    private String getString(String key, String defaultValue) {
        String value = System.getProperty(key);

        if (value != null && !value.isBlank()) {
            return value;
        }

        return properties.getProperty(key, defaultValue);
    }

    private int getInt(String key, int defaultValue) {
        String value = getString(key, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}
