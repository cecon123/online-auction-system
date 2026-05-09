package com.auction.server.dao.sqlite;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.Role;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SQLiteUserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDatabase = Files.createTempFile("auction-test-", ".db");
        System.setProperty(
            "auction.db.url",
            "jdbc:sqlite:" + tempDatabase.toAbsolutePath()
        );
        System.setProperty("auction.skip.seed", "true");

        SchemaInitializer.initialize();
        userDao = new SQLiteUserDao();
    }

    @Test
    void createShouldInsertUserAndReturnGeneratedId() {
        long userId = userDao.create(
            "huy",
            "hashed-password",
            "Nguyen Huy",
            Role.BIDDER,
            new java.math.BigDecimal("1000"),
            java.math.BigDecimal.ZERO
        );

        assertTrue(userId > 0);
    }

    @Test
    void findByUsernameShouldReturnUserWhenExists() {
        long userId = userDao.create(
            "manh",
            "hashed-password",
            "Nguyen Manh",
            Role.SELLER,
            new java.math.BigDecimal("2000"),
            java.math.BigDecimal.ZERO
        );

        Optional<UserDao.UserRecord> user = userDao.findByUsername("manh");

        assertTrue(user.isPresent());
        assertEquals(userId, user.get().id());
        assertEquals("manh", user.get().username());
        assertEquals("Nguyen Manh", user.get().fullName());
        assertEquals(Role.SELLER, user.get().role());
        assertEquals(new java.math.BigDecimal("2000"), user.get().balance());
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
            Role.BIDDER,
            java.math.BigDecimal.ZERO,
            java.math.BigDecimal.ZERO
        );

        userDao.updateActiveStatus(userId, false);

        Optional<UserDao.UserRecord> user = userDao.findById(userId);

        assertTrue(user.isPresent());
        assertFalse(user.get().active());
    }

    @Test
    void updateBalanceShouldChangeUserBalance() {
        long userId = userDao.create(
            "bal-user",
            "p",
            "Name",
            Role.BIDDER,
            java.math.BigDecimal.ZERO,
            java.math.BigDecimal.ZERO
        );

        userDao.updateBalance(userId, new java.math.BigDecimal("500.50"));

        Optional<UserDao.UserRecord> user = userDao.findById(userId);
        assertTrue(user.isPresent());
        assertEquals(new java.math.BigDecimal("500.50"), user.get().balance());
    }

    @Test
    void findAllShouldReturnInsertedUsers() {
        userDao.create(
            "user1",
            "hash1",
            "User One",
            Role.BIDDER,
            java.math.BigDecimal.ZERO,
            java.math.BigDecimal.ZERO
        );
        userDao.create(
            "user2",
            "hash2",
            "User Two",
            Role.SELLER,
            java.math.BigDecimal.ZERO,
            java.math.BigDecimal.ZERO
        );

        assertEquals(2, userDao.findAll().size());
    }
}
