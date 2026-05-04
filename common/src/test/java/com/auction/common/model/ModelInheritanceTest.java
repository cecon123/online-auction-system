package com.auction.common.model;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ModelInheritanceTest {

    @Test
    void bidderShouldHaveBidderRole() {
        Bidder bidder = new Bidder(
            1L,
            "huy",
            "hash",
            "Nguyen Huy",
            true,
            LocalDateTime.now()
        );

        assertEquals(Role.BIDDER, bidder.getRole());
        assertTrue(bidder.canBid());
        assertTrue(bidder.displayName().contains("Bidder"));
    }

    @Test
    void sellerShouldHaveSellerRole() {
        Seller seller = new Seller(
            2L,
            "manh",
            "hash",
            "Nguyen Manh",
            true,
            LocalDateTime.now()
        );

        assertEquals(Role.SELLER, seller.getRole());
        assertTrue(seller.canCreateAuction());
    }

    @Test
    void adminShouldHaveAdminRole() {
        Admin admin = new Admin(
            3L,
            "admin",
            "hash",
            "System Admin",
            true,
            LocalDateTime.now()
        );

        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.canManageSystem());
    }

    @Test
    void itemSubclassesShouldReturnCorrectCategoryDescription() {
        Item electronics = new Electronics(
            1L,
            10L,
            "Laptop",
            "Gaming laptop",
            new BigDecimal("1000"),
            null,
            "Dell",
            "G15",
            LocalDateTime.now()
        );

        Item art = new Art(
            2L,
            10L,
            "Painting",
            "Oil painting",
            new BigDecimal("500"),
            null,
            "Unknown Artist",
            "Oil",
            LocalDateTime.now()
        );

        Item vehicle = new Vehicle(
            3L,
            10L,
            "Car",
            "Classic car",
            new BigDecimal("20000"),
            null,
            "Toyota",
            1998,
            LocalDateTime.now()
        );

        assertTrue(electronics.categoryDescription().contains("Electronics"));
        assertTrue(art.categoryDescription().contains("Art"));
        assertTrue(vehicle.categoryDescription().contains("Vehicle"));
    }

    @Test
    void auctionShouldAcceptBidOnlyWhenRunning() {
        LocalDateTime now = LocalDateTime.now();

        Auction auction = new Auction(
            1L,
            1L,
            2L,
            new BigDecimal("100"),
            null,
            now.minusMinutes(1),
            now.plusMinutes(10),
            AuctionStatus.RUNNING,
            0,
            now.minusHours(1)
        );

        assertTrue(auction.canAcceptBidAt(now));
    }

    @Test
    void updateHighestBidShouldIncreasePriceAndVersion() {
        LocalDateTime now = LocalDateTime.now();

        Auction auction = new Auction(
            1L,
            1L,
            2L,
            new BigDecimal("100"),
            null,
            now.minusMinutes(1),
            now.plusMinutes(10),
            AuctionStatus.RUNNING,
            0,
            now.minusHours(1)
        );

        auction.updateHighestBid(3L, new BigDecimal("150"));

        assertEquals(new BigDecimal("150"), auction.getCurrentPrice());
        assertEquals(3L, auction.getHighestBidderId());
        assertEquals(1L, auction.getVersion());
    }

    @Test
    void bidTransactionShouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            new BidTransaction(
                1L,
                1L,
                2L,
                new BigDecimal("-1"),
                LocalDateTime.now()
            )
        );
    }
}
