package com.auction.server.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Initializes the SQLite database schema from db/schema.sql.
 *
 * This should be called when the server starts.
 */
public final class SchemaInitializer {
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private SchemaInitializer() {
    }

    public static void initialize() {
        String schemaSql = loadSchemaSql();

        try (Connection connection = Database.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            for (String sqlStatement : splitSqlStatements(schemaSql)) {
                if (!sqlStatement.isBlank()) {
                    statement.execute(sqlStatement);
                }
            }

            System.out.println("Database schema initialized.");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database schema", e);
        }
    }

    private static String loadSchemaSql() {
        try (InputStream inputStream = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Schema file not found: " + SCHEMA_RESOURCE);
            }

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read schema file", e);
        }
    }

    private static String[] splitSqlStatements(String sql) {
        String sqlWithoutComments = sql.lines()
            .map(String::trim)
            .filter(line -> !line.startsWith("--"))
            .collect(Collectors.joining("\n"));

        return sqlWithoutComments.split(";");
    }
}
