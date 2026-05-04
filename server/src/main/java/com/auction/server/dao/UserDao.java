package com.auction.server.dao;

import com.auction.common.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for users.
 *
 * Service classes depend on this interface instead of concrete SQLite code.
 */
public interface UserDao {
    long create(String username, String passwordHash, String fullName, Role role);

    Optional<UserRecord> findByUsername(String username);

    Optional<UserRecord> findById(long id);

    List<UserRecord> findAll();

    void updateActiveStatus(long userId, boolean active);

    record UserRecord(
        long id,
        String username,
        String passwordHash,
        String fullName,
        Role role,
        boolean active,
        LocalDateTime createdAt
    ) {
    }
}
