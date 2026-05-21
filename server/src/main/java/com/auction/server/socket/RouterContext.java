package com.auction.server.socket;

import com.auction.common.enums.Role;
import com.auction.common.protocol.Request;
import com.auction.server.concurrency.IdempotencyManager;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.AutoBidDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteAutoBidDao;
import com.auction.server.dao.sqlite.SQLiteBidDao;
import com.auction.server.dao.sqlite.SQLiteItemDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.exception.AuthorizationException;
import com.auction.server.exception.ValidationException;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuthService;
import com.auction.server.service.BidService;
import com.auction.server.service.NotificationService;
import com.auction.server.service.SessionManager;
import com.auction.server.service.WalletService;
import com.auction.server.util.JsonMapper;
import java.io.PrintWriter;

final class RouterContext {

  final JsonMapper jsonMapper;
  final AuthService authService;
  final BidService bidService;
  final WalletService walletService;
  final AuctionService auctionService;
  final NotificationService notificationService;
  final SessionManager sessionManager;
  final IdempotencyManager idempotencyManager;
  final AuctionDao auctionDao;
  final UserDao userDao;
  final ItemDao itemDao;
  final PrintWriter clientWriter;

  RouterContext(PrintWriter clientWriter) {
    this.jsonMapper = JsonMapper.getInstance();
    this.clientWriter = clientWriter;
    this.idempotencyManager = IdempotencyManager.getInstance();

    this.userDao = new SQLiteUserDao();
    this.auctionDao = new SQLiteAuctionDao();
    this.itemDao = new SQLiteItemDao();
    BidDao bidDao = new SQLiteBidDao();
    AutoBidDao autoBidDao = new SQLiteAutoBidDao();

    this.authService = new AuthService(userDao);
    this.walletService = new WalletService(userDao);
    this.bidService = new BidService(auctionDao, bidDao, userDao, autoBidDao, walletService);
    this.auctionService = new AuctionService(auctionDao, itemDao, bidDao, walletService);
    this.notificationService = NotificationService.getInstance();
    this.sessionManager = SessionManager.getInstance();
  }

  Long requireActiveUser(Request<?> request) {
    Long userId = sessionManager.getUserId(request.getToken());
    if (userId == null) {
      throw new AuthenticationException("Unauthorized. Please login.");
    }

    UserDao.UserRecord user =
        userDao.findById(userId).orElseThrow(() -> new AuthenticationException("User not found."));

    if (!user.active()) {
      throw new AuthenticationException("Your account has been suspended.");
    }

    return userId;
  }

  Long requireAdmin(Request<?> request) {
    Long userId = requireActiveUser(request);
    UserDao.UserRecord user = userDao.findById(userId).get();

    if (user.role() != Role.ADMIN) {
      throw new AuthorizationException("Access denied. Admin role required.");
    }

    return userId;
  }

  Long requireRole(Request<?> request, Role expectedRole) {
    Long userId = requireActiveUser(request);
    UserDao.UserRecord user = userDao.findById(userId).get();

    if (user.role() != expectedRole) {
      throw new AuthorizationException("Access denied. " + expectedRole + " role required.");
    }

    return userId;
  }

  <T> T requireData(Request<?> request, Class<T> clazz, String errorMessage) {
    T data = jsonMapper.convertData(request.getData(), clazz);

    if (data == null) {
      throw new ValidationException(errorMessage);
    }

    return data;
  }

  boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
