package com.auction.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.enums.ItemType;
import com.auction.common.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ModelInheritanceTest {

    @Test
    void userSubclassesShouldHaveCorrectRoles() {
        LocalDateTime now = LocalDateTime.now();

        User bidder = new Bidder(
            1L,
            "bidder01",
            "hash",
            "Bidder One",
            true,
            now
        );

        User seller = new Seller(
            2L,
            "seller01",
            "hash",
            "Seller One",
            true,
            now
        );

        User admin = new Admin(3L, "admin01", "hash", "Admin One", true, now);

        assertEquals(Role.BIDDER, bidder.getRole());
        assertEquals(Role.SELLER, seller.getRole());
        assertEquals(Role.ADMIN, admin.getRole());

        assertTrue(((Bidder) bidder).canBid());
        assertTrue(((Seller) seller).canCreateAuction());
        assertTrue(((Admin) admin).canManageSystem());
    }

    @Test
    void itemSubclassesShouldHaveCorrectTypesAndCondition() {
        LocalDateTime now = LocalDateTime.now();

        Item electronics = new Electronics(
            1L,
            10L,
            "Camera",
            "Vintage camera",
            "Used - Excellent",
            new BigDecimal("12000.00"),
            "camera.png",
            "Canon",
            "X100",
            now
        );

        Item art = new Art(
            2L,
            10L,
            "Painting",
            "Abstract painting",
            "Brand New",
            new BigDecimal("8000.00"),
            "painting.png",
            "Unknown Artist",
            "Canvas",
            now
        );

        Item vehicle = new Vehicle(
            3L,
            10L,
            "Scooter",
            "Classic scooter",
            "Used - Good",
            new BigDecimal("20000.00"),
            "scooter.png",
            "Honda",
            1985,
            now
        );

        assertEquals(ItemType.ELECTRONICS, electronics.getItemType());
        assertEquals(ItemType.ART, art.getItemType());
        assertEquals(ItemType.VEHICLE, vehicle.getItemType());

        assertEquals("Used - Excellent", electronics.getCondition());
        assertEquals("Brand New", art.getCondition());
        assertEquals("Used - Good", vehicle.getCondition());

        assertTrue(electronics.categoryDescription().contains("Canon"));
        assertTrue(art.categoryDescription().contains("Unknown Artist"));
        assertTrue(vehicle.categoryDescription().contains("Honda"));
    }
}
