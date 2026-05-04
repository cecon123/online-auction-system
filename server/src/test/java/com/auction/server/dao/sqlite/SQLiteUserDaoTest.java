package com.auction.server.dao.sqlite;

import com.auction.common.enums.Role;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteUserDaoTest {
    private UserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDatabase = Files.createTempFile("auction-test-", ".db");
        System.setProperty("auction.db.url", "jdbc:sqlite:" + tempDatabase.toAbsolutePath());

        SchemaInitializer.initialize();
        userDao = new SQLiteUserDao();
    }

    @Test
    void createShouldInsertUserAndReturnGeneratedId() {
        long userId = userDao.create(
            "huy",
            "hashed-password",
            "Nguyen Huy",
            Role.BIDDER
        );

        assertTrue(userId > 0);
    }

    @Test
    void findByUsernameShouldReturnUserWhenExists() {
        long userId = userDao.create(
            "manh",
            "hashed-password",
            "Nguyen Manh",
            Role.SELLER
        );

        Optional<UserDao.UserRecord> user = userDao.findByUsername("manh");

        assertTrue(user.isPresent());
        assertEquals(userId, user.get().id());
        assertEquals("manh", user.get().username());
        assertEquals("Nguyen Manh", user.get().fullName());
        assertEquals(Role.SELLER, user.get().role());
        assertTrue(user.get().active());
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<UserDao.UserRecord> user = userDao.findByUsername("unknown");

        assertTrue(user.isEmpty());
    }

    @Test
    void updateActiveStatusShouldDisableUser() {
        long userId = userDao.create(
            "linh",
            "hashed-password",
            "Nguyen Linh",
            Role.BIDDER
        );

        userDao.updateActiveStatus(userId, false);

        Optional<UserDao.UserRecord> user = userDao.findById(userId);

        assertTrue(user.isPresent());
        assertFalse(user.get().active());
    }

    @Test
    void findAllShouldReturnInsertedUsers() {
        userDao.create("user1", "hash1", "User One", Role.BIDDER);
        userDao.create("user2", "hash2", "User Two", Role.SELLER);

        assertEquals(2, userDao.findAll().size());
    }
}
