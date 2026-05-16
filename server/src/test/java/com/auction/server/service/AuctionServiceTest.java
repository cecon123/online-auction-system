package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.auction.UpdateAuctionRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.model.Auction;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

  @Mock private AuctionDao auctionDao;
  @Mock private ItemDao itemDao;
  @Mock private BidDao bidDao;
  @Mock private WalletService walletService;

  private AuctionService auctionService;

  private final long SELLER_ID = 1L;
  private final long AUCTION_ID = 10L;
  private final long ITEM_ID = 100L;

  @BeforeEach
  void setUp() {
    auctionService = new AuctionService(auctionDao, itemDao, bidDao, walletService);
  }

  @Test
  void shouldCreateAuctionSuccessfully() {
    // Arrange
    CreateAuctionRequest request =
        new CreateAuctionRequest(
            "Test Item",
            ItemType.ELECTRONICS,
            "New",
            "Description",
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            "image.jpg",
            null);

    when(itemDao.create(any(Item.class))).thenReturn(ITEM_ID);
    when(auctionDao.create(any(Auction.class))).thenReturn(AUCTION_ID);

    // Act
    var response = auctionService.createAuction(SELLER_ID, request);

    // Assert
    assertNotNull(response);
    assertEquals(AUCTION_ID, response.id());
    assertEquals("Test Item", response.title());
    assertEquals(AuctionStatus.OPEN, response.status());

    verify(itemDao).create(any(Item.class));
    verify(auctionDao).create(any(Auction.class));
  }

  @Test
  void shouldCancelAuctionSuccessfully() {
    // Arrange
    Auction auction = createMockAuction(AuctionStatus.RUNNING);
    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

    // Act
    auctionService.cancelAuction(SELLER_ID, AUCTION_ID);

    // Assert
    assertEquals(AuctionStatus.CANCELED, auction.getStatus());
    verify(auctionDao).update(auction);
  }

  @Test
  void shouldFailCancelWhenNotOwner() {
    // Arrange
    Auction auction = createMockAuction(AuctionStatus.RUNNING);
    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> {
          auctionService.cancelAuction(999L, AUCTION_ID);
        });
  }

  @Test
  void shouldUpdateAuctionSuccessfully() {
    // Arrange
    Auction auction = createMockAuction(AuctionStatus.OPEN);
    Item item = createMockItem();

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(itemDao.findById(ITEM_ID)).thenReturn(Optional.of(item));

    UpdateAuctionRequest request =
        new UpdateAuctionRequest(
            AUCTION_ID,
            "Updated Name",
            ItemType.ELECTRONICS,
            "Used",
            "New Desc",
            new BigDecimal("200.00"),
            new BigDecimal("250.00"),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            "new_image.jpg",
            null);

    // Act
    auctionService.updateAuction(SELLER_ID, request);

    // Assert
    assertEquals("Updated Name", item.getName());
    assertEquals(new BigDecimal("200.00"), auction.getCurrentPrice());
    assertEquals(new BigDecimal("200.00"), auction.getHighestMaxBid());
    verify(itemDao).update(item);
    verify(auctionDao).update(auction);
  }

  @Test
  void shouldFailUpdateWhenBidsExist() {
    // Arrange
    Auction auction = createMockAuction(AuctionStatus.OPEN);
    auction.setHighestBidderId(2L); // Bids exist!

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

    UpdateAuctionRequest request =
        new UpdateAuctionRequest(
            AUCTION_ID,
            "Updated Name",
            ItemType.ELECTRONICS,
            "Used",
            "New Desc",
            new BigDecimal("200.00"),
            new BigDecimal("250.00"),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            "new_image.jpg",
            null);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> {
          auctionService.updateAuction(SELLER_ID, request);
        });
  }

  private Auction createMockAuction(AuctionStatus status) {
    return new Auction(
        AUCTION_ID,
        ITEM_ID,
        SELLER_ID,
        new BigDecimal("100.00"),
        new BigDecimal("100.00"),
        new BigDecimal("150.00"),
        null,
        LocalDateTime.now().minusHours(1),
        LocalDateTime.now().plusHours(1),
        status,
        0L,
        LocalDateTime.now());
  }

  private Item createMockItem() {
    return new Electronics(
        ITEM_ID,
        SELLER_ID,
        "Test Item",
        "Desc",
        "New",
        new BigDecimal("100.00"),
        "path",
        "Brand",
        "Model",
        LocalDateTime.now());
  }
}
