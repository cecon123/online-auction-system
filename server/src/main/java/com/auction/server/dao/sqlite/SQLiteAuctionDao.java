package com.auction.server.dao.sqlite;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of AuctionDao.
 */
public class SQLiteAuctionDao implements AuctionDao {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteAuctionDao.class);
    private final Database database;

    public SQLiteAuctionDao() {
        this.database = Database.getInstance();
    }

    @Override
    public long create(Auction auction) {
        String sql = """
                INSERT INTO auctions (
                    item_id, seller_id, current_price, highest_bidder_id,
                    start_time, end_time, status, version, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, auction.getItemId());
            statement.setLong(2, auction.getSellerId());
            statement.setString(3, auction.getCurrentPrice().toString());
            if (auction.getHighestBidderId() != null) {
                statement.setLong(4, auction.getHighestBidderId());
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
            }
            statement.setString(5, auction.getStartTime().toString());
            statement.setString(6, auction.getEndTime().toString());
            statement.setString(7, auction.getStatus().name());
            statement.setLong(8, auction.getVersion());
            statement.setString(9, LocalDateTime.now().toString());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }

            throw new SQLException("Creating auction failed, no generated ID returned.");
        } catch (SQLException e) {
            logger.error("Database error during create auction for item: {}", auction.getItemId(), e);
            throw new IllegalStateException("Could not create auction for item: " + auction.getItemId(), e);
        }
    }

    @Override
    public Optional<Auction> findById(long id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Database error during findById: {}", id, e);
            throw new IllegalStateException("Could not find auction by id: " + id, e);
        }
    }

    @Override
    public Optional<Auction> findByItemId(long itemId) {
        String sql = "SELECT * FROM auctions WHERE item_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRow(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Database error during findByItemId: {}", itemId, e);
            throw new IllegalStateException("Could not find auction by item_id: " + itemId, e);
        }
    }

    @Override
    public List<Auction> findAll() {
        String sql = "SELECT * FROM auctions ORDER BY created_at DESC";
        List<Auction> auctions = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                auctions.add(mapRow(resultSet));
            }
            return auctions;
        } catch (SQLException e) {
            logger.error("Database error during findAll auctions", e);
            throw new IllegalStateException("Could not find all auctions", e);
        }
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY end_time ASC";
        List<Auction> auctions = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    auctions.add(mapRow(resultSet));
                }
            }
            return auctions;
        } catch (SQLException e) {
            logger.error("Database error during findByStatus: {}", status, e);
            throw new IllegalStateException("Could not find auctions by status: " + status, e);
        }
    }

    @Override
    public void update(Auction auction) {
        String sql = """
                UPDATE auctions
                SET current_price = ?, highest_bidder_id = ?, start_time = ?, end_time = ?, status = ?, version = ?
                WHERE id = ? AND version = ?
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            long oldVersion = auction.getVersion();
            auction.increaseVersion(); // This is a bit tricky if we want to follow the auction object state.
            // Actually, the model has increaseVersion() method.

            statement.setString(1, auction.getCurrentPrice().toString());
            if (auction.getHighestBidderId() != null) {
                statement.setLong(2, auction.getHighestBidderId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            statement.setString(3, auction.getStartTime().toString());
            statement.setString(4, auction.getEndTime().toString());
            statement.setString(5, auction.getStatus().name());
            statement.setLong(6, auction.getVersion());
            statement.setLong(7, auction.getId());
            statement.setLong(8, oldVersion);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                // Revert version if update failed
                // Actually, it's better to just throw and let the caller handle it.
                throw new IllegalStateException("Optimistic locking failure for Auction ID: " + auction.getId());
            }
        } catch (SQLException e) {
            logger.error("Database error during update auction: {}", auction.getId(), e);
            throw new IllegalStateException("Could not update auction: " + auction.getId(), e);
        }
    }

    private Auction mapRow(ResultSet rs) throws SQLException {
        long bidderId = rs.getLong("highest_bidder_id");
        Long highestBidderId = rs.wasNull() ? null : bidderId;

        return new Auction(
            rs.getLong("id"),
            rs.getLong("item_id"),
            rs.getLong("seller_id"),
            new BigDecimal(rs.getString("current_price")),
            highestBidderId,
            LocalDateTime.parse(rs.getString("start_time")),
            LocalDateTime.parse(rs.getString("end_time")),
            AuctionStatus.valueOf(rs.getString("status")),
            rs.getLong("version"),
            LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}
