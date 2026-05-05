package com.auction.common.model;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Art item category.
 */
public class Art extends Item {

    private String artist;
    private String material;

    public Art(
        long id,
        long sellerId,
        String name,
        String description,
        String condition,
        BigDecimal startingPrice,
        String imagePath,
        String artist,
        String material,
        LocalDateTime createdAt
    ) {
        super(
            id,
            sellerId,
            ItemType.ART,
            name,
            description,
            condition,
            startingPrice,
            imagePath,
            createdAt
        );
        this.artist = normalizeOptionalText(artist);
        this.material = normalizeOptionalText(material);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = normalizeOptionalText(artist);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = normalizeOptionalText(material);
    }

    @Override
    public String categoryDescription() {
        if (artist.isBlank() && material.isBlank()) {
            return "Art item";
        }

        if (artist.isBlank()) {
            return "Art item - " + material;
        }

        if (material.isBlank()) {
            return "Art item by " + artist;
        }

        return "Art item by " + artist + " - " + material;
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }
}
