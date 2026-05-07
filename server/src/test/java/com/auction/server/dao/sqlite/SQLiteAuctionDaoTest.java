package com.auction.server.dao.sqlite;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Electronics;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SQLiteAuctionDaoTest {

    private AuctionDao auctionDao;
    private ItemDao itemDao;
    private UserDao userDao;
    private long sellerId;
    private long itemId;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDatabase = Files.createTempFile("auction-auc-test-", ".db");
        System.setProperty(
            "auction.db.url",
            "jdbc:sqlite:" + tempDatabase.toAbsolutePath()
        );
        System.setProperty("auction.skip.seed", "true");

        SchemaInitializer.initialize();
        auctionDao = new SQLiteAuctionDao();
        itemDao = new SQLiteItemDao();
        userDao = new SQLiteUserDao();

        sellerId = userDao.create(
            "seller",
            "pass",
            "The Seller",
            Role.SELLER,
            java.math.BigDecimal.ZERO
        );
        itemId = itemDao.create(
            new Electronics(
                0,
                sellerId,
                "Laptop",
                "D",
                "C",
                new BigDecimal("100"),
                null,
                "B",
                "M",
                LocalDateTime.now()
            )
        );
    }

    @Test
    void createAuctionShouldReturnId() {
        Auction auction = new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100"),
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            AuctionStatus.OPEN,
            0,
            LocalDateTime.now()
        );

        long id = auctionDao.create(auction);
        assertTrue(id > 0);

        Optional<Auction> found = auctionDao.findById(id);
        assertTrue(found.isPresent());
        assertEquals(itemId, found.get().getItemId());
        assertEquals(AuctionStatus.OPEN, found.get().getStatus());
    }

    @Test
    void updateShouldSucceedWhenVersionMatches() {
        Auction auction = new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100"),
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            AuctionStatus.OPEN,
            0,
            LocalDateTime.now()
        );
        long id = auctionDao.create(auction);
        auction = auctionDao.findById(id).get();

        auction.setStatus(AuctionStatus.RUNNING);
        auctionDao.update(auction);

        Auction updated = auctionDao.findById(id).get();
        assertEquals(AuctionStatus.RUNNING, updated.getStatus());
        assertEquals(1, updated.getVersion());
    }

    @Test
    void updateShouldFailWhenVersionMismatch() {
        Auction auction = new Auction(
            0,
            itemId,
            sellerId,
            new BigDecimal("100"),
            null,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            AuctionStatus.OPEN,
            0,
            LocalDateTime.now()
        );
        long id = auctionDao.create(auction);

        Auction auctionCopy1 = auctionDao.findById(id).get();
        Auction auctionCopy2 = auctionDao.findById(id).get();

        // Update 1 succeeds
        auctionCopy1.setStatus(AuctionStatus.RUNNING);
        auctionDao.update(auctionCopy1);

        // Update 2 fails due to version mismatch
        auctionCopy2.setStatus(AuctionStatus.FINISHED);
        assertThrows(IllegalStateException.class, () ->
            auctionDao.update(auctionCopy2)
        );
    }
}
