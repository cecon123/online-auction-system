package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.AutoBidDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

  @Mock private AuctionDao auctionDao;
  @Mock private BidDao bidDao;
  @Mock private UserDao userDao;
  @Mock private AutoBidDao autoBidDao;
  @Mock private WalletService walletService;

  private BidService bidService;

  private final long AUCTION_ID = 1L;
  private final long BIDDER_ID = 2L;
  private final long SELLER_ID = 3L;

  @BeforeEach
  void setUp() {
    bidService = new BidService(auctionDao, bidDao, userDao, autoBidDao, walletService);
  }

  @Test
  void shouldPlaceBidSuccessfully() {
    // Arrange
    BigDecimal initialPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("120.00");

    Auction auction = createMockAuction(initialPrice);
    UserDao.UserRecord bidder = createMockUser(BIDDER_ID, "bidder01", new BigDecimal("1000.00"));

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(userDao.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));

    PlaceBidRequest request = new PlaceBidRequest(AUCTION_ID, bidAmount);

    // Act
    PlaceBidResponse response = bidService.placeBid(BIDDER_ID, request);

    // Assert
    assertNotNull(response);
    assertEquals(bidAmount, response.currentPrice());
    assertEquals("bidder01", response.highestBidderUsername());

    verify(walletService).lockFunds(eq(BIDDER_ID), eq(bidAmount));
    verify(auctionDao).update(any(Auction.class));
    verify(bidDao).create(any(BidTransaction.class));
  }

  @Test
  void shouldFailWhenBidIsTooLow() {
    // Arrange
    BigDecimal initialPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("105.00"); // Min increment is 10.00

    Auction auction = createMockAuction(initialPrice);
    auction.setHighestBidderId(4L); // Already has a bidder
    UserDao.UserRecord bidder = createMockUser(BIDDER_ID, "bidder01", new BigDecimal("1000.00"));

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(userDao.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));

    PlaceBidRequest request = new PlaceBidRequest(AUCTION_ID, bidAmount);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              bidService.placeBid(BIDDER_ID, request);
            });

    assertTrue(exception.getMessage().contains("must be at least 110.00"));
  }

  @Test
  void shouldFailWhenInsufficientBalance() {
    // Arrange
    BigDecimal initialPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("500.00");

    Auction auction = createMockAuction(initialPrice);
    UserDao.UserRecord bidder = createMockUser(BIDDER_ID, "bidder01", new BigDecimal("200.00"));

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(userDao.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));

    PlaceBidRequest request = new PlaceBidRequest(AUCTION_ID, bidAmount);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              bidService.placeBid(BIDDER_ID, request);
            });

    assertTrue(exception.getMessage().contains("Insufficient balance"));
  }

  @Test
  void shouldExtendAuctionOnAntiSniping() {
    // Arrange
    BigDecimal initialPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("150.00");

    LocalDateTime endTime = LocalDateTime.now().plusSeconds(10); // Within 30s window
    Auction auction = createMockAuction(initialPrice);
    auction.setEndTime(endTime);

    UserDao.UserRecord bidder = createMockUser(BIDDER_ID, "bidder01", new BigDecimal("1000.00"));

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(userDao.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));

    PlaceBidRequest request = new PlaceBidRequest(AUCTION_ID, bidAmount);

    // Act
    bidService.placeBid(BIDDER_ID, request);

    // Assert
    assertTrue(auction.getEndTime().isAfter(endTime));
    assertEquals(endTime.plusSeconds(60), auction.getEndTime());
  }

  @Test
  void shouldReleasePreviousBidderFunds() {
    // Arrange
    BigDecimal initialPrice = new BigDecimal("100.00");
    BigDecimal bidAmount = new BigDecimal("150.00");
    long previousBidderId = 5L;
    BigDecimal previousMaxBid = new BigDecimal("100.00");

    Auction auction = createMockAuction(initialPrice);
    auction.setHighestBidderId(previousBidderId);
    auction.setHighestMaxBid(previousMaxBid);

    UserDao.UserRecord bidder = createMockUser(BIDDER_ID, "bidder01", new BigDecimal("1000.00"));

    when(auctionDao.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
    when(userDao.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));

    PlaceBidRequest request = new PlaceBidRequest(AUCTION_ID, bidAmount);

    // Act
    bidService.placeBid(BIDDER_ID, request);

    // Assert
    verify(walletService).releaseFunds(eq(previousBidderId), eq(previousMaxBid));
    verify(walletService).lockFunds(eq(BIDDER_ID), eq(bidAmount));
  }

  private Auction createMockAuction(BigDecimal startingPrice) {
    return new Auction(
        AUCTION_ID,
        101L,
        SELLER_ID,
        startingPrice,
        startingPrice,
        startingPrice.add(new BigDecimal("50")),
        null,
        LocalDateTime.now().minusHours(1),
        LocalDateTime.now().plusHours(1),
        AuctionStatus.RUNNING,
        0L,
        LocalDateTime.now());
  }

  private UserDao.UserRecord createMockUser(long id, String username, BigDecimal balance) {
    return new UserDao.UserRecord(
        id,
        username,
        "hash",
        "Full Name",
        com.auction.common.enums.Role.BIDDER,
        balance,
        BigDecimal.ZERO,
        true,
        LocalDateTime.now());
  }
}
