package com.auction.server.dao.sqlite;

import com.auction.common.enums.ItemType;
import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;
import com.auction.server.dao.Database;
import com.auction.server.dao.ItemDao;
import com.auction.server.factory.ItemFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** SQLite implementation of ItemDao. */
public class SQLiteItemDao implements ItemDao {
  private static final Logger logger = LoggerFactory.getLogger(SQLiteItemDao.class);
  private final Database database;
  private final ItemFactory itemFactory;

  public SQLiteItemDao() {
    this.database = Database.getInstance();
    this.itemFactory = new ItemFactory();
  }

  @Override
  public long create(Item item) {
    String sql =
        """
                INSERT INTO items (
                    seller_id, item_type, name, description, condition_text,
                    starting_price, image_path, brand, model, artist,
                    material, manufacturer, vehicle_year, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

    try (Connection connection = database.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setLong(1, item.getSellerId());
      statement.setString(2, item.getItemType().name());
      statement.setString(3, item.getName());
      statement.setString(4, item.getDescription());
      statement.setString(5, item.getCondition());
      statement.setString(6, item.getStartingPrice().toString());
      statement.setString(7, item.getImagePath());

      // Subtype specific fields
      if (item instanceof Electronics e) {
        statement.setString(8, e.getBrand());
        statement.setString(9, e.getModel());
        statement.setNull(10, java.sql.Types.VARCHAR);
        statement.setNull(11, java.sql.Types.VARCHAR);
        statement.setNull(12, java.sql.Types.VARCHAR);
        statement.setNull(13, java.sql.Types.INTEGER);
      } else if (item instanceof Art a) {
        statement.setNull(8, java.sql.Types.VARCHAR);
        statement.setNull(9, java.sql.Types.VARCHAR);
        statement.setString(10, a.getArtist());
        statement.setString(11, a.getMaterial());
        statement.setNull(12, java.sql.Types.VARCHAR);
        statement.setNull(13, java.sql.Types.INTEGER);
      } else if (item instanceof Vehicle v) {
        statement.setNull(8, java.sql.Types.VARCHAR);
        statement.setNull(9, java.sql.Types.VARCHAR);
        statement.setNull(10, java.sql.Types.VARCHAR);
        statement.setNull(11, java.sql.Types.VARCHAR);
        statement.setString(12, v.getManufacturer());
        statement.setInt(13, v.getYear());
      } else {
        statement.setNull(8, java.sql.Types.VARCHAR);
        statement.setNull(9, java.sql.Types.VARCHAR);
        statement.setNull(10, java.sql.Types.VARCHAR);
        statement.setNull(11, java.sql.Types.VARCHAR);
        statement.setNull(12, java.sql.Types.VARCHAR);
        statement.setNull(13, java.sql.Types.INTEGER);
      }

      statement.setString(14, LocalDateTime.now().toString());

      statement.executeUpdate();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return generatedKeys.getLong(1);
        }
      }

      throw new SQLException("Creating item failed, no generated ID returned.");
    } catch (SQLException e) {
      logger.error("Database error during create item: {}", item.getName(), e);
      throw new IllegalStateException("Could not create item: " + item.getName(), e);
    }
  }

  @Override
  public Optional<Item> findById(long id) {
    String sql = "SELECT * FROM items WHERE id = ? AND deleted = 0";

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
      throw new IllegalStateException("Could not find item by id: " + id, e);
    }
  }

  @Override
  public List<Item> findBySellerId(long sellerId) {
    String sql = "SELECT * FROM items WHERE seller_id = ? AND deleted = 0 ORDER BY created_at DESC";
    List<Item> items = new ArrayList<>();

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setLong(1, sellerId);

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          items.add(mapRow(resultSet));
        }
      }
      return items;
    } catch (SQLException e) {
      logger.error("Database error during findBySellerId: {}", sellerId, e);
      throw new IllegalStateException("Could not find items by seller_id: " + sellerId, e);
    }
  }

  @Override
  public List<Item> findAll() {
    String sql = "SELECT * FROM items WHERE deleted = 0 ORDER BY created_at DESC";
    List<Item> items = new ArrayList<>();

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        items.add(mapRow(resultSet));
      }
      return items;
    } catch (SQLException e) {
      logger.error("Database error during findAll items", e);
      throw new IllegalStateException("Could not find all items", e);
    }
  }

  @Override
  public void update(Item item) {
    String sql =
        """
                UPDATE items SET
                    name = ?, description = ?, condition_text = ?,
                    image_path = ?, starting_price = ?
                WHERE id = ? AND deleted = 0
                """;

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, item.getName());
      statement.setString(2, item.getDescription());
      statement.setString(3, item.getCondition());
      statement.setString(4, item.getImagePath());
      statement.setString(5, item.getStartingPrice().toString());
      statement.setLong(6, item.getId());

      int affected = statement.executeUpdate();
      if (affected == 0) {
        throw new IllegalStateException("No item updated, id not found: " + item.getId());
      }
      logger.info("Updated item id={}", item.getId());
    } catch (SQLException e) {
      logger.error("Database error during update item: {}", item.getId(), e);
      throw new IllegalStateException("Could not update item: " + item.getId(), e);
    }
  }

  @Override
  public void delete(long id) {
    String sql = "UPDATE items SET deleted = 1 WHERE id = ?";

    try (Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setLong(1, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      logger.error("Database error during soft delete item: {}", id, e);
      throw new IllegalStateException("Could not soft delete item: " + id, e);
    }
  }

  private Item mapRow(ResultSet rs) throws SQLException {
    ItemFactory.CreateItemData data =
        new ItemFactory.CreateItemData(
            rs.getLong("id"),
            rs.getLong("seller_id"),
            ItemType.valueOf(rs.getString("item_type")),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("condition_text"),
            new BigDecimal(rs.getString("starting_price")),
            rs.getString("image_path"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getString("artist"),
            rs.getString("material"),
            rs.getString("manufacturer"),
            rs.getInt("vehicle_year"),
            LocalDateTime.parse(rs.getString("created_at")));

    return itemFactory.create(data);
  }
}
