package com.auction.server.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.common.enums.ItemType;
import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ItemFactoryTest {

    private final ItemFactory itemFactory = new ItemFactory();

    @Test
    void createShouldReturnElectronicsWhenTypeIsElectronics() {
        Item item = itemFactory.create(
            new ItemFactory.CreateItemData(
                1L,
                10L,
                ItemType.ELECTRONICS,
                "Laptop",
                "Gaming laptop",
                new BigDecimal("1000"),
                null,
                "Dell",
                "G15",
                null,
                null,
                null,
                0,
                LocalDateTime.now()
            )
        );

        assertInstanceOf(Electronics.class, item);
        assertEquals(ItemType.ELECTRONICS, item.getItemType());
        assertEquals("Laptop", item.getName());

        Electronics electronics = (Electronics) item;
        assertEquals("Dell", electronics.getBrand());
        assertEquals("G15", electronics.getModel());
    }

    @Test
    void createShouldReturnArtWhenTypeIsArt() {
        Item item = itemFactory.create(
            new ItemFactory.CreateItemData(
                2L,
                10L,
                ItemType.ART,
                "Oil Painting",
                "Vintage oil painting",
                new BigDecimal("500"),
                null,
                null,
                null,
                "Unknown Artist",
                "Oil",
                null,
                0,
                LocalDateTime.now()
            )
        );

        assertInstanceOf(Art.class, item);
        assertEquals(ItemType.ART, item.getItemType());
        assertEquals("Oil Painting", item.getName());

        Art art = (Art) item;
        assertEquals("Unknown Artist", art.getArtist());
        assertEquals("Oil", art.getMaterial());
    }

    @Test
    void createShouldReturnVehicleWhenTypeIsVehicle() {
        Item item = itemFactory.create(
            new ItemFactory.CreateItemData(
                3L,
                10L,
                ItemType.VEHICLE,
                "Classic Car",
                "1965 vintage roadster",
                new BigDecimal("20000"),
                null,
                null,
                null,
                null,
                null,
                "Ford",
                1965,
                LocalDateTime.now()
            )
        );

        assertInstanceOf(Vehicle.class, item);
        assertEquals(ItemType.VEHICLE, item.getItemType());
        assertEquals("Classic Car", item.getName());

        Vehicle vehicle = (Vehicle) item;
        assertEquals("Ford", vehicle.getManufacturer());
        assertEquals(1965, vehicle.getYear());
    }

    @Test
    void createShouldRejectNullData() {
        assertThrows(NullPointerException.class, () ->
            itemFactory.create(null)
        );
    }

    @Test
    void createDataShouldRejectInvalidSellerId() {
        assertThrows(IllegalArgumentException.class, () ->
            new ItemFactory.CreateItemData(
                1L,
                0L,
                ItemType.ELECTRONICS,
                "Laptop",
                "Description",
                new BigDecimal("1000"),
                null,
                "Dell",
                "G15",
                null,
                null,
                null,
                0,
                LocalDateTime.now()
            )
        );
    }

    @Test
    void createDataShouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            new ItemFactory.CreateItemData(
                1L,
                10L,
                ItemType.ELECTRONICS,
                " ",
                "Description",
                new BigDecimal("1000"),
                null,
                "Dell",
                "G15",
                null,
                null,
                null,
                0,
                LocalDateTime.now()
            )
        );
    }

    @Test
    void createDataShouldRejectNegativeStartingPrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new ItemFactory.CreateItemData(
                1L,
                10L,
                ItemType.ELECTRONICS,
                "Laptop",
                "Description",
                new BigDecimal("-1"),
                null,
                "Dell",
                "G15",
                null,
                null,
                null,
                0,
                LocalDateTime.now()
            )
        );
    }
}
