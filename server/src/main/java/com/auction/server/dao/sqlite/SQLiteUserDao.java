package com.auction.server.dao.sqlite;

import com.auction.common.enums.Role;
import com.auction.server.dao.Database;
import com.auction.server.dao.UserDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLite implementation of UserDao.
 */
public class SQLiteUserDao implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(
        SQLiteUserDao.class
    );

    private final Database database;

    public SQLiteUserDao() {
        this.database = Database.getInstance();
    }

    @Override
    public long create(
        String username,
        String passwordHash,
        String fullName,
        Role role,
        java.math.BigDecimal balance
    ) {
        String sql = """
            INSERT INTO users(username, password_hash, full_name, role, balance, active, created_at)
            VALUES (?, ?, ?, ?, ?, 1, ?)
            """;

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            statement.setString(5, balance.toPlainString());
            statement.setString(6, LocalDateTime.now().toString());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }

            throw new SQLException(
                "Creating user failed, no generated ID returned."
            );
        } catch (SQLException e) {
            logger.error("Database error during create user: {}", username, e);
            throw new IllegalStateException(
                "Could not create user: " + username,
                e
            );
        }
    }

    @Override
    public Optional<UserRecord> findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, full_name, role, balance, active, created_at
            FROM users
            WHERE username = ?
            """;

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Database error during findByUsername: {}", username, e);
            throw new IllegalStateException(
                "Could not find user by username: " + username,
                e
            );
        }
    }

    @Override
    public Optional<UserRecord> findById(long id) {
        String sql = """
            SELECT id, username, password_hash, full_name, role, balance, active, created_at
            FROM users
            WHERE id = ?
            """;

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Database error during findById: {}", id, e);
            throw new IllegalStateException(
                "Could not find user by id: " + id,
                e
            );
        }
    }

    @Override
    public List<UserRecord> findAll() {
        String sql = """
            SELECT id, username, password_hash, full_name, role, balance, active, created_at
            FROM users
            ORDER BY id ASC
            """;

        List<UserRecord> users = new ArrayList<>();

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }

            return users;
        } catch (SQLException e) {
            logger.error("Database error during findAll", e);
            throw new IllegalStateException("Could not find all users", e);
        }
    }

    @Override
    public void updateActiveStatus(long userId, boolean active) {
        String sql = """
            UPDATE users
            SET active = ?
            WHERE id = ?
            """;

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, active ? 1 : 0);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error during updateActiveStatus: {}", userId, e);
            throw new IllegalStateException(
                "Could not update user active status: " + userId,
                e
            );
        }
    }

    @Override
    public void updateBalance(long userId, java.math.BigDecimal balance) {
        String sql = """
            UPDATE users
            SET balance = ?
            WHERE id = ?
            """;

        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, balance.toPlainString());
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error during updateBalance: {}", userId, e);
            throw new IllegalStateException(
                "Could not update user balance: " + userId,
                e
            );
        }
    }

    private UserRecord mapRow(ResultSet resultSet) throws SQLException {
        return new UserRecord(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("password_hash"),
            resultSet.getString("full_name"),
            Role.valueOf(resultSet.getString("role")),
            new java.math.BigDecimal(resultSet.getString("balance")),
            resultSet.getInt("active") == 1,
            LocalDateTime.parse(resultSet.getString("created_at"))
        );
    }
}
