package com.auction.server.dao.sqlite;

import com.auction.common.model.AutoBidRule;
import com.auction.server.dao.AutoBidDao;
import com.auction.server.dao.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** SQLite implementation of AutoBidDao. */
public class SQLiteAutoBidDao implements AutoBidDao {

  private static final Logger logger = LoggerFactory.getLogger(SQLiteAutoBidDao.class);
  private final Database database;

  public SQLiteAutoBidDao() {
    this.database = Database.getInstance();
  }

  @Override
  public void createOrUpdate(AutoBidRule rule) {
    String sql =
        """
            INSERT INTO auto_bids (auction_id, bidder_id, max_bid, increment, active, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(auction_id, bidder_id) DO UPDATE SET
                max_bid = excluded.max_bid,
                increment = excluded.increment,
                active = excluded.active
            """;

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, rule.getAuctionId());
      statement.setLong(2, rule.getBidderId());
      statement.setString(3, rule.getMaxBid().toString());
      statement.setString(4, rule.getIncrement().toString());
      statement.setInt(5, rule.isActive() ? 1 : 0);
      statement.setString(6, LocalDateTime.now().toString());

      statement.executeUpdate();
    } catch (SQLException e) {
      logger.error(
          "Database error during createOrUpdate auto bid: Auction {}, Bidder {}",
          rule.getAuctionId(),
          rule.getBidderId(),
          e);
      throw new IllegalStateException("Could not save auto bid rule", e);
    }
  }

  @Override
  public Optional<AutoBidRule> findByAuctionAndBidder(long auctionId, long bidderId) {
    String sql = "SELECT * FROM auto_bids WHERE auction_id = ? AND bidder_id = ?";

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, auctionId);
      statement.setLong(2, bidderId);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return Optional.empty();
        }
        return Optional.of(mapRow(resultSet));
      }
    } catch (SQLException e) {
      logger.error(
          "Database error during findByAuctionAndBidder: Auction {}, Bidder {}",
          auctionId,
          bidderId,
          e);
      throw new IllegalStateException("Could not find auto bid rule", e);
    }
  }

  @Override
  public List<AutoBidRule> findActiveByAuction(long auctionId) {
    String sql = "SELECT * FROM auto_bids WHERE auction_id = ? AND active = 1";
    List<AutoBidRule> rules = new ArrayList<>();

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, auctionId);

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          rules.add(mapRow(resultSet));
        }
      }
      return rules;
    } catch (SQLException e) {
      logger.error("Database error during findActiveByAuction: Auction {}", auctionId, e);
      throw new IllegalStateException("Could not find auto bid rules", e);
    }
  }

  @Override
  public void delete(long auctionId, long bidderId) {
    String sql = "DELETE FROM auto_bids WHERE auction_id = ? AND bidder_id = ?";

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, auctionId);
      statement.setLong(2, bidderId);
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.error(
          "Database error during delete auto bid: Auction {}, Bidder {}", auctionId, bidderId, e);
      throw new IllegalStateException("Could not delete auto bid rule", e);
    }
  }

  private AutoBidRule mapRow(ResultSet rs) throws SQLException {
    return new AutoBidRule(
        rs.getLong("id"),
        rs.getLong("auction_id"),
        rs.getLong("bidder_id"),
        new BigDecimal(rs.getString("max_bid")),
        new BigDecimal(rs.getString("increment")),
        rs.getInt("active") == 1,
        LocalDateTime.parse(rs.getString("created_at")));
  }
}
