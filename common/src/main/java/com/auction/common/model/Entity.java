package com.auction.common.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base abstract class for all domain entities.
 *
 * This class demonstrates abstraction and encapsulates common identity fields.
 */
public abstract class Entity {

    private long id;
    private LocalDateTime createdAt;

    protected Entity() {
        this.createdAt = LocalDateTime.now();
    }

    protected Entity(long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException(
                "Entity id must not be negative."
            );
        }
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(
            createdAt,
            "createdAt must not be null"
        );
    }

    /**
     * Polymorphic display method.
     * Subclasses override this method to provide specific information.
     */
    public abstract String displayName();
}
