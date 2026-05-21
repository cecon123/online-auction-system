package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Electronics item category. */
public class Electronics extends Item {

  private String brand;
  private String model;

  public Electronics(
      long id,
      long sellerId,
      String name,
      String description,
      String condition,
      BigDecimal startingPrice,
      String imagePath,
      String brand,
      String model,
      LocalDateTime createdAt) {
    super(
        id,
        sellerId,
        ItemType.ELECTRONICS,
        name,
        description,
        condition,
        startingPrice,
        imagePath,
        createdAt);
    this.brand = normalizeOptionalText(brand);
    this.model = normalizeOptionalText(model);
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = normalizeOptionalText(brand);
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = normalizeOptionalText(model);
  }

  @Override
  public String categoryDescription() {
    String details = formatOptionalDetails();

    if (details.isBlank()) {
      return "Electronics item";
    }

    return "Electronics item - " + details;
  }

  private String formatOptionalDetails() {
    String combined = (brand + " " + model).trim();
    return combined;
  }

  private String normalizeOptionalText(String value) {
    return value == null ? "" : value.trim();
  }
}
