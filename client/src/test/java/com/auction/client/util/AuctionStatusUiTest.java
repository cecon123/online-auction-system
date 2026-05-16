package com.auction.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.enums.AuctionStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AuctionStatusUiTest {

  @Test
  void shouldUseCanonicalCanceledCssClass() {
    assertEquals("status-canceled", AuctionStatusUi.cssClass(AuctionStatus.CANCELED));
    assertEquals(AuctionStatus.CANCELED, AuctionStatusUi.parse("CANCELLED"));
  }

  @Test
  void shouldProvideDetailSpecificRunningLabel() {
    assertEquals("RUNNING", AuctionStatusUi.badgeText(AuctionStatus.RUNNING));
    assertEquals("LIVE AUCTION", AuctionStatusUi.detailBadgeText(AuctionStatus.RUNNING));
  }

  @Test
  void shouldAllowBidsOnlyForRunningAuctionsBeforeEndTime() {
    assertTrue(
        AuctionStatusUi.acceptsBids(AuctionStatus.RUNNING, LocalDateTime.now().plusMinutes(1)));
    assertFalse(
        AuctionStatusUi.acceptsBids(AuctionStatus.OPEN, LocalDateTime.now().plusMinutes(1)));
    assertFalse(
        AuctionStatusUi.acceptsBids(AuctionStatus.RUNNING, LocalDateTime.now().minusMinutes(1)));
  }
}
