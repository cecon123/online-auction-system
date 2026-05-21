package com.auction.server.socket;

import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.exception.BusinessException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Routes socket requests to the correct handler. */
public class RequestRouter {

  private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);

  private final RouterContext context;
  private final AuthRequestHandler authHandler;
  private final AuctionRequestHandler auctionHandler;
  private final BidRequestHandler bidHandler;
  private final WalletRequestHandler walletHandler;
  private final AdminRequestHandler adminHandler;
  private final SubscriptionRequestHandler subscriptionHandler;

  public RequestRouter(PrintWriter clientWriter) {
    this.context = new RouterContext(clientWriter);
    this.authHandler = new AuthRequestHandler(context);
    this.auctionHandler = new AuctionRequestHandler(context);
    this.bidHandler = new BidRequestHandler(context);
    this.walletHandler = new WalletRequestHandler(context);
    this.adminHandler = new AdminRequestHandler(context);
    this.subscriptionHandler = new SubscriptionRequestHandler(context);
  }

  public Response<?> route(Request<?> request) {
    if (request == null) {
      return Response.fail(null, null, "Request body is null");
    }

    if (request.getType() == null) {
      return Response.fail(null, request.getRequestId(), "Request type is required");
    }

    if (!context.idempotencyManager.isNewRequest(request.getRequestId())) {
      logger.warn("Replay attack detected or duplicate request: {}", request.getRequestId());
      return Response.fail(
          request.getType(),
          request.getRequestId(),
          "Duplicate request detected (Replay protection)");
    }

    MessageType type = request.getType();
    logger.info("Request type={} id={}", type, request.getRequestId());

    try {
      return switch (type) {
        case LOGIN -> authHandler.handleLogin(request);
        case REGISTER -> authHandler.handleRegister(request);
        case LOGOUT -> authHandler.handleLogout(request);
        case PLACE_BID -> bidHandler.handlePlaceBid(request);
        case GET_AUCTIONS -> auctionHandler.handleGetAuctions(request);
        case GET_SELLER_AUCTIONS -> auctionHandler.handleGetSellerAuctions(request);
        case GET_SELLER_STATS -> auctionHandler.handleGetSellerStats(request);
        case GET_AUCTION_DETAIL -> auctionHandler.handleGetAuctionDetail(request);
        case CREATE_AUCTION -> auctionHandler.handleCreateAuction(request);
        case UPDATE_AUCTION -> auctionHandler.handleUpdateAuction(request);
        case CANCEL_AUCTION -> auctionHandler.handleCancelAuction(request);
        case DEPOSIT -> walletHandler.handleDeposit(request);
        case WITHDRAW -> walletHandler.handleWithdraw(request);
        case GET_BID_HISTORY -> bidHandler.handleGetBidHistory(request);
        case SUBSCRIBE_AUCTION -> subscriptionHandler.handleSubscribeAuction(request);
        case UNSUBSCRIBE_AUCTION -> subscriptionHandler.handleUnsubscribeAuction(request);
        case GET_MY_BIDS -> bidHandler.handleGetMyBids(request);
        case GET_USER_BID_HISTORY -> bidHandler.handleGetUserBidHistory(request);
        case GET_DASHBOARD -> walletHandler.handleGetDashboard(request);
        case SET_AUTO_BID -> bidHandler.handleSetAutoBid(request);
        case GET_AUTO_BID -> bidHandler.handleGetAutoBid(request);
        case ADMIN_GET_USERS -> adminHandler.handleGetUsers(request);
        case ADMIN_UPDATE_USER_STATUS -> adminHandler.handleUpdateUserStatus(request);
        case ADMIN_GET_AUCTIONS -> auctionHandler.handleAdminGetAuctions(request);
        case ADMIN_CANCEL_AUCTION -> auctionHandler.handleAdminCancelAuction(request);
        default ->
            Response.fail(
                type, request.getRequestId(), "Unsupported message type in router: " + type);
      };
    } catch (BusinessException e) {
      return Response.fail(type, request.getRequestId(), e.getMessage());
    } catch (IllegalArgumentException | IllegalStateException e) {
      return Response.fail(type, request.getRequestId(), e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error processing request: {}", type, e);
      return Response.fail(
          type, request.getRequestId(), "Server internal error: " + e.getMessage());
    }
  }
}
