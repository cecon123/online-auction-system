package com.auction.server.dao;

import com.auction.common.model.Item;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for Items.
 */
public interface ItemDao {
    /**
     * Persists a new item.
     * @param item The item to create (id will be ignored and replaced with generated one).
     * @return The generated ID.
     */
    long create(Item item);

    /**
     * Finds an item by its ID.
     */
    Optional<Item> findById(long id);

    /**
     * Finds all items owned by a specific seller.
     */
    List<Item> findBySellerId(long sellerId);

    /**
     * Finds all items in the system.
     */
    List<Item> findAll();

    /**
     * Updates an existing item's mutable fields (name, description, condition, imagePath).
     * Starting price is not updatable after creation.
     */
    void update(Item item);

    /**
     * Deletes an item.
     */
    void delete(long id);
}
