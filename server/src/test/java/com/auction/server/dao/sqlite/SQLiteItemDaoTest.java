package com.auction.server.dao.sqlite;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.Role;
import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SQLiteItemDaoTest {

    private ItemDao itemDao;
    private UserDao userDao;
    private long sellerId;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDatabase = Files.createTempFile("auction-item-test-", ".db");
        System.setProperty(
            "auction.db.url",
            "jdbc:sqlite:" + tempDatabase.toAbsolutePath()
        );
        System.setProperty("auction.skip.seed", "true");

        SchemaInitializer.initialize();
        itemDao = new SQLiteItemDao();
        userDao = new SQLiteUserDao();

        // Create a seller for items
        sellerId = userDao.create(
            "seller",
            "pass",
            "The Seller",
            Role.SELLER,
            java.math.BigDecimal.ZERO
        );
    }

    @Test
    void createElectronicsShouldReturnId() {
        Electronics electronics = new Electronics(
            0,
            sellerId,
            "Laptop",
            "Gaming laptop",
            "New",
            new BigDecimal("1000"),
            null,
            "Dell",
            "Alienware",
            LocalDateTime.now()
        );

        long id = itemDao.create(electronics);
        assertTrue(id > 0);

        Optional<Item> found = itemDao.findById(id);
        assertTrue(found.isPresent());
        assertTrue(found.get() instanceof Electronics);
        Electronics e = (Electronics) found.get();
        assertEquals("Laptop", e.getName());
        assertEquals("Dell", e.getBrand());
    }

    @Test
    void createArtShouldReturnId() {
        Art art = new Art(
            0,
            sellerId,
            "Mona Lisa",
            "Famous painting",
            "Old",
            new BigDecimal("1000000"),
            null,
            "Da Vinci",
            "Oil on wood",
            LocalDateTime.now()
        );

        long id = itemDao.create(art);
        assertTrue(id > 0);

        Optional<Item> found = itemDao.findById(id);
        assertTrue(found.isPresent());
        assertTrue(found.get() instanceof Art);
        Art a = (Art) found.get();
        assertEquals("Da Vinci", a.getArtist());
    }

    @Test
    void createVehicleShouldReturnId() {
        Vehicle vehicle = new Vehicle(
            0,
            sellerId,
            "Model S",
            "Electric car",
            "Used",
            new BigDecimal("50000"),
            null,
            "Tesla",
            2022,
            LocalDateTime.now()
        );

        long id = itemDao.create(vehicle);
        assertTrue(id > 0);

        Optional<Item> found = itemDao.findById(id);
        assertTrue(found.isPresent());
        assertTrue(found.get() instanceof Vehicle);
        Vehicle v = (Vehicle) found.get();
        assertEquals(2022, v.getYear());
    }

    @Test
    void findBySellerIdShouldReturnOnlySellerItems() {
        long otherSellerId = userDao.create(
            "other",
            "pass",
            "Other Seller",
            Role.SELLER,
            java.math.BigDecimal.ZERO
        );

        itemDao.create(
            new Electronics(
                0,
                sellerId,
                "L1",
                "D",
                "C",
                new BigDecimal("100"),
                null,
                "B",
                "M",
                LocalDateTime.now()
            )
        );
        itemDao.create(
            new Electronics(
                0,
                otherSellerId,
                "L2",
                "D",
                "C",
                new BigDecimal("100"),
                null,
                "B",
                "M",
                LocalDateTime.now()
            )
        );

        List<Item> sellerItems = itemDao.findBySellerId(sellerId);
        assertEquals(1, sellerItems.size());
        assertEquals("L1", sellerItems.get(0).getName());
    }

    @Test
    void deleteShouldRemoveItem() {
        long id = itemDao.create(
            new Electronics(
                0,
                sellerId,
                "L1",
                "D",
                "C",
                new BigDecimal("100"),
                null,
                "B",
                "M",
                LocalDateTime.now()
            )
        );
        assertTrue(itemDao.findById(id).isPresent());

        itemDao.delete(id);
        assertFalse(itemDao.findById(id).isPresent());
    }
}
