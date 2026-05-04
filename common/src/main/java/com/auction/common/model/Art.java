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
            startingPrice,
            imagePath,
            createdAt
        );
        this.artist = artist;
        this.material = material;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    @Override
    public String categoryDescription() {
        if (artist == null || artist.isBlank()) {
            return "Art item";
        }

        return "Art item by " + artist;
    }
}
