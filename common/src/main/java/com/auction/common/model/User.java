package com.auction.common.model;

import com.auction.common.enums.Role;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base abstract class for all users.
 *
 * User -> Bidder, Seller, Admin demonstrates inheritance and abstraction.
 */
public abstract class User extends Entity {

    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean active;

    protected User() {
        super();
        this.active = true;
    }

    protected User(
        long id,
        String username,
        String passwordHash,
        String fullName,
        Role role,
        boolean active,
        LocalDateTime createdAt
    ) {
        super(id, createdAt);
        this.username = requireText(username, "username");
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.fullName = requireText(fullName, "fullName");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = requireText(username, "username");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "passwordHash");
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = requireText(fullName, "fullName");
    }

    public Role getRole() {
        return role;
    }

    protected void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasRole(Role expectedRole) {
        return role == expectedRole;
    }

    @Override
    public String displayName() {
        return fullName + " (" + username + ")";
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " must not be blank."
            );
        }
        return value.trim();
    }
}
