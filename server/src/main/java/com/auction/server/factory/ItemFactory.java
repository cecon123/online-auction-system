package com.auction.server.factory;

import com.auction.common.enums.ItemType;
import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Factory Method for creating concrete Item objects.
 *
 * <p>The system has an abstract Item class and concrete subclasses: Electronics, Art, Vehicle.
 *
 * <p>Instead of scattering object creation logic across services/controllers, this factory
 * centralizes the creation of Item objects based on ItemType.
 */
public class ItemFactory {

  public Item create(CreateItemData data) {
    Objects.requireNonNull(data, "data must not be null");
    Objects.requireNonNull(data.itemType(), "itemType must not be null");

    return switch (data.itemType()) {
      case ELECTRONICS -> createElectronics(data);
      case ART -> createArt(data);
      case VEHICLE -> createVehicle(data);
    };
  }

  private Item createElectronics(CreateItemData data) {
    return new Electronics(
        data.id(),
        data.sellerId(),
        data.name(),
        data.description(),
        data.condition(),
        data.startingPrice(),
        data.imagePath(),
        data.brand(),
        data.model(),
        data.createdAt());
  }

  private Item createArt(CreateItemData data) {
    return new Art(
        data.id(),
        data.sellerId(),
        data.name(),
        data.description(),
        data.condition(),
        data.startingPrice(),
        data.imagePath(),
        data.artist(),
        data.material(),
        data.createdAt());
  }

  private Item createVehicle(CreateItemData data) {
    return new Vehicle(
        data.id(),
        data.sellerId(),
        data.name(),
        data.description(),
        data.condition(),
        data.startingPrice(),
        data.imagePath(),
        data.manufacturer(),
        data.year(),
        data.createdAt());
  }

  /**
   * Data object used by ItemFactory.
   *
   * <p>Some fields are only meaningful for specific item types: - Electronics: brand, model - Art:
   * artist, material - Vehicle: manufacturer, year
   */
  public record CreateItemData(
      long id,
      long sellerId,
      ItemType itemType,
      String name,
      String description,
      String condition,
      BigDecimal startingPrice,
      String imagePath,
      String brand,
      String model,
      String artist,
      String material,
      String manufacturer,
      int year,
      LocalDateTime createdAt) {
    public CreateItemData {
      if (sellerId <= 0) {
        throw new IllegalArgumentException("sellerId must be positive.");
      }

      if (itemType == null) {
        throw new IllegalArgumentException("itemType must not be null.");
      }

      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("name must not be blank.");
      }

      if (startingPrice == null || startingPrice.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("startingPrice must be non-negative.");
      }

      if (condition == null || condition.isBlank()) {
        condition = "Brand New";
      } else {
        condition = condition.trim();
      }

      if (createdAt == null) {
        createdAt = LocalDateTime.now();
      }
    }
  }
}
