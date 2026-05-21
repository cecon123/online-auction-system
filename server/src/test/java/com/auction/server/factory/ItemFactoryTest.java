package com.auction.server.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
  void shouldCreateElectronicsItem() {
    Item item =
        itemFactory.create(
            new ItemFactory.CreateItemData(
                1L,
                10L,
                ItemType.ELECTRONICS,
                "Vintage Camera",
                "A vintage camera",
                "Used - Excellent",
                new BigDecimal("12000.00"),
                "camera.png",
                "Canon",
                "X100",
                null,
                null,
                null,
                0,
                LocalDateTime.now()));

    assertInstanceOf(Electronics.class, item);
    assertEquals(ItemType.ELECTRONICS, item.getItemType());
    assertEquals("Used - Excellent", item.getCondition());
  }

  @Test
  void shouldCreateArtItem() {
    Item item =
        itemFactory.create(
            new ItemFactory.CreateItemData(
                2L,
                10L,
                ItemType.ART,
                "Painting",
                "Abstract painting",
                "Brand New",
                new BigDecimal("8000.00"),
                "painting.png",
                null,
                null,
                "Unknown Artist",
                "Canvas",
                null,
                0,
                LocalDateTime.now()));

    assertInstanceOf(Art.class, item);
    assertEquals(ItemType.ART, item.getItemType());
    assertEquals("Brand New", item.getCondition());
  }

  @Test
  void shouldCreateVehicleItem() {
    Item item =
        itemFactory.create(
            new ItemFactory.CreateItemData(
                3L,
                10L,
                ItemType.VEHICLE,
                "Classic Scooter",
                "Classic scooter from 1985",
                "Used - Good",
                new BigDecimal("20000.00"),
                "scooter.png",
                null,
                null,
                null,
                null,
                "Honda",
                1985,
                LocalDateTime.now()));

    assertInstanceOf(Vehicle.class, item);
    assertEquals(ItemType.VEHICLE, item.getItemType());
    assertEquals("Used - Good", item.getCondition());
  }
}
