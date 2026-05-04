package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vehicle item category.
 */
public class Vehicle extends Item {

    private String manufacturer;
    private int year;

    public Vehicle(
        long id,
        long sellerId,
        String name,
        String description,
        BigDecimal startingPrice,
        String imagePath,
        String manufacturer,
        int year,
        LocalDateTime createdAt
    ) {
        super(
            id,
            sellerId,
            ItemType.VEHICLE,
            name,
            description,
            startingPrice,
            imagePath,
            createdAt
        );
        this.manufacturer = manufacturer;
        this.year = year;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year < 1800) {
            throw new IllegalArgumentException("Vehicle year is invalid.");
        }
        this.year = year;
    }

    @Override
    public String categoryDescription() {
        if (manufacturer == null || manufacturer.isBlank()) {
            return "Vehicle item";
        }

        return "Vehicle item - " + manufacturer + " " + year;
    }
}
