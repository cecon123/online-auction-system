package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base abstract class for auction items.
 *
 * Item -> Electronics, Art, Vehicle demonstrates inheritance and polymorphism.
 */
public abstract class Item extends Entity {

    private long sellerId;
    private ItemType itemType;
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private String imagePath;

    protected Item(
        long id,
        long sellerId,
        ItemType itemType,
        String name,
        String description,
        BigDecimal startingPrice,
        String imagePath,
        LocalDateTime createdAt
    ) {
        super(id, createdAt);
        setSellerId(sellerId);
        this.itemType = Objects.requireNonNull(
            itemType,
            "itemType must not be null"
        );
        setName(name);
        setDescription(description);
        setStartingPrice(startingPrice);
        this.imagePath = imagePath;
    }

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        if (sellerId <= 0) {
            throw new IllegalArgumentException("sellerId must be positive.");
        }
        this.sellerId = sellerId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    protected void setItemType(ItemType itemType) {
        this.itemType = Objects.requireNonNull(
            itemType,
            "itemType must not be null"
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        if (
            startingPrice == null ||
            startingPrice.compareTo(BigDecimal.ZERO) < 0
        ) {
            throw new IllegalArgumentException(
                "startingPrice must be non-negative."
            );
        }
        this.startingPrice = startingPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String displayName() {
        return name + " [" + itemType + "]";
    }

    public abstract String categoryDescription();

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " must not be blank."
            );
        }
        return value.trim();
    }
}
