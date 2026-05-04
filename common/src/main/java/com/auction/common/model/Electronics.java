package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Electronics item category.
 */
public class Electronics extends Item {

    private String brand;
    private String model;

    public Electronics(
        long id,
        long sellerId,
        String name,
        String description,
        BigDecimal startingPrice,
        String imagePath,
        String brand,
        String model,
        LocalDateTime createdAt
    ) {
        super(
            id,
            sellerId,
            ItemType.ELECTRONICS,
            name,
            description,
            startingPrice,
            imagePath,
            createdAt
        );
        this.brand = brand;
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String categoryDescription() {
        return "Electronics item" + formatOptionalDetails();
    }

    private String formatOptionalDetails() {
        if (
            (brand == null || brand.isBlank()) &&
            (model == null || model.isBlank())
        ) {
            return "";
        }

        return " - " + nullToEmpty(brand) + " " + nullToEmpty(model);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
