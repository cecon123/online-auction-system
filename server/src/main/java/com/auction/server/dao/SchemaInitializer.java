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
    private static final String SEED_RESOURCE = "/db/seed.sql";

    private SchemaInitializer() {}

    public static void initialize() {
        String schemaSql = loadSql(SCHEMA_RESOURCE);

        try (
            Connection connection = Database.getInstance().getConnection();
            Statement statement = connection.createStatement()
        ) {
            for (String sqlStatement : splitSqlStatements(schemaSql)) {
                if (!sqlStatement.isBlank()) {
                    statement.execute(sqlStatement);
                }
            }

            System.out.println("Database schema initialized.");

            // Check if we should seed the database
            boolean skipSeed = Boolean.getBoolean("auction.skip.seed");
            if (!skipSeed && isDatabaseEmpty(connection)) {
                System.out.println("Database is empty. Loading seed data...");
                String seedSql = loadSql(SEED_RESOURCE);
                for (String sqlStatement : splitSqlStatements(seedSql)) {
                    if (!sqlStatement.isBlank()) {
                        statement.execute(sqlStatement);
                    }
                }
                System.out.println("Seed data loaded successfully.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Could not initialize database schema",
                e
            );
        }
    }

    private static boolean isDatabaseEmpty(Connection connection)
        throws SQLException {
        try (
            Statement stmt = connection.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users")
        ) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            // Table might not exist yet or other error
            return true;
        }
    }

    private static String loadSql(String resourcePath) {
        try (
            InputStream inputStream =
                SchemaInitializer.class.getResourceAsStream(resourcePath)
        ) {
            if (inputStream == null) {
                throw new IllegalStateException(
                    "SQL file not found: " + resourcePath
                );
            }

            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                )
            ) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not read SQL file: " + resourcePath,
                e
            );
        }
    }

    private static String[] splitSqlStatements(String sql) {
        String sqlWithoutComments = sql
            .lines()
            .map(String::trim)
            .filter(line -> !line.startsWith("--"))
            .collect(Collectors.joining("\n"));

        return sqlWithoutComments.split(";");
    }
}
