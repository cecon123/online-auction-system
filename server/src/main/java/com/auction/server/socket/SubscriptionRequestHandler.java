package com.auction.server.socket;

import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;

final class SubscriptionRequestHandler {

  private final RouterContext context;

  SubscriptionRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<Void> handleSubscribeAuction(Request<?> request) {
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new IllegalArgumentException("Missing auction ID");
    }

    context.notificationService.subscribe(auctionId, context.clientWriter);
    return Response.ok(
        MessageType.SUBSCRIBE_AUCTION,
        request.getRequestId(),
        "Subscribed to auction " + auctionId,
        null);
  }

  Response<Void> handleUnsubscribeAuction(Request<?> request) {
    Long auctionId = context.jsonMapper.convertData(request.getData(), Long.class);
    if (auctionId == null) {
      throw new IllegalArgumentException("Missing auction ID");
    }

    context.notificationService.unsubscribe(auctionId, context.clientWriter);
    return Response.ok(
        MessageType.UNSUBSCRIBE_AUCTION,
        request.getRequestId(),
        "Unsubscribed from auction " + auctionId,
        null);
  }
}
