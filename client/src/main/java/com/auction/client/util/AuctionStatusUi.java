package com.auction.client.util;

import com.auction.common.enums.AuctionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.Label;

/** Centralized UI mapping for auction status badges and bid availability messages. */
public final class AuctionStatusUi {

  public static final List<String> STATUS_STYLE_CLASSES =
      List.of(
          "status-badge",
          "status-running",
          "status-open",
          "status-finished",
          "status-paid",
          "status-canceled",
          "status-cancelled",
          "status-ended");

  private AuctionStatusUi() {}

  public static AuctionStatus parse(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase(Locale.ROOT);
    if ("CANCELLED".equals(normalized)) {
      normalized = "CANCELED";
    }
    try {
      return AuctionStatus.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static String cssClass(AuctionStatus status) {
    if (status == null) {
      return "status-ended";
    }
    return switch (status) {
      case RUNNING -> "status-running";
      case OPEN -> "status-open";
      case FINISHED -> "status-finished";
      case PAID -> "status-paid";
      case CANCELED -> "status-canceled";
    };
  }

  public static String badgeText(AuctionStatus status) {
    return status == null ? "UNKNOWN" : status.name();
  }

  public static String detailBadgeText(AuctionStatus status) {
    return status == AuctionStatus.RUNNING ? "LIVE AUCTION" : badgeText(status);
  }

  public static void applyBadge(Label label, AuctionStatus status) {
    applyBadge(label, status, badgeText(status));
  }

  public static void applyDetailBadge(Label label, AuctionStatus status) {
    applyBadge(label, status, detailBadgeText(status));
  }

  public static boolean acceptsBids(AuctionStatus status, LocalDateTime endTime) {
    return status == AuctionStatus.RUNNING
        && endTime != null
        && LocalDateTime.now().isBefore(endTime);
  }

  public static String inactiveBidMessage(AuctionStatus status) {
    if (status == AuctionStatus.OPEN) {
      return "This auction has not started yet.";
    }
    if (status == AuctionStatus.RUNNING) {
      return "This auction has ended. Waiting for settlement.";
    }
    if (status == AuctionStatus.FINISHED) {
      return "This auction has ended and payment is being settled.";
    }
    if (status == AuctionStatus.PAID) {
      return "This auction has been paid.";
    }
    if (status == AuctionStatus.CANCELED) {
      return "This auction has been canceled.";
    }
    return "Bidding is not available for this auction.";
  }

  private static void applyBadge(Label label, AuctionStatus status, String text) {
    label.setText(text);
    label.getStyleClass().removeAll(STATUS_STYLE_CLASSES);
    label.getStyleClass().addAll("status-badge", cssClass(status));
  }
}
