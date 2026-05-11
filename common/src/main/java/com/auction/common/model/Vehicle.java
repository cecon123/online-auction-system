package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vehicle item category. */
public class Vehicle extends Item {

  private String manufacturer;
  private int year;

  public Vehicle(
      long id,
      long sellerId,
      String name,
      String description,
      String condition,
      BigDecimal startingPrice,
      String imagePath,
      String manufacturer,
      int year,
      LocalDateTime createdAt) {
    super(
        id,
        sellerId,
        ItemType.VEHICLE,
        name,
        description,
        condition,
        startingPrice,
        imagePath,
        createdAt);
    this.manufacturer = normalizeOptionalText(manufacturer);
    setYear(year);
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = normalizeOptionalText(manufacturer);
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    if (year != 0 && year < 1800) {
      throw new IllegalArgumentException("Vehicle year is invalid.");
    }
    this.year = year;
  }

  @Override
  public String categoryDescription() {
    if (manufacturer.isBlank() && year == 0) {
      return "Vehicle item";
    }

    if (manufacturer.isBlank()) {
      return "Vehicle item - " + year;
    }

    if (year == 0) {
      return "Vehicle item - " + manufacturer;
    }

    return "Vehicle item - " + manufacturer + " " + year;
  }

  private String normalizeOptionalText(String value) {
    return value == null ? "" : value.trim();
  }
}
