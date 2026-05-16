package com.auction.server.socket;

import com.auction.common.dto.dashboard.DashboardDto;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.UserDao;
import java.math.BigDecimal;
import java.util.List;

final class WalletRequestHandler {

  private final RouterContext context;

  WalletRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<DashboardDto> handleGetDashboard(Request<?> request) {
    Long userId = context.requireActiveUser(request);

    UserDao.UserRecord user =
        context.userDao
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

    BigDecimal balance = user.balance();
    int participatingAuctionsCount = 0;
    int winningAuctionsCount = 0;
    int activeAuctionsCount = 0;
    int totalAuctionsCount = 0;
    int totalUsersCount = 0;

    if (user.role() == Role.BIDDER) {
      List<Long> joinedAuctionIds = context.bidService.getMyBids(userId);
      List<Auction> joinedAuctions =
          joinedAuctionIds.stream()
              .map(context.auctionDao::findById)
              .filter(java.util.Optional::isPresent)
              .map(java.util.Optional::get)
              .toList();

      participatingAuctionsCount =
          (int)
              joinedAuctions.stream()
                  .filter(
                      auction ->
                          auction.getStatus() == AuctionStatus.RUNNING
                              || auction.getStatus() == AuctionStatus.OPEN)
                  .count();

      winningAuctionsCount =
          (int)
              joinedAuctions.stream()
                  .filter(
                      auction ->
                          auction.getStatus() == AuctionStatus.RUNNING
                              && userId.equals(auction.getHighestBidderId()))
                  .count();
    } else if (user.role() == Role.SELLER) {
      List<Auction> sellerAuctions = context.auctionDao.findBySellerId(userId);
      totalAuctionsCount = sellerAuctions.size();
      activeAuctionsCount =
          (int)
              sellerAuctions.stream()
                  .filter(auction -> auction.getStatus() == AuctionStatus.RUNNING)
                  .count();
    } else if (user.role() == Role.ADMIN) {
      List<Auction> allAuctions = context.auctionDao.findAll();
      totalAuctionsCount = allAuctions.size();
      activeAuctionsCount =
          (int)
              allAuctions.stream()
                  .filter(auction -> auction.getStatus() == AuctionStatus.RUNNING)
                  .count();
      totalUsersCount = context.userDao.findAll().size();
    }

    DashboardDto dashboard =
        new DashboardDto(
            balance,
            user.lockedBalance(),
            participatingAuctionsCount,
            winningAuctionsCount,
            activeAuctionsCount,
            totalAuctionsCount,
            totalUsersCount);

    return Response.ok(
        MessageType.GET_DASHBOARD, request.getRequestId(), "Dashboard data loaded", dashboard);
  }

  Response<BigDecimal> handleDeposit(Request<?> request) {
    Long userId = context.requireActiveUser(request);
    BigDecimal amount = context.jsonMapper.convertData(request.getData(), BigDecimal.class);
    if (amount == null) {
      throw new IllegalArgumentException("Missing deposit amount");
    }

    BigDecimal newBalance = context.walletService.deposit(userId, amount);
    return Response.ok(
        MessageType.DEPOSIT, request.getRequestId(), "Deposit successful", newBalance);
  }

  Response<BigDecimal> handleWithdraw(Request<?> request) {
    Long userId = context.requireActiveUser(request);
    BigDecimal amount = context.jsonMapper.convertData(request.getData(), BigDecimal.class);
    if (amount == null) {
      throw new IllegalArgumentException("Missing withdraw amount");
    }

    BigDecimal newBalance = context.walletService.withdraw(userId, amount);
    return Response.ok(
        MessageType.WITHDRAW, request.getRequestId(), "Withdraw successful", newBalance);
  }
}
