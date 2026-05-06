package com.auction.server.dao.sqlite;

import com.auction.common.model.BidTransaction;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of BidDao.
 */
public class SQLiteBidDao implements BidDao {
    private final Database database;

    public SQLiteBidDao() {
        this.database = Database.getInstance();
    }

    @Override
    public long create(BidTransaction bid) {
        String sql = """
                INSERT INTO bids (auction_id, bidder_id, amount, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, bid.getAuctionId());
            statement.setLong(2, bid.getBidderId());
            statement.setString(3, bid.getAmount().toString());
            statement.setString(4, LocalDateTime.now().toString());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }

            throw new SQLException("Creating bid failed, no generated ID returned.");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create bid for auction: " + bid.getAuctionId(), e);
        }
    }

    @Override
    public List<BidTransaction> findByAuctionId(long auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY created_at DESC";
        List<BidTransaction> bids = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, auctionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bids.add(mapRow(resultSet));
                }
            }
            return bids;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find bids by auction_id: " + auctionId, e);
        }
    }

    @Override
    public List<BidTransaction> findByBidderId(long bidderId) {
        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY created_at DESC";
        List<BidTransaction> bids = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, bidderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bids.add(mapRow(resultSet));
                }
            }
            return bids;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not find bids by bidder_id: " + bidderId, e);
        }
    }

    private BidTransaction mapRow(ResultSet rs) throws SQLException {
        return new BidTransaction(
            rs.getLong("id"),
            rs.getLong("auction_id"),
            rs.getLong("bidder_id"),
            new BigDecimal(rs.getString("amount")),
            LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}
